package qopmo.wdm;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import qopmo.wdm.qop.Exclusividad;
import qopmo.wdm.qop.Nivel;
import qopmo.wdm.qop.Servicio;

@Entity
@Table(name = "CanalOptico")
/**
 * Clase que representa los canales opticos que forman parte de la red.
 * <p>
 * Descripcion: Canal optico utilizado para representar la agrupacion de enlaces
 * que estan definidos por longitud de onda, por fibra, y por canal.
 * </p>
 * 
 * @author aamadeo
 * 
 */
public class CanalOptico implements Comparable<CanalOptico> {

	@OneToMany(cascade = CascadeType.ALL)
	@OrderBy("longitudDeOnda ASC")
	private Set<Enlace> enlaces = new HashSet<Enlace>();

	@Transient
	private Set<Enlace> enlacesNecesarios = new HashSet<Enlace>();

	@Id
	@GeneratedValue
	private int id;

	private int fibras;

	@Transient
	int fibrasExtra = 0;

	private int ldos;

	private int costo;

	@ManyToOne(cascade = CascadeType.ALL)
	private Nodo extremoA;

	@ManyToOne(cascade = CascadeType.ALL)
	private Nodo extremoB;

	@Transient
	private boolean bloqueado = false;

	public CanalOptico() {
	}

	/**
	 * Constructor principal. Setea los atributos principales y genera los
	 * enlaces del canal.
	 * 
	 * @param origen
	 *            Nodo Origen
	 * @param destino
	 *            Nodo Destino
	 * @param fibras
	 *            Cantidad de fibras en el canal
	 * @param ldos
	 *            Cantidad de Longitudes de Onda por fibra
	 */
	public CanalOptico(Nodo a, Nodo b, int fibras, int ldos) {
		this.extremoA = a;
		this.extremoB = b;
		this.fibras = fibras;
		this.fibrasExtra = 0;
		this.ldos = ldos;

		this.enlaces.clear();
		crearEnlaces();
	}

	/*
	 * Auxiliar para el Constructor, para cargar todos los enlaces.
	 */
	private void crearEnlaces() {
		for (int i = 0; i < fibras; i++) {
			for (int j = 0; j < ldos; j++) {
				this.enlaces.add(new Enlace(i, j, this));
			}
		}
	}

	/*
	 * GETTERS Y SETTERS.
	 */

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getFibrasExtra() {
		return this.fibrasExtra;
	}

	public int getFibras() {
		return fibras;
	}

	public void setFibras(int fibras) {
		this.fibras = fibras;
	}

	public int getLdos() {
		return ldos;
	}

	public void setLdos(int ldos) {
		this.ldos = ldos;
	}

	public Nodo getExtremoA() {
		return this.extremoA;
	}

	public void setOrigen(Nodo a) {
		this.extremoA = a;
	}

	public Nodo getExtremoB() {
		return extremoB;
	}

	public void setDestino(Nodo b) {
		this.extremoB = b;
	}

	public boolean estaBloqueado() {
		return this.bloqueado;
	}

	public int getCosto() {
		return costo;
	}

	public void setCosto(int costo) {
		this.costo = costo;
	}

	/**
	 * Bloquear el enlace porque forma parte del camino primario de algun
	 * Servicio. Corresponde al setter de bloqueado.
	 */
	public void bloquear() {
		this.bloqueado = true;
	}

	/**
	 * DesBloquear el enlace porque dejo de formar parte del camino primario de
	 * algun Servicio. Corresponde al setter de bloqueado.
	 */
	public void desbloquear() {
		this.bloqueado = false;
	}

	public Set<Enlace> getEnlaces() {
		return enlaces;
	}

	public void setEnlaces(Set<Enlace> enlaces) {
		this.enlaces = enlaces;
		this.enlacesNecesarios.clear();
		this.enlacesNecesarios.addAll(enlaces);
	}

	/*
	 * FUNCIONES AUXILIARES.
	 */

	/**
	 * Inicializa los valores del canal, en caso de que algun enlace este
	 * bloqueado, reservado.
	 */
	public void inicializar() {
		this.desbloquear();
		this.fibrasExtra = 0;
		this.enlacesNecesarios.clear();
		this.enlacesNecesarios.addAll(enlaces);

		for (Enlace e : enlaces) {
			e.inicializar();
		}
	}

	/*
	 * METODOS DE SIMULACIÓN.
	 */

	/**
	 * Simula una falla, echando cada enlace y notificando en cada servicio.
	 */
	public void echarCanal() {
		for (Enlace e : enlaces) {
			e.echar();
		}
	}

	/**
	 * Agrega Fibra para satisfacer las demandas si hubiere necesidad.
	 */
	public void agregarFibraExtra() {
		//this.fibrasExtra++;

		for (int i = 0; i < ldos; i++) {
			enlacesNecesarios.add(new Enlace(fibras + fibrasExtra, i, this));
		}
	}

	/**
	 * Función que obtiene el otro extremo de Canal.
	 * 
	 * @param a
	 * @return
	 */
	public Nodo getOtroExtremo(Nodo a) {
		if (!a.equals(extremoA) && !a.equals(extremoB))
			return null;

		if (a.equals(extremoA))
			return extremoB;

		return extremoA;
	}

