package qopmo.wdm;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import qopmo.wdm.qop.Exclusividad;
import qopmo.wdm.qop.Nivel;
import qopmo.wdm.qop.Servicio;

/**
 * Clase que representa el enlace de un canal Optico.
 * <p>
 * Descripcion: Enlace cuyos componentes son: longitud de Onda
 * </p>
 * 
 * @author aamadeo
 * 
 */

@Entity
public class Enlace {

	@Id
	@GeneratedValue
	private long id;

	private int longitudDeOnda = -1;
	private int fibra = -1;

	@ManyToOne(cascade = CascadeType.ALL)
	private CanalOptico canal;

	// Propiedad de Simulacion
	@Transient
	private Servicio servicio;

	@Transient
	private Set<Servicio> reservas = new HashSet<Servicio>();

	@Transient
	private boolean bloqueado = false;

	@Transient
	private boolean disponible = true;

	public Enlace() {
	}

	/**
	 * Constructor principal
	 * 
	 * @param ldo
	 *            Longitud de Onda
	 * @param fibra
	 *            Identificador de Fibra
	 * @param canal
	 *            Canal Optico que contiene al enlace
	 */
	public Enlace(int fibra, int ldo, CanalOptico canal) {
		this.longitudDeOnda = ldo;
		this.fibra = fibra;
		this.canal = canal;
	}

	/*
	 * GETTERS Y SETTERS
	 */

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getLongitudDeOnda() {
		return this.longitudDeOnda;
	}

	public void setLongitudDeOnda(int longitudDeOnda) {
		this.longitudDeOnda = longitudDeOnda;
	}

	public int getFibra() {
		return fibra;
	}

	public void setFibra(int fibra) {
		this.fibra = fibra;
	}

	public CanalOptico getCanal() {
		return canal;
	}

	public void setCanal(CanalOptico canal) {
		this.canal = canal;
	}

	public Set<Servicio> getReservas() {
		return reservas;
	}

	public void setReservas(Set<Servicio> reservas) {
		this.reservas = reservas;
	}

	public boolean isBloqueado() {
		return bloqueado;
	}

	public void bloquear() {
		this.bloqueado = true;
	}

	public void desbloquear() {
		this.bloqueado = false;
	}

	/*
	 * FUNCIONES AUXILIARES.
	 */

	/**
	 * Restablece los valores iniciales del enlace
	 */
	public void inicializar() {
		this.bloqueado = false;
		this.disponible = true;
		this.reservas.clear();
		this.servicio = null;
	}

	/**
	 * Retorna el nodo origen del canal optico.
	 * 
	 * @return Nodo Origen
	 */
	public Nodo getExtremoA() {
		return canal.getExtremoA();
	}

	/**
	 * Retorna el nodo destino del canal optico.
	 * 
	 * @return Nodo destino.
	 */
	public Nodo getExtremoB() {
		return canal.getExtremoB();
	}

	/**
	 * Reserva el canal optico como parte de un segmento alternativo
	 * 
	 * @param servicio
	 *            Servicio que utilizara el enlace en su segmento alternativo
	 */
	public void reservar(Servicio servicio) {
		if (!reservas.contains(servicio)) {
			reservas.add(servicio);
		}
	}

	/**
	 * Retorna true si el enlace tiene al menos una reserva.
	 * 
	 * @return
	 */
	public boolean estaReservado() {
		return (reservas.size() > 0);
	}

	/**
	 * Elimina una reserva especifica
	 * 
	 * @param servicio
	 *            Servicio que reservo el enlace.
	 */
	public void eliminarReserva(Servicio servicio) {
		reservas.remove(servicio);
	}

	/**
	 * Elminar todas las reservas.
	 */
	public void eliminarReservas() {
		reservas.clear();
	}

	/**
	 * Función que trae el otro extremo (nodo) del Canal al que está vinculado.
	 * 
	 * @param a
	 * @return
	 */
	public Nodo getOtroExtremo(Nodo a) {
		return canal.getOtroExtremo(a);
	}

	/**
	 * Se verifica si puede ser utilizado debido a la exclusividad requerida.
	 * 
	 * @param exclusividad
	 * @return
	 */
	public boolean cumpleExclusividad(Exclusividad exclusividad) {
		if (exclusividad == Exclusividad.Exclusivo)
			return (reservas.size() == 0);
		if (exclusividad == Exclusividad.SinReservasBronce) {
			for (Servicio s : reservas) {
				if (s.getSolicitud().getNivel() == Nivel.Bronce)
					return false;
			}
		}
		return true;
	}

	/**
	 * Función personalizada de toString. Incluye: id, longitud de onda, fibra,
	 * canal.
	 */
	@Override
	public String toString() {
		return "Enlace [id=" + id + ", longitudDeOnda=" + longitudDeOnda
				+ ", fibra=" + fibra + ", canal=" + canal + "]";
	}

	public String toString2() {
		return "[LdO=" + longitudDeOnda + ",F=" + fibra + "]";
	}

	/**
	 * Función personalizada de equals.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Enlace other = (Enlace) obj;
		if (canal == null) {
			if (other.canal != null)
				return false;
		} else if (!canal.equals(other.canal))
			return false;
		return true;
	}

	/*
	 * FUNCIONES DE SIMULACIÓN
	 */

	/**
	 * Funcion de simulación, que provee conexion al servicio en cuestion.
	 * 
	 * @param servicio
	 *            Servicio que desea utilizar el enlace.
	 */
	public void utilizar(Servicio servicio) {
		this.servicio = servicio;
	}

	/**
	 * Funcion de simulacion, que libera de uso al enlace.
	 */
	public void liberar() {
		this.servicio = null;
	}

	/**
	 * Funcion de simulacion, que retorna true si el enlace esta siendo
	 * utilizado.
	 * 
	 * @return
	 */
	public boolean estaLibre() {
		return this.servicio != null;
	}

	/**
	 * Funcion de simulacion, que interrumpe la conectividad del enlace.
	 * Notifica al servicio que lo utilizaba, si hubiera alguno.
	 */
	public void echar() {
		this.disponible = false;

		if (this.servicio == null)
			return;

		this.servicio.setDisponible(false);
	}

	/**
	 * Funcion de simulacion, Restablece el servicio del enlace.
	 */
	public void restablecer() {
		this.disponible = true;
	}

	/**
	 * Funcion de simulacion, retorna true si el enlace esta disponible
	 * 
	 * @return Estado del enlace
	 */
	public boolean estaDisponible() {
		return this.disponible;
	}

}
