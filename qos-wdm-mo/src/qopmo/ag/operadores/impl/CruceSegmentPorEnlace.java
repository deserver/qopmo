package qopmo.ag.operadores.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import qopmo.wdm.Camino;
import qopmo.wdm.Enlace;
import qopmo.wdm.Nodo;
import qopmo.wdm.Salto;
import qopmo.wdm.qop.Exclusividad;
import qopmo.wdm.qop.Nivel;
import qopmo.wdm.qop.Servicio;
import qopmo.ag.Individuo;
import qopmo.ag.Poblacion;
import qopmo.ag.Solucion;
import qopmo.ag.operadores.OperadorCruce;
import jmetal.core.Solution;

/**
 * Implementación específica del Cruce mencionado en paper de cnunez.
 * <p>
 * Este cruce se define asi: dada dos soluciones de entrada (s1 y s2), en una
 * primera parte (P1) se copian los Caminos cuyos genes son iguales entre los
 * genes de los padres.
 * </p>
 * <p>
 * Luego, en una segunda parte (P2), se utiliza el algoritmo SPD para completar
 * los caminos faltantes.
 * </p>
 * <p>
 * Finalmente, en una tercera parte (P3) se asignan las longitudes de onda, la
 * primera de forma aleatoria, y luego se trata de mantener dicha longitud de
 * onda. En el caso que no se encuentra disponible la longitud de onda se
 * procederá a elegir otro randomicamente. Así sucesivamente hasta llegar al
 * final del camino.
 * </p>
 */
public class CruceSegmentPorEnlace implements OperadorCruce {