	/**
	 * Obtiene aleatoriamente un enlace disponible del conjunto de
	 * enlacesNecesarios.
	 * 
	 * @param exclusividad
	 * @return
	 */
	public Enlace getEnlaceLibre(Exclusividad exclusividad) {
		if (bloqueado)
			return null;

		Enlace[] disponibles = new Enlace[enlacesNecesarios.size()];
		int i = 0;
		for (Enlace e : this.enlacesNecesarios) {
			if (!e.isBloqueado()) {
				if (e.cumpleExclusividad(exclusividad)) {
					disponibles[i++] = e;
				}
			}
		}

		int sorteado = (int) (Math.random() * ((double) i));

		return disponibles[sorteado];
	}

	/**
	 * Obtiene un enlace para una longitud de onda específica (ldo) y si no hay
	 * disponible, se busca aleatoriamente un enlace disponible del conjunto de
	 * enlacesNecesarios.
	 * 
	 * @param exclusividad
	 * @param ldO
	 * @return
	 */
	public Enlace getEnlaceLibre(Exclusividad exclusividad, int ldO) {
		if (bloqueado)
			return null;

		for (Enlace e : enlacesNecesarios) {
			if (!e.isBloqueado()) {
				if (e.getLongitudDeOnda() == ldO) {
					if (e.cumpleExclusividad(exclusividad)) {
						return e;
					}
				}
			}
		}

		return getEnlaceLibre(exclusividad);
	}

	/**
	 * Obtiene un enlace especifico a una fibra y longitud de onda.
	 * 
	 * @param fibra
	 * @param ldo
	 * @return
	 */
	public Enlace getEnlace(int fibra, int ldo) {
		for (Enlace e : enlacesNecesarios) {
			if (e.getFibra() == fibra && e.getLongitudDeOnda() == ldo)
				return e;
		}

		return null;
	}

	/**
	 * Función para calcular el porcentaje de utilización de los enlaces
	 * relacionadosa este canal.
	 * 
	 * @return
	 */
	public int getUso() {
		double total = 0;
		double utilizados = 0;
		for (Enlace e : enlacesNecesarios) {
			total += 1;
			if (e.isBloqueado())
				utilizados += 1;
		}

		return (int) (100.0 * utilizados / total);
	}

	/**
	 * Controla si el canal tiene al menos un enlace para ser utilizados
	 * exclusivamente.
	 * 
	 * @return
	 */
	public boolean tieneEnlacesExclusivos() {
		for (Enlace e : enlacesNecesarios) {
			if (!e.estaReservado() && !e.isBloqueado()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Se verifica el canal tenga un enlace libre según la exclusividad buscada.
	 * 
	 * @param exclusividad
	 * @return
	 */
	public boolean libreSegunExclusividad(Exclusividad exclusividad) {

		if (Exclusividad.Exclusivo == exclusividad)
			return tieneEnlacesExclusivos();

		if (Exclusividad.SinReservasBronce == exclusividad) {
			for (Enlace e : enlacesNecesarios) {
				if (!e.isBloqueado()) {
					boolean tieneReservasBronce = false;

					for (Servicio s : e.getReservas()) {
						if (s.getSolicitud().getNivel() == Nivel.Bronce) {
							tieneReservasBronce = true;
							break;
						}
					}
					if (!tieneReservasBronce)
						return true;
				}
			}
			return false;
		}

		/* Exclusividad : NoExclusivo (comentario aamadeo) */
		for (Enlace e : enlacesNecesarios) {
			if (!e.isBloqueado())
				return true;
		}
		return false;
	}

	/**
	 * Función personalizada de compareTo se realiza compareTo de Nodos
	 * extremos: extremoA y extremoB.
	 */
	@Override
	public int compareTo(CanalOptico arg0) {
		int cmpOrigen1 = extremoA.compareTo(arg0.extremoA);
		int cmpDestino1 = extremoB.compareTo(arg0.extremoB);
		int cmpOrigen2 = extremoB.compareTo(arg0.extremoA);
		int cmpDestino2 = extremoA.compareTo(arg0.extremoB);
		//System.err.println("C-canal: " + this + " -obj: " + arg0);
		if (cmpOrigen1 == 0 && cmpDestino1 == 0)
			return cmpOrigen1;
		else if (cmpOrigen2 == 0 && cmpDestino2 == 0) {
			return cmpOrigen2;
		} else {
			if (cmpOrigen1 != 0)
				return cmpOrigen1;
			else {
				// cmpDestino1 != 0
				return cmpDestino1;
			}
		}
	}

	/**
	 * Funcion personalizada de equals.
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CanalOptico))
			return false;

		CanalOptico b = (CanalOptico) obj;
		boolean opuesto = this.extremoA.equals(b.extremoB)
				&& this.extremoB.equals(b.extremoA);

		boolean retorno = this.extremoA.equals(b.extremoA)
				&& this.extremoB.equals(b.extremoB) || opuesto;

		return retorno;
	}

	/**
	 * Funcion personalizada de toString. Tiene: extremoA y extremoB
	 */
	@Override
	public String toString() {
		return extremoA + "-" + extremoB;
	}
}
