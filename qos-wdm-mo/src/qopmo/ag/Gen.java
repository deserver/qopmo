package qopmo.ag;

import java.util.ArrayList;

import qopmo.wdm.CanalOptico;

/**
 * Clase Gen que representa la solución particular para una Solicitud realizada.
 * <p>
 * Consiste en un canal primario + un canal secundario.
 * </p>
 * 
 * @author mrodas
 * 
 */
@Deprecated
public class Gen {

	/*
	 * Conjunto de Canales Ópticos primarios que representan la solución para
	 * una Solicitud realizada.
	 */
	private ArrayList<CanalOptico> primario;

	/*
	 * Canal Óptico de respaldo que es posible solución para una Solicitud
	 * realizada.
	 */
	private ArrayList<CanalOptico> secundario;

	public Gen() {
		this.primario = null;
		this.secundario = null;
	}

	public Gen(ArrayList<CanalOptico> primario,
			ArrayList<CanalOptico> secundario) {
		super();
		this.primario = primario;
		this.secundario = secundario;
	}

	/**
	 * @return Canal Óptico Primario.
	 */
	public ArrayList<CanalOptico> getPrimario() {
		return primario;
	}

	/**
	 * @param primario
	 *            Canal Óptico Primario.
	 */
	public void setPrimario(ArrayList<CanalOptico> primario) {
		this.primario = primario;
	}

	/**
	 * @return Canal Óptico Secundario.
	 */
	public ArrayList<CanalOptico> getSecundario() {
		return secundario;
	}

	/**
	 * @param secundario
	 *            Canal Óptico Secundario.
	 */
	public void setSecundario(ArrayList<CanalOptico> secundario) {
		this.secundario = secundario;
	}

	static int VALOR_DISTANCIA = 1;
	static int VALOR_CAMBIO_LDO = 1;

	/**
	 * Calcula el Costo total del Gen
	 * 
	 * @return Costo total del Gen
	 */
	public double getCosto() {
		double total = 0.0;
		boolean primero = true;
		int longitudOnda = 0;
		for (CanalOptico canalOptico : this.primario) {
			if (primero) {
				longitudOnda = canalOptico.getLdos();
				primero = false;
			} else {
				if (longitudOnda > 0) {
					// Si hay cambio en la longitud de onda aumenta el Costo
				}
			}

			// total_1 = distancia total del Canal Optico * VALOR_DISTANCIA
			// + Cantidad de cambios de longitud de onda * VALOR_CAMBIO_LDO
			total += canalOptico.getCosto() * VALOR_DISTANCIA;
		}
		return total;
	}

}
