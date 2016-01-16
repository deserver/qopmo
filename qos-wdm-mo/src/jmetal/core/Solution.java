//  Solution.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Description: 
// 
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jmetal.core;

import jmetal.encodings.variable.Binary;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

import qopmo.ag.Individuo;
import qopmo.ag.Solucion;
import qopmo.wdm.Camino;
import qopmo.wdm.CanalOptico;
import qopmo.wdm.Enlace;
import qopmo.wdm.qop.EsquemaRestauracion;
import qopmo.wdm.qop.Nivel;
import qopmo.wdm.qop.Servicio;
import qopmo.wdm.qop.Solicitud;

/**
 * Class representing a solution for a problem.
 */
public class Solution implements Serializable, Individuo {  
	/**
	 * Stores the problem 
	 */
  private Problem problem_ ;
	
  /**
   * Stores the type of the encodings.variable
   */	
  //private SolutionType type_ ; 
  
  private Solucion type_;

  /**
   * Stores the decision variables of the solution.
   */
  private Variable[] variable_ ;

  /**
   * Stores the objectives values of the solution.
   */
  private final double [] objective_ ;

  /**
   * Stores the number of objective values of the solution
   */
  private int numberOfObjectives_ ;

  /**
   * Stores the so called fitness value. Used in some metaheuristics
   */
  public double fitness_ ;

  /**
   * Used in algorithm AbYSS, this field is intended to be used to know
   * when a <code>Solution</code> is marked.
   */
  private boolean marked_ ;

  /**
   * Stores the so called rank of the solution. Used in NSGA-II
   */
  private int rank_ ;

  /**
   * Stores the overall constraint violation of the solution.
   */
  private double  overallConstraintViolation_ ;

  /**
   * Stores the number of constraints violated by the solution.
   */
  private int  numberOfViolatedConstraints_ ;

  /**
   * This field is intended to be used to know the location of
   * a solution into a <code>SolutionSet</code>. Used in MOCell
   */
  private int location_ ;

  /**
   * Stores the distance to his k-nearest neighbor into a 
   * <code>SolutionSet</code>. Used in SPEA2.
   */
  private double kDistance_ ; 

  /**
   * Stores the crowding distance of the the solution in a 
   * <code>SolutionSet</code>. Used in NSGA-II.
   */
  private double crowdingDistance_ ; 

  /**
   * Stores the distance between this solution and a <code>SolutionSet</code>.
   * Used in AbySS.
   */
  private double distanceToSolutionSet_ ;    
  
  @GeneratedValue
  private long id;

  public double costo;


	@ManyToMany(cascade = CascadeType.ALL)
	public Set<Servicio> genes;	

  @Transient
  public int contadorFailOroPrimario = 0;
  @Transient
  public int contadorFailPlataPrimario = 0;
  @Transient
  public int contadorFailBroncePrimario = 0;
  @Transient
  public int contadorFailOroAlternativo = 0;
  @Transient
  public int contadorFailPlataAlternativo = 0;
  @Transient
  public Set<Enlace> enlacesContado;

	// Valor por kilometro.
	public static double a = 0.1;
	// Valor por cambio de longitud de onda
	public static double b = 2;


/**
   * Constructor.
   */
  public Solution() {        
  	problem_                      = null  ;
    marked_                       = false ;
    overallConstraintViolation_   = 0.0   ;
    numberOfViolatedConstraints_  = 0     ;  
    type_                         = null ;
    variable_                     = null ;
    objective_                    = null ;
	this.genes = new TreeSet<Servicio>();
	this.fitness_ = Double.MAX_VALUE;
	this.costo = Double.MAX_VALUE;
  } // Solution

  /**
   * Constructor
   * @param numberOfObjectives Number of objectives of the solution
   * 
   * This constructor is used mainly to read objective values from a file to
   * variables of a SolutionSet to apply quality indicators
   */
  public Solution(int numberOfObjectives) {
    numberOfObjectives_ = numberOfObjectives;
    objective_          = new double[numberOfObjectives];
  }
  
