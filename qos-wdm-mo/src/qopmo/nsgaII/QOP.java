package qopmo.nsgaII;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.SolutionType;
import jmetal.core.Variable;
import jmetal.encodings.solutionType.BinaryRealSolutionType;
import jmetal.encodings.solutionType.RealSolutionType;
import jmetal.encodings.variable.Permutation;
import jmetal.util.JMException;
import qopmo.ag.Solucion;
import qopmo.wdm.Camino;
import qopmo.wdm.CanalOptico;
import qopmo.wdm.Enlace;
import qopmo.wdm.qop.EsquemaRestauracion;
import qopmo.wdm.qop.Nivel;
import qopmo.wdm.qop.Servicio;

public class QOP extends Problem {
	private int contadorFailOroPrimario = 0;
	private int contadorFailPlataPrimario = 0;
	private int contadorFailBroncePrimario = 0;
	private int contadorFailOroAlternativo = 0;
	private int contadorFailPlataAlternativo = 0;
	private Set<Enlace> enlacesContado;
	
	//Genes de la solución (Conjunto de Servicios)
	@ManyToMany(cascade = CascadeType.ALL)
	private Set<Servicio> genes;
	
	// Fitness de la Solución
	private double fitness;

	// Costo de la Solución
	private double costo;
	
	// Valor por kilometro.
	public static double a = 0.1;
	// Valor por cambio de longitud de onda
	public static double b = 2;
	
	/*
	 * Obtiene el costo total de los canales utilizados en la solución.
	 * 
	 * @return
	 */
	public int contadorCosto;
	public int cambiosLDO;
	
	
	
	public QOP (){
		numberOfVariables_= 1;
		numberOfObjectives_ = 4;
		numberOfConstraints_ = 0; 
		problemName_ = "qop_wdm";
		
		
		solutionType_ = new RealSolutionType(this) ;
	}

	
	@Override
	public void evaluate(Solution solution) throws JMException {
		this.contadorFailOroPrimario = 0;
		this.contadorFailOroAlternativo = 0;
		this.contadorFailPlataPrimario = 0;
		this.contadorFailPlataAlternativo = 0;
		this.contadorFailBroncePrimario = 0;
		

		// Costo de una Solucion
		this.costo = this.costoTotalCanales2();
		// Fitness de la Solución
		this.fitness = 1 / this.costo;

		//return this.fitness;
	}
	
	/*
	 * Obtiene el costo total de los canales utilizados en la solución.
	 * 
	 * @return
	 */
	private double costoTotalCanales2() {
		contadorCosto = 0;
		cambiosLDO = 0;
		enlacesContado = new HashSet<Enlace>();
		/*
		 * El cálculo de las variables del costo se suman para cada gen
		 * (Servicio) del individuo (Solucion).
		 */
		for (Servicio gen : this.genes) {

			if (gen == null)
				continue;

			// Se cuenta cada Oro que no tiene un alternativo.
			if (!gen.oroTieneAlternativo())
				this.contadorFailOroAlternativo++;

			// Se cuenta cada Plata que no tiene un alternativo.
			if (!gen.plataTieneAlternativo())
				this.contadorFailPlataAlternativo++;

			/*
			 * Evaluacion Primario: Si no tiene primario se cuenta como Error.
			 * Si tiene primario se suman sus costos de Canales Opticos
			 * utilizados y se cuentan los cambios de Longitud de Onda
			 * realizados.
			 */
			Camino primario = gen.getPrimario();

			if (primario == null) {
				if (gen.getSolicitud().getNivel() == Nivel.Oro)
					this.contadorFailOroPrimario++;
				else if (gen.getSolicitud().getNivel() == Nivel.Plata1)
					this.contadorFailPlataPrimario++;
				else if (gen.getSolicitud().getNivel() == Nivel.Bronce)
					this.contadorFailBroncePrimario++;
			} else {
				// Se cuentan y suman los enlaces y cambios de longitud de onda
				// del primario.
				contadorInterno(primario.getEnlaces());
			}

			/*
			 * Evaluación Alternativo: Si tiene alternativo se suman los costos
			 * de Canales Opticos utilizados y se cuentan los cambios de
			 * Longitud de Onda realizados. Link-Oriented es un caso especial.
			 */
			if (gen.getSolicitud().getEsquema() != EsquemaRestauracion.Link) {
				Camino alternativo = gen.getAlternativo();
				if (alternativo != null) {
					contadorInterno(alternativo.getEnlaces());
				}
			} else {
				if (gen.getAlternativoLink() != null) {
					for (Camino alternativo : gen.getAlternativoLink()) {
						if (alternativo != null) {
							contadorInterno(alternativo.getEnlaces());
						}
					}
				}
			}
		}

		// Fórmula de Costo de una Solución
		double costo = (contadorCosto * a) + (cambiosLDO * b);
		return costo;
	}
	
