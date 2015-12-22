package qopmo.ag.operadores;

import qopmo.ag.Individuo;

/**
 * Interfaz que define la operación de mutación.
 * <p>
 * OBSERVACION: No se utiliza un operador de mutación, debido a las
 * características aleatorias del cruce implementado.
 * </p>
 * 
 * @author mrodas
 * 
 */
public interface OperadorMutacion {
	/**
	 * Realiza una operaci�n de mutación sobre un Individuo dado.
	 * 
	 * @param i
	 *            El Individuo a ser mutado
	 */
	public void mutar(Individuo i);
}