  public Solution(Set<Solicitud> solicitudes) {

		Set<Servicio> servicios = new TreeSet<Servicio>();
		for (Solicitud s : solicitudes) {
			Servicio servicio = new Servicio(s);
			servicio.setDisponible(true);
			servicios.add(servicio);
		}

		this.genes = new TreeSet<Servicio>(servicios);
		this.fitness_ = Double.MAX_VALUE;
		this.costo = Double.MAX_VALUE;
		objective_          = new double[numberOfObjectives_];
		
	}
  
  /** 
   * Constructor.
   * @param problem The problem to solve
   * @throws ClassNotFoundException 
   */
  public Solution(Problem problem) throws ClassNotFoundException{
    problem_ = problem ; 
    type_ = problem.getTipoSolucion_();
    numberOfObjectives_ = problem.getNumberOfObjectives() ;
    objective_          = new double[numberOfObjectives_] ;

    // Setting initial values
    fitness_              = 0.0 ;
    kDistance_            = 0.0 ;
    crowdingDistance_     = 0.0 ;        
    distanceToSolutionSet_ = Double.POSITIVE_INFINITY ;
    //<-
	this.genes = new TreeSet<Servicio>();
	this.fitness_ = Double.MAX_VALUE;
	this.costo = Double.MAX_VALUE;

    //variable_ = problem.solutionType_.createVariables() ; 
    /*******************************************************************************************************************
     * **********************************************************************************************************
     */
    //variable_ = type_.createVariables() ; 
  } // Solution
  
  static public Solution getNewSolution(Problem problem) throws ClassNotFoundException {
    return new Solution(problem) ;
  }
  
  /** 
   * Constructor
   * @param problem The problem to solve
   */
  public Solution(Problem problem, Variable [] variables){
    problem_ = problem ;
  	type_ = problem.getTipoSolucion_() ;
    numberOfObjectives_ = problem.getNumberOfObjectives() ;
    objective_          = new double[numberOfObjectives_] ;

    // Setting initial values
    fitness_              = 0.0 ;
    kDistance_            = 0.0 ;
    crowdingDistance_     = 0.0 ;        
    distanceToSolutionSet_ = Double.POSITIVE_INFINITY ;
    //<-

    variable_ = variables ;
	this.genes = new TreeSet<Servicio>();
	this.fitness_ = Double.MAX_VALUE;
	this.costo = Double.MAX_VALUE;
  } // Constructor
  
  /** 
   * Copy constructor.
   * @param solution Solution to copy.
   */    
  public Solution(Solution solution) {            
    problem_ = solution.problem_ ;
    type_ = solution.type_;

    numberOfObjectives_ = solution.getNumberOfObjectives();
    objective_ = new double[numberOfObjectives_];
    for (int i = 0; i < objective_.length;i++) {
      objective_[i] = solution.getObjective(i);
    } // for
	this.genes = new TreeSet<Servicio>();
	this.fitness_ = Double.MAX_VALUE;
	this.costo = Double.MAX_VALUE;
    //<-
/**
 * **************************************************************************************************************************
 */
    //variable_ = type_.copyVariables(solution.variable_) ;
    overallConstraintViolation_  = solution.getOverallConstraintViolation();
    numberOfViolatedConstraints_ = solution.getNumberOfViolatedConstraint();
    distanceToSolutionSet_ = solution.getDistanceToSolutionSet();
    crowdingDistance_     = solution.getCrowdingDistance();
    kDistance_            = solution.getKDistance();                
    fitness_              = solution.getFitness();
    marked_               = solution.isMarked();
    rank_                 = solution.getRank();
    location_             = solution.getLocation();
  } // Solution

  /**
   * Sets the distance between this solution and a <code>SolutionSet</code>.
   * The value is stored in <code>distanceToSolutionSet_</code>.
   * @param distance The distance to a solutionSet.
   */
  public void setDistanceToSolutionSet(double distance){
    distanceToSolutionSet_ = distance;
  } // SetDistanceToSolutionSet

