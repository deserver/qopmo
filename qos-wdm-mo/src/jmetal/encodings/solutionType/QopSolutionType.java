package jmetal.encodings.solutionType;

import java.util.Set;

import jmetal.core.Problem;
import qopmo.ag.Individuo;
import jmetal.core.SolutionType;
import jmetal.core.Variable;
import jmetal.encodings.variable.Int;
import qopmo.ag.Solucion;


public class QopSolutionType extends SolutionType {
	
	public QopSolutionType(Problem problem) {
		super(problem) ;
	}
	
	@Override
	public Variable[] createVariables() throws ClassNotFoundException {
		// TODO Auto-generated method stub
		Variable [] variables = new Variable[problem_.getNumberOfVariables()];
		//variables[0] = new Set<Solucion>();

		for (int var = 0; var < problem_.getNumberOfVariables(); var++)
			variables[var] = new Int((int)0, (int)1);
		return variables;
	}
}
