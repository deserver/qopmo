package qopmo.wdm.qop;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import qopmo.wdm.Nodo;
import qopmo.wdm.Red;

@Entity
public class Caso {

	@Id
	private String nombre;

	@ManyToOne
	Red red;

	@OneToMany(cascade = CascadeType.ALL)
	Set<Solicitud> solicitudes;

	public Red getRed() {
		return red;
	}

	public void setRed(Red red) {
		this.red = red;
	}

	public Set<Solicitud> getSolicitudes() {
		return solicitudes;
	}

	public void setSolicitudes(Set<Solicitud> solicitudes) {
		this.solicitudes = solicitudes;
	}

	public Caso() {
	}

	/**
	 * Crea un caso randomico.
	 * 
	 * @param red
	 *            Red
	 * @param cantSolicitudes
	 *            Cantidad de solicitudes que se generaran
	 * @param probNiveles
	 *            Probabilidades de que una solicitud tenga un nivel de calidad
	 *            dado(Oro=0,Plata=1,Bronce=2). Se asume que las probabilidades
	 *            suman 1.
	 */
	public Caso(Red red, int cantSolicitudes, double[] probNiveles) {
		this.red = red;

		solicitudes = new HashSet<Solicitud>();

		double marcaOro = probNiveles[0];
		double marcaPlata = marcaOro + probNiveles[1];

		for (int i = 0; i < cantSolicitudes; i++) {
			Solicitud s = null;

			do {
				Nodo origen = red.randomNodo();
				Nodo destino = red.randomNodo();
				Nivel nivel = null;

				while (origen.equals(destino))
					destino = red.randomNodo();

				double ruleta = Math.random();

				if (ruleta <= marcaOro) {
					nivel = Nivel.Oro;
				} else if (ruleta <= marcaPlata) {
					nivel = Nivel.Plata1;
				} else {
					nivel = Nivel.Bronce;
				}

				s = new Solicitud(origen, destino, nivel);

			} while (solicitudes.contains(s));

			solicitudes.add(s);
		}
	}

	public Caso(Red red, String nombre) {
		this.red = red;
		this.nombre = nombre;
	}

	@Override
	public String toString() {
		return nombre;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

}