  /**
   * Gets the distance from the solution to a <code>SolutionSet</code>. 
   * <b> REQUIRE </b>: this method has to be invoked after calling 
   * <code>setDistanceToPopulation</code>.
   * @return the distance to a specific solutionSet.
   */
  public double getDistanceToSolutionSet(){
    return distanceToSolutionSet_;
  } // getDistanceToSolutionSet


  /** 
   * Sets the distance between the solution and its k-nearest neighbor in 
   * a <code>SolutionSet</code>. The value is stored in <code>kDistance_</code>.
   * @param distance The distance to the k-nearest neighbor.
   */
  public void setKDistance(double distance){
    kDistance_ = distance;
  } // setKDistance

  /** 
   * Gets the distance from the solution to his k-nearest nighbor in a 
   * <code>SolutionSet</code>. Returns the value stored in
   * <code>kDistance_</code>. <b> REQUIRE </b>: this method has to be invoked 
   * after calling <code>setKDistance</code>.
   * @return the distance to k-nearest neighbor.
   */
  double getKDistance(){
    return kDistance_;
  } // getKDistance

  /**
   * Sets the crowding distance of a solution in a <code>SolutionSet</code>.
   * The value is stored in <code>crowdingDistance_</code>.
   * @param distance The crowding distance of the solution.
   */  
  public void setCrowdingDistance(double distance){
    crowdingDistance_ = distance;
  } // setCrowdingDistance


  /** 
   * Gets the crowding distance of the solution into a <code>SolutionSet</code>.
   * Returns the value stored in <code>crowdingDistance_</code>.
   * <b> REQUIRE </b>: this method has to be invoked after calling 
   * <code>setCrowdingDistance</code>.
   * @return the distance crowding distance of the solution.
   */
  public double getCrowdingDistance(){
    return crowdingDistance_;
  } // getCrowdingDistance

  /**
   * Sets the fitness of a solution.
   * The value is stored in <code>fitness_</code>.
   * @param fitness The fitness of the solution.
   */
  public void setFitness(double fitness) {
    fitness_ = fitness;
  } // setFitness

  /**
   * Gets the fitness of the solution.
   * Returns the value of stored in the encodings.variable <code>fitness_</code>.
   * <b> REQUIRE </b>: This method has to be invoked after calling 
   * <code>setFitness()</code>.
   * @return the fitness.
   */
  public double getFitness() {
    return fitness_;
  } // getFitness

  /**
   * Sets the value of the i-th objective.
   * @param i The number identifying the objective.
   * @param value The value to be stored.
   */
  public void setObjective(int i, double value) {
    objective_[i] = value;
  } // setObjective

  /**
   * Returns the value of the i-th objective.
   * @param i The value of the objective.
   */
  public double getObjective(int i) {
    return objective_[i];
  } // getObjective

  /**
   * Returns the number of objectives.
   * @return The number of objectives.
   */
  public int getNumberOfObjectives() {
    if (objective_ == null)
      return 0 ;
    else
      return numberOfObjectives_;
  } // getNumberOfObjectives

  /**  
   * Returns the number of decision variables of the solution.
   * @return The number of decision variables.
   */
  public int numberOfVariables() {
    return problem_.getNumberOfVariables() ;
  } // numberOfVariables

  /** 
   * Returns a string representing the solution.
   * @return The string.
   */
  /*
  public String toString() {
    String aux="";
    for (int i = 0; i < this.numberOfObjectives_; i++)
      aux = aux + this.getObjective(i) + " ";

    return aux;
  } // toString
  */
  @Override
	public String toString() {
		final int maxLen = genes.size();
		return "[Solucion(" + this.id + "):\n [fitness=" + fitness_ + ", costo="
				+ costo + "(" + this.contadorCosto + "#" + this.cambiosLDO
				+ "), genes="
				+ (genes != null ? toString(genes, maxLen) : "Vacio.") + "]";
	}