	/**
	 * Cuenta la cantidad de Enlaces y los cambios de longitud de onda de los
	 * enlaces obtenidos como parámetro. Se suman a los atributos locales
	 * contadorCosto y cambiosLDO.
	 */
	private void contadorInterno(Set<Enlace> enlaces) {
		// Si se utiliza el auxiliar debe definirse como variable global.
		// Set<CanalOptico> auxiliar = new HashSet<CanalOptico>();
		
		Enlace e1 = null;
		Enlace e2 = null;
		int ldo1 = 0;
		int ldo2 = 0;
		boolean primero = true;

		for (Enlace s : enlaces) {
			if (s == null)
				continue;
			CanalOptico ca = s.getCanal();

			if (primero) {
				e1 = s;
				primero = false;
			} else {
				e1 = e2;
			}
			ldo1 = e1.getLongitudDeOnda();
			e2 = s;
			ldo2 = e2.getLongitudDeOnda();

			// Si existe un cambio de longitud de onda, se suman en 1.
			if (ldo1 != ldo2)
				cambiosLDO = 0;

			// inserto es false cuando ya existía (no suma)
			// boolean inserto = auxiliar.add(ca);
			// se suman costos de Canales Opticos utilizados.
			// if (inserto)
			if (!enlacesContado.contains(s)) {
				enlacesContado.add(s);
				contadorCosto += ca.getCosto();
			}
		}

	}

}
/*

public ConstrEx(String solutionType) {
    numberOfVariables_  = 2;
    numberOfObjectives_ = 2;
    numberOfConstraints_= 2;
    problemName_        = "Constr_Ex";
        
    lowerLimit_ = new double[numberOfVariables_];
    upperLimit_ = new double[numberOfVariables_];        
    lowerLimit_[0] = 0.1;
    lowerLimit_[1] = 0.0;        
    upperLimit_[0] = 1.0;
    upperLimit_[1] = 5.0;
        
    if (solutionType.compareTo("BinaryReal") == 0)
      solutionType_ = new BinaryRealSolutionType(this) ;
    else if (solutionType.compareTo("Real") == 0)
    	solutionType_ = new RealSolutionType(this) ;
    else {
    	System.out.println("Error: solution type " + solutionType + " invalid") ;
    	System.exit(-1) ;
    }  
  } // ConstrEx
     
  /** 
  * Evaluates a solution 
  * @param solution The solution to evaluate
   * @throws JMException 
  */
/*
  public void evaluate(Solution solution) throws JMException {
    Variable[] variable  = solution.getDecisionVariables();
       
    double [] f = new double[numberOfObjectives_];
    f[0] = variable[0].getValue();        
    f[1] = (1.0 + variable[1].getValue())/variable[0].getValue();        
    
    solution.setObjective(0,f[0]);
    solution.setObjective(1,f[1]);
  } // evaluate

 /** 
  * Evaluates the constraint overhead of a solution 
  * @param solution The solution
 * @throws JMException 
  */
/*
  public void evaluateConstraints(Solution solution) throws JMException {
    double [] constraint = new double[this.getNumberOfConstraints()];

    double x1 = solution.getDecisionVariables()[0].getValue();
    double x2 = solution.getDecisionVariables()[1].getValue();
        
    constraint[0] =  (x2 + 9*x1 -6.0) ;
    constraint[1] =  (-x2 + 9*x1 -1.0);
        
    double total = 0.0;
    int number = 0;
    for (int i = 0; i < this.getNumberOfConstraints(); i++)
      if (constraint[i]<0.0){
        total+=constraint[i];
        number++;
      }
        
    solution.setOverallConstraintViolation(total);    
    solution.setNumberOfViolatedConstraint(number);         
  } // evaluateConstraints  
} // ConstrEx
*/