	@Override
	public Individuo cruzar(Individuo i1, Individuo i2) {
		Solution s1 = (Solution) i1;
		Solution s2 = (Solution) i2;

		// System.out.println("---------------------------");
		// System.out.println("@Padre1@" + s1 + "@Padre1@.");
		// System.out.println("@Padre2@" + s2 + "@Padre2@.");

		Collection<Servicio> hijoAux = new ArrayList<Servicio>();
		Solution hijo = new Solution(3);

		List<Enlace> enlacesIniciales = new ArrayList<Enlace>();
		List<Enlace> enlacesIguales = new ArrayList<Enlace>();
		List<Salto> saltosIguales = new ArrayList<Salto>();

		Camino nuevoPrimario = null;
		Iterator<Servicio> iterador1 = s1.getGenes().iterator();
		Iterator<Servicio> iterador2 = s2.getGenes().iterator();

		Servicio gen1 = null;
		Servicio gen2 = null;

		/*
		 * Calculos del Nuevo Camino Primario y del Nuevo Camino Secundario.
		 */
		while (iterador1.hasNext() && iterador2.hasNext()) {

			enlacesIniciales.clear();
			enlacesIguales.clear();
			saltosIguales.clear();

			gen1 = iterador1.next();
			gen2 = iterador2.next();
			Camino primario1 = gen1.getPrimario();
			Camino primario2 = gen2.getPrimario();
			Nivel nivel = gen1.getSolicitud().getNivel();

			Exclusividad exclusividadPrimario = gen1.getSolicitud()
					.getExclusividadPrimario();

			/*
			 * P1.1. Se obtienen los Enlaces (encapsulados en saltos) del primer
			 * padre.
			 */
			for (Salto salto : primario1.getSaltos()) {
				enlacesIniciales.add(salto.getEnlace());
			}

			/*
			 * P1.2. Se copian los Enlaces iguales (encapsulados en saltos)
			 * entre los caminos de los padres.
			 */
			boolean primerNull = true;
			for (Salto salto : primario2.getSaltos()) {
				Enlace e = salto.getEnlace();

				if (enlacesIniciales.contains(e)) {
					saltosIguales.add(salto);
					
					primerNull = true;
				} else {
					if (primerNull) {
						saltosIguales.add(null);
						primerNull = false;
					}
				}

			}

			Nodo inicio = primario1.getOrigen();
			Nodo fin = primario1.getDestino();

			// se carga el nuevo Camino Primario.
			nuevoPrimario = new Camino(inicio);

			boolean usarDijkstra = false;

			/*
			 * P 2.1. Se iteran sobre los enlaces encontrados y sus nulls.
			 */
			for (Salto s : saltosIguales) {
				// Si dijkstra es true, en la iteración anterior vino null.
				if (usarDijkstra) {

					// Se obtiene el destino del camino construido
					Nodo A = nuevoPrimario.getDestino();
					if (A == null) {
						this.imprimirServicio(gen1, gen2);
						System.err.println("Desde " + A + " hasta " + fin);
						System.err.println(nuevoPrimario);
						System.err.println("Iniciales:");
						for (Enlace en: enlacesIniciales) {
							System.err.println("$ "+en);
						}
						System.err.println("Iguales:");
						for (Salto salto : saltosIguales) {
							System.err.println("$ "+salto);
						}
							
					}
					// Se obtiene el origen del siguiente enlace.
					Nodo B = s.getCanal().getExtremoA();
					if (nuevoPrimario.contiene(B))
						continue;
					// Se construye el camino faltante
					Camino subCamino = A.dijkstra(B, exclusividadPrimario);
					// Condición que si es cierta, ocurrio un error
					
					if (subCamino == null) {
						this.imprimirServicio(gen1, gen2);
						System.err.println("Desde " + A + " hasta " + B);
						System.err.println(nuevoPrimario);
						System.exit(1);
					}

					// se va anexando el camino creado.
					nuevoPrimario.anexar(subCamino);
					// luego de agregar, se bloquea los Canales.
					subCamino.bloquearCanales();

				}

				if (s == null) {
					usarDijkstra = true;
				} else {
					// Variables de Inicializaci�n para dijkstra
					usarDijkstra = false;

					// Se agrega el enlace (encapsulado en el salto)
					nuevoPrimario.addSalto(s);

					// Se bloquea el Canal Agregado.
					s.getCanal().bloquear();
				}

			}

			// Si el ultimo enlace es null, se agrega conexion con el destino
			// final.
			if (nuevoPrimario.esVacio() || usarDijkstra) {
				Nodo A = nuevoPrimario.getDestino();
				if (A == null) {
					this.imprimirServicio(gen1, gen2);
					System.err.println("Desde " + A + " hasta " + fin);
					System.err.println(nuevoPrimario);
					System.err.println("Iniciales:");
					for (Enlace en: enlacesIniciales) {
						System.err.println("$ "+en);
					}
					System.err.println("Iguales:");
					for (Salto salto : saltosIguales) {
						System.err.println("$ "+salto);
					}
				}

				Camino lastCamino = A.dijkstra(fin, exclusividadPrimario);
				if (lastCamino == null) {
					this.imprimirServicio(gen1, gen2);

					System.err.println("Desde " + A + " hasta " + fin);
					System.err.println(nuevoPrimario);

					System.exit(1);
				}
				
				// se va anexando el camino creado.
				nuevoPrimario.anexar(lastCamino);
				// luego de agregar, se bloquea los Canales.
				lastCamino.bloquearCanales();
			}

			/*
			 * P2.2. Se crea el nuevo Servicio y se asigna su Camino Primario.
			 */
			Servicio newServicio = new Servicio(gen1.getSolicitud());
			newServicio.setPrimario(nuevoPrimario);
			newServicio.setPrimario();

			/*
			 * P3.1. Cargar Nuevo Secundario: Realizar algoritmo Shortest Path
			 * Disjktra (SPD) desde el Nodo inicio al Nodo fin.
			 */
			if (nivel != Nivel.Bronce) {
				newServicio.buscarAlternativo();
				newServicio.setAlternativo();
			}
			/*
			 * P3.2. Se almacena el Nuevo Servicio (gen), a un conjunto
			 * auxiliar.
			 */
			hijoAux.add(newServicio);
		}

		/*
		 * P4.1. Se almacena el nuevo conjunto de Nuevos Servicios en el
		 * Conjunto Hijo.
		 */
		Collection<Servicio> aux = new TreeSet<Servicio>(hijoAux);
		hijo.setGenes(aux);

		// System.out.println(" #Hijo#" + hijo + "#Hijo.#");
		// System.out.println("---------------------------");

		return hijo;
	}
	
	private void imprimirServicio(Servicio gen1, Servicio gen2) {
		String dir = "C:\\Users\\mrodas\\Downloads\\tesis";
		Poblacion.getRed().drawServicio(gen1, dir, "cruce_error_a");
		Poblacion.getRed().drawServicio(gen2, dir, "cruce_error_b");
		Poblacion.getRed().utilizacion(dir, "");
	}
}
