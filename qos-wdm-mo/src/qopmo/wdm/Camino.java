package qopmo.wdm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import qopmo.wdm.qop.Exclusividad;
import qopmo.wdm.qop.Servicio;

/**
 * Clase Camino, representa un camino por su nodo origen, y una lista de enlaces
 * que debe seguir.
 * 
 * @author albert
 * 
 */
@Entity
public class Camino {

	public static String BUFFER_DEBUG = "";

	@Id
	@GeneratedValue
	private long id;

	@ManyToOne(cascade = CascadeType.ALL)
	private Nodo origen;

	@ManyToOne(cascade = CascadeType.ALL)
	private Nodo destino;

	@OneToMany(cascade = CascadeType.ALL)
	@OrderBy("secuencia ASC")
	private Set<Salto> saltos;

	private int distancia = 0;

	/**
	 * Constructor vacio.
	 */
	public Camino() {
	}

	/**
	 * Constructor principal.
	 * 
	 * @param origen
	 * @param destino
	 */
	public Camino(Nodo origen) {
		this.origen = origen;
		this.destino = origen;
		this.saltos = new TreeSet<Salto>();
		this.saltos.clear();
		this.distancia = 0;
	}

	/**
	 * Constructor a partir de un camino existente.
	 * 
	 * @param c
	 *            Camino existente
	 */
	public Camino(Camino c) {
		this.origen = c.origen;

		this.saltos = new TreeSet<Salto>();
		this.saltos.addAll(c.saltos);
		this.destino = c.destino;
		this.distancia = c.distancia;
	}

	/**
	 * Constructor que toma un origen un destino y un Salto. Se utiliza en el
	 * Cruce.
	 * 
	 * @param origen
	 * @param destino
	 * @param s
	 */
	public Camino(Nodo origen, Nodo destino, Salto s) {
		this.origen = origen;
		this.destino = destino;
		this.saltos = new TreeSet<Salto>();
		this.saltos.add(s);
		this.distancia = s.getCanal().getCosto();
	}

	/*
	 * GETTERS Y SETTERS
	 */

	public Nodo getDestino() {
		return this.destino;
	}