	private String toString(Set<Servicio> collection, int maxLen) {
		StringBuilder builder = new StringBuilder();
		builder.append(" [\n");
		int i = 0;
		for (Iterator<Servicio> iterator = collection.iterator(); iterator
				.hasNext(); i++) {
			if (i > 0)
				builder.append(", \n");
			builder.append(iterator.next().toString());
		}
		builder.append("]");
		return builder.toString();
	}

	public TreeSet<Servicio> getGenes() {
		return (TreeSet<Servicio>) genes;
	}

	public void setGenes(Collection<Servicio> hijoAux) {
		this.genes = (TreeSet<Servicio>) hijoAux;
	}

/**
   * Returns the decision variables of the solution.
   * @return the <code>DecisionVariables</code> object representing the decision
   * variables of the solution.
   */
  public Variable[] getDecisionVariables() {
    return variable_ ;
  } // getDecisionVariables

  /**
   * Sets the decision variables for the solution.
   * @param variables The <code>DecisionVariables</code> object
   * representing the decision variables of the solution.
   */
  public void setDecisionVariables(Variable [] variables) {
    variable_ = variables ;
  } // setDecisionVariables

  public Problem getProblem() {
     return problem_ ;
  }

  /**
   * Indicates if the solution is marked.
   * @return true if the method <code>marked</code> has been called and, after 
   * that, the method <code>unmarked</code> hasn't been called. False in other
   * case.
   */
  public boolean isMarked() {
    return this.marked_;
  } // isMarked

  /**
   * Establishes the solution as marked.
   */
  public void marked() {
    this.marked_ = true;
  } // marked

  /**
   * Established the solution as unmarked.
   */
  public void unMarked() {
    this.marked_ = false;
  } // unMarked

  /**  
   * Sets the rank of a solution. 
   * @param value The rank of the solution.
   */
  public void setRank(int value){
    this.rank_ = value;
  } // setRank

  /**
   * Gets the rank of the solution.
   * <b> REQUIRE </b>: This method has to be invoked after calling 
   * <code>setRank()</code>.
   * @return the rank of the solution.
   */
  public int getRank(){
    return this.rank_;
  } // getRank

  /**
   * Sets the overall constraints violated by the solution.
   * @param value The overall constraints violated by the solution.
   */
  public void setOverallConstraintViolation(double value) {
    this.overallConstraintViolation_ = value;
  } // setOverallConstraintViolation

  /**
   * Gets the overall constraint violated by the solution.
   * <b> REQUIRE </b>: This method has to be invoked after calling 
   * <code>overallConstraintViolation</code>.
   * @return the overall constraint violation by the solution.
   */
  public double getOverallConstraintViolation() {
    return this.overallConstraintViolation_;
  }  //getOverallConstraintViolation


  /**
   * Sets the number of constraints violated by the solution.
   * @param value The number of constraints violated by the solution.
   */
  public void setNumberOfViolatedConstraint(int value) {
    this.numberOfViolatedConstraints_ = value;
  } //setNumberOfViolatedConstraint

  /**
   * Gets the number of constraint violated by the solution.
   * <b> REQUIRE </b>: This method has to be invoked after calling
   * <code>setNumberOfViolatedConstraint</code>.
   * @return the number of constraints violated by the solution.
   */
  public int getNumberOfViolatedConstraint() {
    return this.numberOfViolatedConstraints_;
  } // getNumberOfViolatedConstraint

  /**
   * Sets the location of the solution into a solutionSet. 
   * @param location The location of the solution.
   */
  public void setLocation(int location) {
    this.location_ = location;
  } // setLocation

  /**
   * Gets the location of this solution in a <code>SolutionSet</code>.
   * <b> REQUIRE </b>: This method has to be invoked after calling
   * <code>setLocation</code>.
   * @return the location of the solution into a solutionSet
   */
  public int getLocation() {
    return this.location_;
  } // getLocation

  /**
   * Sets the type of the encodings.variable.
   * @param type The type of the encodings.variable.
   */
  //public void setType(String type) {
   // type_ = Class.forName("") ;
  //} // setType

