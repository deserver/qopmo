//  NSGAII_main.java
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

package qopmo.nsgaII.testLink;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import jmetal.util.Configuration;
import jmetal.util.JMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import qopmo.nsgaII.QOP;
import qopmo.ag.operadores.impl.*;
import qopmo.ag.Poblacion;

/** 
 * Class to configure and execute the NSGA-II algorithm.  
 *     
 * Besides the classic NSGA-II, a steady-state version (ssNSGAII) is also
 * included (See: J.J. Durillo, A.J. Nebro, F. Luna and E. Alba 
 *                  "On the Effect of the Steady-State Selection Scheme in 
 *                  Multi-Objective Genetic Algorithms"
 *                  5th International Conference, EMO 2009, pp: 183-197. 
 *                  April 2009)
 */ 
@RunWith(Parameterized.class)
public class NSGAII_main_testLink {
  public static Logger      logger_ ;      // Logger object
  public static FileHandler fileHandler_ ; // FileHandler object


  /**
   * @param args Command line arguments.
   * @throws JMException 
   * @throws IOException 
   * @throws SecurityException 
   * Usage: three options
   *      - jmetal.metaheuristics.nsgaII.NSGAII_main
   *      - jmetal.metaheuristics.nsgaII.NSGAII_main problemName
   *      - jmetal.metaheuristics.nsgaII.NSGAII_main problemName paretoFrontFile
   */
  public static void main(String [] args) throws 
                                  JMException, 
                                  SecurityException, 
                                  IOException, 
                                  ClassNotFoundException {
	  
	    Problem   problem   ; // The problem to solve
	    Algorithm algorithm ; // The algorithm to use
	    Operator  crossover ; // Crossover operator
	    Operator  mutation  ; // Mutation operator
	    Operator  selection ; // Selection operator
	    
	    HashMap  parameters ; // Operator parameters
	    
	
	    // Logger object and file to store log messages
	    logger_      = Configuration.logger_ ;
	    fileHandler_ = new FileHandler("NSGAII_main.log"); 
	    logger_.addHandler(fileHandler_) ;
	        
	    problem = new QOP();
	    int corridas, caso;
	    corridas =0;
	    while(corridas < 40){
		    algorithm = new NSGAII_testLink(problem, corridas);
		    caso = getCaso(corridas);
		    //algorithm = new ssNSGAII(problem);
		
		    // Algorithm parameters
		    algorithm.setInputParameter("populationSize",50);
		    algorithm.setInputParameter("maxEvaluations",50);
		
		    // Mutation and Crossover for Real codification 
		    /*parameters = new HashMap() ;
		    parameters.put("probability", 0.9) ;
		    parameters.put("distributionIndex", 20.0) ;
		    crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover", parameters);                   
		
		    parameters = new HashMap() ;
		    parameters.put("probability", 1.0/problem.getNumberOfVariables()) ;
		    parameters.put("distributionIndex", 20.0) ;
		    mutation = MutationFactory.getMutationOperator("PolynomialMutation", parameters);                    
		
		    // Selection Operator 
		    parameters = null ;
		    selection = SelectionFactory.getSelectionOperator("BinaryTournament2", parameters) ;  
		    */
		    
		    // Add the operators to the algorithm
		    /*algorithm.addOperator("crossover",crossover);
		    algorithm.addOperator("mutation",mutation);
		    algorithm.addOperator("selection",selection);*/
		    //algorithm.addOperator("torneobinario", torneobinario);
		
		    // Add the indicator object to the algorithm
		    
		    System.out.println("Corrida: "+corridas);
		    // Execute the Algorithm
		    long initTime = System.currentTimeMillis();
		    Poblacion population = algorithm.execute();
		    long estimatedTime = System.currentTimeMillis() - initTime;
		    
		    System.out.println("FIN Prueba Algoritmo Genetico. (Link-Oriented)");
		    // Result messages 
		    logger_.info("Total execution time: "+estimatedTime + "ms");
		    logger_.info("Variables values have been writen to file VAR");
		    population.printVariablesToFile("VAR_"+caso);    
		    logger_.info("Objectives values have been writen to file FUN");
		    population.printObjectivesToFile("FUN_"+caso);
		  	
		    /*if (indicators != null) {
		      logger_.info("Quality indicators") ;
		      logger_.info("Hypervolume: " + indicators.getHypervolume(population)) ;
		      logger_.info("GD         : " + indicators.getGD(population)) ;
		      logger_.info("IGD        : " + indicators.getIGD(population)) ;
		      logger_.info("Spread     : " + indicators.getSpread(population)) ;
		      logger_.info("Epsilon    : " + indicators.getEpsilon(population)) ;  
		     
		      int evaluations = ((Integer)algorithm.getOutputParameter("evaluations")).intValue();
		      logger_.info("Speed      : " + evaluations + " evaluations") ;      
		    } // if*/
		    corridas++;
	  }
	  
  } //main
  
  public static int getCaso(int x){
		int num = x, caso;
		if (num <= 10) {
			caso = 40;
		} else if (num <= 20) {
			caso = 30;
		} else if (num <= 30) {
			caso = 20;
		} else if (num <= 40) {
			caso = 10;
		} else {
			caso = 1;
		}
		return caso;
  }
} // NSGAII_main