	public int getDistancia() {
		return this.distancia;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Set<Salto> getSaltos() {
		return saltos;
	}

	public void setSaltos(Set<Salto> saltos) {
		this.saltos = saltos;
	}

	public void setDestino(Nodo destino) {
		this.destino = destino;
	}

	public Nodo getOrigen() {
		return origen;
	}

	public void setOrigen(Nodo origen) {
		this.origen = origen;
	}

	/*
	 * METODOS DE PROCESAMIENTO DEL CAMINO
	 */

	/**
	 * Metodo para agregar un salto al camino. Si saltos está null se instancia.
	 * 
	 * @param salto
	 */
	public void addSalto(Salto salto) {

		if (this.saltos == null)
			this.saltos = new TreeSet<Salto>();

		this.saltos.add(salto);

		if (this.destino == null)
			this.destino = this.origen;

		if (salto != null) {
			this.destino = salto.getCanal().getOtroExtremo(this.destino);
			this.distancia += salto.getCanal().getCosto();
		}
	}

	/**
	 * Bloquear Canales del Camino (sirve para Protección Segment-Oriented)
	 */
	public void bloquearCanales() {
		for (Salto salto : saltos) {
			salto.getCanal().bloquear();
		}
	}

	/**
	 * Desbloquea Canales del Camino (sirve para Protección Segment-Oriented)
	 */
	public void desbloquearCanales() {
		for (Salto salto : saltos) {
			salto.getCanal().desbloquear();
		}
	}

	/**
	 * Bloquea todos los Nodos del Camino (sirve para Protección Path-Oriented).
	 */
	public void bloquearNodos() {
		Nodo actual = this.origen;
		actual.bloquear();

		for (Salto salto : saltos) {
			CanalOptico canal = salto.getCanal();
			actual = canal.getOtroExtremo(actual);
			actual.bloquear();
		}
	}

	/**
	 * Desbloquea todos los Nodos del Camino (Sirve para Protección
	 * Path-Oriented).
	 */
	public void desbloquearNodos() {
		Nodo actual = this.origen;

		actual.desbloquear();
		for (Salto salto : saltos) {
			actual = salto.getCanal().getOtroExtremo(actual);

			actual.desbloquear();
		}
	}

	/**
	 * Desbloquea los Enlaces del Camino para la reserva de enlaces.
	 */
	public void desbloquearEnlaces() {
		for (Salto salto : saltos) {
			Enlace e = salto.getEnlace();
			if (e != null)
				e.desbloquear();
		}
	}

	/**
	 * Concatena el camino actual con el camino que viene de parametro.
	 * 
	 * @param c
	 */
	public void anexar(Camino c) {
		if (!this.destino.equals(c.origen))
			return;

		int secuencia = this.saltos.size() + 1;
		Nodo actual = this.destino;

		for (Salto s : c.saltos) {
			actual = s.getCanal().getOtroExtremo(actual);
			Salto newSalto = new Salto(secuencia++, s.getCanal());
			this.saltos.add(newSalto);
		}

		this.distancia = distancia + c.distancia;
		this.destino = c.destino;
	}

	/**
	 * Función que asigna Enlace para cada salto del Camino.
	 */
	public boolean setEnlaces() {
		int ldo = -1;
		boolean retorno = true;
		for (Salto salto : saltos) {
			ldo = salto.setEnlace(ldo);
			// ldo es -5 cuando no se encontró enlace disponible.
			if (ldo == -5) {
				retorno = false;
				break;
			}
		}
		return retorno;
	}

	/**
	 * Función para bloquear enlaces de cada Salto del Camino. Si un Salto tiene
	 * un enlace cuya fibra no entra en el Canal correspondiente, se agregar una
	 * Fibra Extra.
	 */
	public void fijarEnlaces() {
		for (Salto salto : saltos) {
			Enlace e = salto.getEnlace();

			/*
			 * Si el servicio utiliza una fibra extra
			 */
			if (e.getFibra() >= e.getCanal().getFibras()) {
				salto.getCanal().agregarFibraExtra();

				/*
				 * Se actualiza la referencia al enlace extra.
				 */
				e = e.getCanal().getEnlace(e.getFibra(), e.getLongitudDeOnda());
				salto.setEnlace(e);
			}

			e.bloquear();
		}
	}

	/**
	 * Función para Reservar el enlace deL CanalOptico en cada Salto del Camino.
	 * Si un Salto tiene un enlace cuya fibra no entra en el Canal
	 * correspondiente, se agregar una Fibra Extra.
	 */
	public void fijarReservas(Servicio s) {
		for (Salto salto : saltos) {
			Enlace e = salto.getEnlace();

			/*
			 * Si el servicio utiliza una fibra extra
			 */
			if (e.getFibra() >= e.getCanal().getFibras()) {
				salto.getCanal().agregarFibraExtra();

				/*
				 * Se actualiza la referencia al enlace extra.
				 */
				e = e.getCanal().getEnlace(e.getFibra(), e.getLongitudDeOnda());
				salto.setEnlace(e);
			}

			e.reservar(s);
		}
	}

	/**
	 * Función para Reservar los enlaces de los Saltos del Camino.
	 * 
	 * @param servicio
	 * @param exclusividad
	 */
	public void setReservas(Servicio servicio, Exclusividad exclusividad) {
		int ldo = -1;
		for (Salto salto : saltos) {
			ldo = salto.setReserva(ldo, servicio, exclusividad);
		}
	}

	/**
	 * Función para eliminar Reservas del enlace en cada Salto del Camino.
	 * 
	 * @param s
	 */
	public void eliminarReservas(Servicio s) {
		for (Salto salto : saltos) {
			salto.getEnlace().eliminarReserva(s);
		}
	}

	/**
	 * Obtiene el conjunto de Enlaces del Camino.
	 * 
	 * @return
	 */
	public Set<Enlace> getEnlaces() {
		HashSet<Enlace> enlaces = new HashSet<Enlace>();

		for (Salto s : saltos) {
			enlaces.add(s.getEnlace());
		}

		return enlaces;
	}

	/**
	 * Consulta si se utiliza el camino utiliza el CanalOptico recibido.
	 * 
	 * @param c
	 * @return
	 */
	public boolean usaCanal(CanalOptico c) {
		for (Salto salto : saltos) {
			if (salto.getEnlace().getCanal().equals(c))
				return true;
		}

		return false;
	}

	/**
	 * Obtiene la cantidad de Cambios de Longitud de Onda que ocurre en el
	 * Camino.
	 * 
	 * @return
	 */
	public int getCambiosLDO() {
		int retorno = 0;
		int ldo1 = 0;
		int ldo2 = 0;
		Iterator<Salto> isaltos = this.saltos.iterator();
		// al menos un enlace Salto debe existir.
		if (isaltos.hasNext()) {

			Salto salto = isaltos.next();
			if (salto.getEnlace() == null)
				throw new Error("GetCambiosLDO: Enlace Nulo. ID:"
						+ salto.getId());
			ldo1 = salto.getEnlace().getLongitudDeOnda();
			while (isaltos.hasNext()) {

				ldo2 = isaltos.next().getEnlace().getLongitudDeOnda();
				if (ldo1 != ldo2)
					retorno++;

				ldo1 = ldo2;
			}
		}

		return retorno;
	}

	/**
	 * Función para agregar Fibras en los canales si es que lo necesita.
	 * Dependiendo de la exclusividad recibida.
	 * 
	 * @param exclusividad
	 */
	/*public void agregarFibras(Exclusividad exclusividad) {
		for (Salto s : saltos) {
			CanalOptico c = s.getCanal();

			if (!c.libreSegunExclusividad(exclusividad)) {
				c.agregarFibraExtra();
			}
		}
	}*/

	/**
	 * Función que verifica si el Nodo recibido se encuentra en este Camino.
	 * 
	 * @param nodo
	 * @return
	 */
	public boolean contiene(Nodo nodo) {
		boolean retorno = false;
		for (Salto s : this.saltos) {

			CanalOptico c = s.getCanal();

			Nodo a = c.getExtremoA();
			if (a.equals(nodo)) {
				retorno = true;
				break;
			}

			Nodo b = c.getExtremoB();
			if (b.equals(nodo)) {
				retorno = true;
				break;
			}
		}
		return retorno;
	}

	/**
	 * SobreEscritura de la Función para pasar a String el Camino.
	 */
	@Override
	public String toString() {
		// Origen
		String A = origen.toString();
		String B = "";
		String camino = "";
		Nodo actual = this.origen;

		for (Salto s : saltos) {
			actual = s.getCanal().getOtroExtremo(actual);
			Enlace e = s.getEnlace();
			B = actual.toString();
			// Se concatena el resto del Camino.
			camino += A + "-" + B + e.toString2();
			A = B;
		}

		return camino;
	}

	/**
	 * Función que verifica si el camino solo fue inicializado o no.
	 * 
	 * @return
	 */
	public boolean esVacio() {
		return this.origen == this.destino;
	}
}
