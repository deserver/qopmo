package qopmo.ag.operadores;

import java.util.Collection;

import qopmo.ag.Individuo;
import qopmo.ag.Poblacion;

/**
 * Interfaz que define la operación de selección.
 * 
 * @author mrodas
 * 
 */
public interface OperadorSeleccion {
	/**
	 * Realiza la operación de selección sobre una población de individuos.
	 * 
	 * @param poblacion
	 *            La población sobre la cual realizar la selección
	 * @return Individuos seleccionados
	 */
	public Collection<Individuo> seleccionar(Poblacion poblacion);
}
