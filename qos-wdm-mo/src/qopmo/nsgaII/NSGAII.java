//  NSGAII.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
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

package qopmo.nsgaII;



import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import jmetal.core.*;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.Distance;
import jmetal.util.JMException;
import jmetal.util.Ranking;
import jmetal.util.comparators.CrowdingComparator;

import qopmo.ag.operadores.impl.TorneoBinario;
import qopmo.wdm.Red;
import qopmo.wdm.qop.Caso;
import qopmo.wdm.qop.EsquemaRestauracion;
import qopmo.util.CSVWriter;
import qopmo.ag.operadores.*;
import qopmo.ag.Individuo;
import qopmo.ag.Poblacion;
import qopmo.ag.Solucion;

/** 
 *  Implementation of NSGA-II.
 *  This implementation of NSGA-II makes use of a QualityIndicator object
 *  to obtained the convergence speed of the algorithm. This version is used
 *  in the paper:
 *     A.J. Nebro, J.J. Durillo, C.A. Coello Coello, F. Luna, E. Alba 
 *     "A Study of Convergence Speed in Multi-Objective Metaheuristics." 
 *     To be presented in: PPSN'08. Dortmund. September 2008.
 */

public class NSGAII extends Algorithm {
  /**
   * Constructor
   * @param problem Problem to solve
   */
  public NSGAII(Problem problem) {
    super (problem) ;
  } // NSGAII
	  
  private static EntityManagerFactory emf = Persistence
				.createEntityManagerFactory("tesis");
  private static EntityManager em = emf.createEntityManager();
  private String casoPrincipal = "CasoCNunez_";
  
  public Poblacion population;
  public Red NSFNET;
  public EsquemaRestauracion esquema = EsquemaRestauracion.Link;
  private CSVWriter csv = new CSVWriter();

  /**   
   * Runs the NSGA-II algorithm.
   * @return a <code>SolutionSet</code> that is a set of non dominated solutions
   * as a result of the algorithm execution
   * @throws JMException 
   */
  public Poblacion execute() throws JMException, ClassNotFoundException {
    int populationSize;
    int maxEvaluations;
    int evaluations;
    
    QualityIndicator indicators; // QualityIndicator object
    int requiredEvaluations; // Use in the example of use of the
    // indicators object (see below)

    //Poblacion population;
    //SolutionSet population;
    Poblacion offspringPopulation;
    Poblacion union;

    Operator mutationOperator;
    Operator crossoverOperator;
    Operator selectionOperator;

    Distance distance = new Distance();

    //Read the parameters
    populationSize = ((Integer) getInputParameter("populationSize")).intValue();
    maxEvaluations = ((Integer) getInputParameter("maxEvaluations")).intValue();
    indicators = (QualityIndicator) getInputParameter("indicators");

    //Initialize the variables
    //population = new SolutionSet(populationSize);
    evaluations = 0;

    
    //Initialize Nsfnet
	NSFNET = em.find(Red.class, 1); // NSFnet
	NSFNET.inicializar();
    
    this.obtenerPoblacion(populationSize);
	

    requiredEvaluations = 0;

    //Read the operators
    mutationOperator = operators_.get("mutation");
    crossoverOperator = operators_.get("crossover");
    //selectionOperator = operators_.get("selection");
    OperadorSeleccion seleccionOp = new TorneoBinario();

    // Create the initial solutionSet
    /*Solution newSolution;
    for (int i = 0; i < populationSize; i++) {
      newSolution = new Solution(problem_);
      problem_.evaluate(newSolution);
      problem_.evaluateConstraints(newSolution);
      evaluations++;
      population.add(newSolution);
    } //for       */

    // Generations 
    while (evaluations < maxEvaluations) {

      // Create the offSpring solutionSet      
      offspringPopulation = new Poblacion(populationSize);
      Solution parents = new Solution();
      
      for (int i = 0; i < (populationSize); i++) {
        if (evaluations < maxEvaluations) {
          //obtain parents
          //parents[0] = (Solution) selectionOperator.execute(population);
          //parents[1] = (Solution) selectionOperator.execute(population);
          population.evaluar();	
        	
        	
        	
        	//Binary Tournament Application
          //parents = (Solution) seleccionOp.seleccionar(population);
          Collection<Individuo> selectos = seleccionOp.seleccionar(population);
          
          //Crossover
          Solution offSpring = (Solution) population.cruzar(selectos);
          
          
          //Solution[] offSpring = (Solution[]) crossoverOperator.execute(parents);
          //mutationOperator.execute(offSpring[0]);
          //mutationOperator.execute(offSpring[1]);
          
          problem_.evaluate(offSpring);
          //problem_.evaluateConstraints(offSpring[0]);
          //problem_.evaluate(offSpring[1]);
          //problem_.evaluateConstraints(offSpring[1]);
          
          offspringPopulation.add(offSpring);
          //offspringPopulation.add(offSpring[1]);
          
          csv.addValor(population.almacenarMejor(evaluations));
          evaluations++;
        } // if                            
      } // for

      // Create the solutionSet union of solutionSet and offSpring
     // union = ((SolutionSet) population).union(offspringPopulation);
      
      union = offspringPopulation;
      // Ranking the union
      Ranking ranking = new Ranking(union);

      int remain = populationSize;
      int index = 0;
      Poblacion front = null;
      population.clear();

      // Obtain the next front
      front = ranking.getSubfront(index);

      while ((remain > 0) && (remain >= front.size())) {
        //Assign crowding distance to individuals
        distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());
        //Add the individuals of this front
        for (int k = 0; k < front.size(); k++) {
          population.add(front.get(k));
        } // for

        //Decrement remain
        remain = remain - front.size();

        //Obtain the next front
        index++;
        if (remain > 0) {
          front = ranking.getSubfront(index);
        } // if        
      } // while

      // Remain is less than front(index).size, insert only the best one
      if (remain > 0) {  // front contains individuals to insert                        
        distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());
        front.sort(new CrowdingComparator());
        for (int k = 0; k < remain; k++) {
          population.add(front.get(k));
        } // for

        remain = 0;
      } // if                               

