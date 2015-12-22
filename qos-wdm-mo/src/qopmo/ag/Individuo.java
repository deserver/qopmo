package qopmo.ag;

/**
 * Interface del Individuo que define las operaciones propias del individuo.
 * 
 * @author mrodas
 * 
 */
public interface Individuo {

	/**
	 * Función para calcular el Fitness del Individuo.
	 * 
	 * @return fitness del Individuo
	 */
	public double evaluar();
	
	/**
	 * Funcion para comparar 2 individuos e indicar el mejor.
	 * @return
	 */
	public boolean comparar(Individuo i);

}