  /**
   * Sets the type of the encodings.variable.
   * @param type The type of the encodings.variable.
   */
  public void setType(Solucion type) {
    type_ = type ;
  } // setType

  /**
   * Gets the type of the encodings.variable
   * @return the type of the encodings.variable
   */
  public Solucion getType() {
    return type_;
  } // getType

  /** 
   * Returns the aggregative value of the solution
   * @return The aggregative value.
   */
  public double getAggregativeValue() {
    double value = 0.0;                
    for (int i = 0; i < getNumberOfObjectives(); i++){
      value += getObjective(i);
    }                
    return value;
  } // getAggregativeValue

  /**
   * Returns the number of bits of the chromosome in case of using a binary
   * representation
   * @return The number of bits if the case of binary variables, 0 otherwise
   * This method had a bug which was fixed by Rafael Olaechea
   */
  public int getNumberOfBits() {
    int bits = 0 ;

    for (int i = 0;  i < variable_.length  ; i++)
      if ((variable_[i].getVariableType() == jmetal.encodings.variable.Binary.class) ||
          (variable_[i].getVariableType() == jmetal.encodings.variable.BinaryReal.class))

        bits += ((Binary)(variable_[i])).getNumberOfBits() ;

    return bits ;
  } // getNumberOfBits
  

	public double getCosto() {
		return costo;
	}
	
	public void setCosto(double costo) {
		this.costo = costo;
	}
	
	public int getContadorFailOroPrimario() {
		return contadorFailOroPrimario;
	}
	
	public void setContadorFailOroPrimario(int contadorFailOroPrimario) {
		this.contadorFailOroPrimario = contadorFailOroPrimario;
	}
	
	public int getContadorFailPlataPrimario() {
		return contadorFailPlataPrimario;
	}
	
	public void setContadorFailPlataPrimario(int contadorFailPlataPrimario) {
		this.contadorFailPlataPrimario = contadorFailPlataPrimario;
	}
	
	public int getContadorFailBroncePrimario() {
		return contadorFailBroncePrimario;
	}
	
	public void setContadorFailBroncePrimario(int contadorFailBroncePrimario) {
		this.contadorFailBroncePrimario = contadorFailBroncePrimario;
	}
	
	public int getContadorFailOroAlternativo() {
		return contadorFailOroAlternativo;
	}
	
	public void setContadorFailOroAlternativo(int contadorFailOroAlternativo) {
		this.contadorFailOroAlternativo = contadorFailOroAlternativo;
	}
	
	public int getContadorFailPlataAlternativo() {
		return contadorFailPlataAlternativo;
	}
	
	public void setContadorFailPlataAlternativo(int contadorFailPlataAlternativo) {
		this.contadorFailPlataAlternativo = contadorFailPlataAlternativo;
	}
	
	public int getContadorFailOro() {
		return contadorFailOroPrimario;
	}

	public void setContadorFailOro(int contadorFailOro) {
		this.contadorFailOroPrimario = contadorFailOro;
	}

	public int getContadorFailPlata() {
		return contadorFailPlataPrimario;
	}

	public void setContadorFailPlata(int contadorFailPlata) {
		this.contadorFailPlataPrimario = contadorFailPlata;
	}

	public int getContadorFailBronce() {
		return contadorFailBroncePrimario;
	}