      // This piece of code shows how to use the indicator object into the code
      // of NSGA-II. In particular, it finds the number of evaluations required
      // by the algorithm to obtain a Pareto front with a hypervolume higher
      // than the hypervolume of the true Pareto front.
      /*if ((indicators != null) &&
          (requiredEvaluations == 0)) {
        double HV = indicators.getHypervolume(population);
        if (HV >= (0.98 * indicators.getTrueParetoFrontHypervolume())) {
          requiredEvaluations = evaluations;
        } // if
      } // if*/
    } // while

    // Return as output parameter the required evaluations
    setOutputParameter("evaluations", requiredEvaluations);

    // Return the first non-dominated front
    Ranking ranking = new Ranking(population);
    ranking.getSubfront(0).printFeasibleFUN("FUN_NSGAII") ;

    return ranking.getSubfront(0);
  } // execute
  /*
	 * Funcion para obtener una cantidad de Individuos para la población
	 * Inicial, cuya Solicitud es la unica seteada hasta el momento.
	 */
	private Set<Individuo> obtenerPrueba(int cantidad) {
	
		Set<Individuo> individuos = new HashSet<Individuo>(cantidad);
		Caso prueba1 = em.find(Caso.class, casoPrincipal);
		if (prueba1 != null){
	
			for (int i = 0; i < cantidad; i++) {
				Solucion solucion = new Solucion(prueba1.getSolicitudes());
		
				individuos.add(solucion);
			}
		}else{
			System.out.println("ERRORRRRR, prueba1 = null");
		}
		
	
		return individuos;
	}//obtenerPrueba
	
	/*
	 * Obtiene la población Inicial a partir de la Prueba cargada.
	 */
	private void obtenerPoblacion(int tamanho) {
	
		// 0. Obtener individuos Iniciales.
		Set<Individuo> individuos = this.obtenerPrueba(tamanho);
	
		// 1. Se crea la Poblacion Inicial con los individuos iniciales.
		population = new Poblacion(individuos);
		// 2. Se carga la Red en la Poblacion.
		Poblacion.setRed(NSFNET);
		// 3. Se generan los caminos de la poblacion inicial.
		population.generarPoblacion(esquema);
		// 4. Se imprime la Poblacion Inicial
		// System.out.println(p.toString());
	}//obtenerPoblacion

	
} // NSGA-II

