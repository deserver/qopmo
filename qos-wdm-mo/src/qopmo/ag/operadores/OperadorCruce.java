package qopmo.ag.operadores;

import qopmo.ag.Individuo;
import jmetal.core.Solution;

/**
 * Interfaz OperadorCruce que define la implementación de una operación de
 * cruce.
 * 
 * @author mrodas
 * 
 */
public interface OperadorCruce {
	/**
	 * Realiza una operación de cruce entre dos Individuos, produciendo un
	 * conjunto de Individuos hijos.
	 * 
	 * @param primero
	 *            Primer Individuo a cruzar
	 * @param segundo
	 *            Segundo Individuo a cruzar
	 * @return Individuos hijos
	 */
	public Individuo cruzar(Individuo primero, Individuo segundo);

}