	public void setContadorFailBronce(int contadorFailBronce) {
		this.contadorFailBroncePrimario = contadorFailBronce;
	}

	
	/**
	 * Función que compara la solucion con otra. Si los valores resultantes son
	 * 0, entonces las soluciones son iguales, si los valores resultantes son
	 * menores a 0, esta solucion es mejor; y si son mayores a 0 el parametro
	 * recibido es mejor. Las prioridades siguen el siguiente orden:
	 * Primario_Oro, Secundario_Oro, Primario_Plata, Secundario_Plata, Bronce.
	 * 
	 * @param s
	 * @return
	 */
	public boolean comparar(Solution i) {
		Solution s = (Solution) i;
		boolean retorno = false;
		int oroP = this.contadorFailOroPrimario;
		oroP -= s.contadorFailOroPrimario;
		int oroA = this.contadorFailOroAlternativo;
		oroA -= s.contadorFailOroAlternativo;
		/*
		 * int plataP = this.contadorFailPlataPrimario; plataP -=
		 * s.contadorFailPlataPrimario; int plataA =
		 * this.contadorFailPlataAlternativo; plataA -=
		 * s.contadorFailPlataAlternativo; int bronce =
		 * this.contadorFailBroncePrimario; bronce -=
		 * s.contadorFailBroncePrimario;
		 */
		double costoResultante = this.costo - s.costo;

		if (oroP == 0) {
			if (oroA == 0) {
				if (costoResultante == 0) {
					retorno = false;
				} else {
					if (costoResultante > 0)
						retorno = true;
					else
						retorno = false;
				}
			} else {
				if (oroA > 0)
					retorno = true;
				else
					retorno = false;
			}
		} else {
			if (oroP > 0)
				retorno = true;
			else
				retorno = false;
		}

		return retorno;
	}
	
	/**
	 * Calcula el costo en función de la Fórmula de Evaluación Definida. Tambien
	 * mantiene contadores de Alternativos no existentes, cuando deberían
	 * existir.
	 * <p>
	 * Costo = suma_de_distancia x a + suma_de_cambios_LDO x b
	 * </p>
	 */
	@Override
	public double evaluar() {

		this.contadorFailOroPrimario = 0;
		this.contadorFailOroAlternativo = 0;
		this.contadorFailPlataPrimario = 0;
		this.contadorFailPlataAlternativo = 0;
		this.contadorFailBroncePrimario = 0;

		// Costo de una Solucion
		this.costo = this.costoTotalCanales2();
		// Fitness de la Solución
		this.fitness_ = 1 / this.costo;

		return this.fitness_;
	}
	
	public int contadorCosto;
	public int cambiosLDO;
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
	
	public boolean comparar(Individuo i) {
		Solution s = (Solution) i;
		boolean retorno = false;
		int oroP = this.contadorFailOroPrimario;
		oroP -= s.contadorFailOroPrimario;
		int oroA = this.contadorFailOroAlternativo;
		oroA -= s.contadorFailOroAlternativo;
		/*
		 * int plataP = this.contadorFailPlataPrimario; plataP -=
		 * s.contadorFailPlataPrimario; int plataA =
		 * this.contadorFailPlataAlternativo; plataA -=
		 * s.contadorFailPlataAlternativo; int bronce =
		 * this.contadorFailBroncePrimario; bronce -=
		 * s.contadorFailBroncePrimario;
		 */
		double costoResultante = this.costo - s.costo;

		if (oroP == 0) {
			if (oroA == 0) {
				if (costoResultante == 0) {
					retorno = false;
				} else {
					if (costoResultante > 0)
						retorno = true;
					else
						retorno = false;
				}
			} else {
				if (oroA > 0)
					retorno = true;
				else
					retorno = false;
			}
		} else {
			if (oroP > 0)
				retorno = true;
			else
				retorno = false;
		}

		return retorno;
	}
	/*
	 * OPERACIONES SOBRE LA SOLUCION
	 */

	/**
	 * Función para generar Servicios Randómicos.
	 * 
	 * @param esquema
	 */
	public void random(EsquemaRestauracion esquema) {
		for (Servicio s : this.genes) {
			s.getSolicitud().setEsquema(esquema);
			s.random();
		}
	}

	public void extremos(int i) {
		EsquemaRestauracion e = null;
		if (i == 1) {
			e = EsquemaRestauracion.FullPath;
		} else if (i == 2) {
			e = EsquemaRestauracion.Link;
		}

		for (Servicio s : this.genes) {
			s.getSolicitud().setEsquema(e);
			s.extremos();
			s.getSolicitud().setEsquema(EsquemaRestauracion.Segment);
		}
	}


	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	

} // Solution
