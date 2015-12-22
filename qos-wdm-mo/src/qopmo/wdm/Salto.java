package qopmo.wdm;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import qopmo.wdm.qop.Exclusividad;
import qopmo.wdm.qop.Servicio;

@Entity
public class Salto implements Comparable<Salto> {

	@Transient
	private CanalOptico canal;

	@ManyToOne(cascade = CascadeType.ALL)
	private Enlace enlace;

	private int secuencia;

	@Id
	@GeneratedValue
	private long id;

	public Salto() {
	}

	public Salto(int secuencia, CanalOptico c) {
		this.secuencia = secuencia;
		this.canal = c;
	}

	/*
	 * GETTERS Y SETTERS
	 */
	public int getSecuencia() {
		return secuencia;
	}

	public void setSecuencia(int secuencia) {
		this.secuencia = secuencia;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public CanalOptico getCanal() {
		return canal;
	}

	public void setCanal(CanalOptico c) {
		this.canal = c;
		this.enlace = null;
	}

	public Enlace getEnlace() {
		return enlace;
	}

	public void setEnlace(Enlace enlace) {
		this.enlace = enlace;
	}

	/*
	 * FUNCIONES AUXILIARES
	 */

	/**
	 * Función para obtener y bloquear un enlace específico del canal
	 * relacionado.
	 * 
	 * @param ldO
	 * @return
	 */
	public int setEnlace(int ldO) {
		if (ldO < 0)
			this.enlace = canal.getEnlaceLibre(Exclusividad.Exclusivo);
		else
			this.enlace = canal.getEnlaceLibre(Exclusividad.Exclusivo, ldO);

		// En el caso que no existan mas recursos disponibles el enlace es null.
		int retorno = -5;
		if (this.enlace != null) {
			enlace.bloquear();
			retorno = enlace.getLongitudDeOnda();
		}

		return retorno;
	}

	/**
	 * Función para obtener y reservar un enlace específico del canal
	 * relacionado.
	 * 
	 * @param ldO
	 * @param servicio
	 * @param exclusividad
	 * @return
	 */
	public int setReserva(int ldO, Servicio servicio, Exclusividad exclusividad) {
		if (ldO < 0)
			this.enlace = canal.getEnlaceLibre(exclusividad);
		else
			this.enlace = canal.getEnlaceLibre(exclusividad, ldO);

		enlace.reservar(servicio);

		return enlace.getLongitudDeOnda();
	}

	/**
	 * Función para comparar el orden de 2 Saltos. Se controla que este salto
	 * sea menor, igual o mayor que el Salto b recibido como parametro.
	 */
	@Override
	public int compareTo(Salto b) {
		return this.secuencia - b.secuencia;
	}

	/**
	 * Se utiliza de hashCode la Secuencia.
	 */
	@Override
	public int hashCode() {
		return secuencia;
	}

	/**
	 * Se tiene un toString personalizado. Se muestra: id, secuencia, canal y
	 * enlace.
	 */
	@Override
	public String toString() {
		return "Salto [id=" + id + ", secuencia=" + secuencia + ", canal="
				+ canal + ", enlace=" + enlace + "]";
	}
}
