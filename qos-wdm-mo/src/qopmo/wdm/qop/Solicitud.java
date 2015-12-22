package qopmo.wdm.qop;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import qopmo.wdm.Nodo;

@Entity
public class Solicitud implements Comparable<Solicitud> {

	@Id
	@GeneratedValue
	private long id;

	@ManyToOne(cascade = CascadeType.ALL)
	private Nodo origen;

	@ManyToOne(cascade = CascadeType.ALL)
	private Nodo destino;

	private Nivel nivel;

	private EsquemaRestauracion esquema = EsquemaRestauracion.FullPath;

	public Solicitud() {
	}

	/**
	 * Constructor principal
	 * 
	 * @param origen
	 *            Nodo Origen
	 * @param destino
	 *            Nodo Destino
	 * @param nivel
	 *            Nivel de Calidad de Proteccion solicitada.
	 */
	public Solicitud(Nodo origen, Nodo destino, Nivel nivel) {
		this.origen = origen;
		this.destino = destino;
		this.nivel = nivel;
		this.esquema = EsquemaRestauracion.FullPath;
	}

	/*
	 * GETTERS Y SETTERS
	 */

	public Nodo getOrigen() {
		return origen;
	}

	public Nodo getDestino() {
		return destino;
	}

	public Nivel getNivel() {
		return nivel;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setOrigen(Nodo origen) {
		this.origen = origen;
	}

	public void setDestino(Nodo destino) {
		this.destino = destino;
	}

	public void setNivel(Nivel nivel) {
		this.nivel = nivel;
	}

	public EsquemaRestauracion getEsquema() {
		return esquema;
	}

	public void setEsquema(EsquemaRestauracion esquema) {
		this.esquema = esquema;
	}

	/*
	 * FUNCIONES AUXILIARES
	 */

	/**
	 * Función para Obtener La Exclusividad para Primario en función al Nivel.
	 * 
	 * @return
	 */
	public Exclusividad getExclusividadPrimario() {
		if (nivel == Nivel.Oro || nivel == Nivel.Plata1)
			return Exclusividad.Exclusivo;

		return Exclusividad.SinReservasBronce;
	}

	/**
	 * Función para Obtener La Exclusividad para Alternativo en función al
	 * Nivel.
	 * 
	 * @return
	 */
	public Exclusividad getExclusividadAlternativo() {
		if (nivel == Nivel.Oro)
			return Exclusividad.Exclusivo;

		return Exclusividad.NoExclusivo;
	}

	public boolean igualesDatos(Object obj) {
		if (!(obj instanceof Solicitud))
			return false;

		Solicitud solicitud = (Solicitud) obj;
		boolean respuesta = ((this.id == solicitud.id)
				&& this.origen.equals(solicitud.origen) && this.destino
				.equals(solicitud.destino));
		respuesta = respuesta && this.nivel.equals(solicitud.nivel);
		return respuesta;
	}

	@Override
	public int hashCode() {
		return (int) this.id;
	}

	@Override
	public boolean equals(Object obj) {
		// System.err.println("Solicitud: "+this.id+"-"+this);
		if (!(obj instanceof Solicitud))
			return false;

		Solicitud solicitud = (Solicitud) obj;
		boolean respuesta = ((this.id == solicitud.id));
		return respuesta;
	}

	@Override
	public int compareTo(Solicitud s) {
		int cmpOrd = (int) (this.id - s.id);
		return cmpOrd;
	}

	@Override
	public String toString() {
		return "ID:"+(id-1086)+"(De:" + origen + " A:" + destino + " Nivel:" + nivel + ")";
	}
}
