package qopmo.nsgaII;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.log4j.BasicConfigurator;

import qopmo.wdm.CanalOptico;
import qopmo.wdm.Nodo;
import qopmo.wdm.Red;
import qopmo.wdm.qop.Caso;
import qopmo.wdm.qop.Nivel;
import qopmo.wdm.qop.Solicitud;

/**
 * Clase de prueba 1.
 * <p>
 * Se prueba cual enfoque de protección es más eficiente. Los enfoques
 * considerados son: Orientado a Camino, Orientado a Segmento y Orientado a
 * Enlace. Se comparan los costos de las mejores soluciones de cada corrida para
 * cada enfoque.
 * </p>
 * Caracteristicas
 * <p>
 * Red NSFNet, donde los Canales Opticos tienen 1 fibra y 55 Longitudes de Onda.
 * El algoritmo Genetico tiene una población de 50 individuos y se realizan 50
 * generaciones. Existen 4 conjuntos de solicitudes de prueba: 10, 20, 30 y 40.
 * Se realizan 10 Corridas para cada conjunto de solicitudes de prueba para
 * obtener el promedio de las mejores Soluciones obtenidas.
 * </p>
 * 
 * @author mrodas
 * 
 */
public class CargarPrueba1 {
	
	// Variables de conexión a la Base de Datos
	private static EntityManagerFactory emf = Persistence
			.createEntityManagerFactory("tesis");
	private static EntityManager em = emf.createEntityManager();

	// Variables de Dimensión de la Red NSF.
	private static int numeroFibras = 1;
	private static int numeroLongitudesDeOnda =55;

	public static void main(String args[]) {
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("genRedes"))
				genRedes();
			if (args[0].equalsIgnoreCase("pruebasCnunez")) {
				prueba_CNnunez10();
				prueba_CNnunez20();
				prueba_CNnunez30();
				prueba_CNnunez40();
			}
		}
	}

	/**
	 * Función para Cargar la Red NSF.
	 */
	private static void genRedes() {

		/* NSFnet */
		int[][] NSFnet_enlaces = { { 1, 2, 1 }, { 1, 3, 1 }, { 1, 4, 1 },
				{ 2, 4, 1 }, { 2, 7, 1 }, { 3, 5, 1 }, { 3, 8, 1 },
				{ 4, 11, 1 }, { 5, 6, 1 }, { 5, 11, 1 }, { 6, 7, 1 },
				{ 7, 10, 1 }, { 8, 9, 1 }, { 8, 14, 1 }, { 9, 10, 1 },
				{ 9, 13, 1 }, { 10, 12, 1 }, { 10, 14, 1 }, { 11, 12, 1 },
				{ 11, 13, 1 }, { 13, 14, 1 }, };

		persistNet(14, NSFnet_enlaces, "NSFNet");
		System.out.println("NSF_NET: " + NSFnet_enlaces.length);
	}

	private static void persistNet(int nodos, int[][] enlaces, String nombre) {

		HashMap<String, Nodo> nodoMap = new HashMap<String, Nodo>();
		Red red = new Red();
		red.setNombre(nombre);

		em.getTransaction().begin();
		for (int i = 1; i <= nodos; i++) {
			Nodo nodo = new Nodo();
			nodo.setLabel("" + i);
			nodoMap.put("" + i, nodo);
			red.addNodo(nodo);
			System.out.println("=>"+nodo.toString());
			
		}
		em.persist(red);
		em.getTransaction().commit();

		em.getTransaction().begin();
		for (int i = 0; i < enlaces.length; i++) {
			Nodo a = nodoMap.get("" + enlaces[i][0]);
			Nodo b = nodoMap.get("" + enlaces[i][1]);
			CanalOptico canal = new CanalOptico(a, b, numeroFibras, numeroLongitudesDeOnda);
			a.addCanal(canal);
			b.addCanal(canal);
			canal.setCosto(enlaces[i][2]);
			red.addCanal(canal);
		}
		em.persist(red);
		em.getTransaction().commit();
		// casos de prueba;
		prueba_CNnunez10();
		prueba_CNnunez20();
		prueba_CNnunez30();
		prueba_CNnunez40();
	}

	/**
	 * Función para cargar solicitudes con Nivel Oro.
	 */
	private static Set<Solicitud> cargarSolicitudes(List<Long[]> lista) {

		Set<Solicitud> solicitudes = new HashSet<Solicitud>();
		Nodo origen = null;
		Nodo destino = null;
		Solicitud sol = null;
		for (Long[] par : lista) {
			origen = em.find(Nodo.class, par[0]);
			destino = em.find(Nodo.class, par[1]);
			sol = new Solicitud(origen, destino, Nivel.Oro);
			em.getTransaction().begin();
			em.persist(sol);
			em.getTransaction().commit();
			solicitudes.add(sol);
		}

		return solicitudes;
	}

	/**
	 * Función que carga el Caso 1 que corresponde a 10 Solicitudes de Oro.
	 */
	private static void prueba_CNnunez10() {
		Caso c = new Caso(em.find(Red.class, 1), "CasoCNunez_10");
		// (2,15,2) (14,5,2) (4,13,2) (12,9,2) (8,14,2)
		// (4,14,1) (12,8,1) (2,14,1) (3,14,0) (9,13,0)
		Long[][] aux = { { 2L, 15L }, { 14L, 5L }, { 4L, 13L }, { 12L, 9L },
				{ 8L, 14L }, { 4L, 14L }, { 12L, 8L }, { 2L, 14L },
				{ 3L, 14L }, { 9L, 13L } };

		List<Long[]> lista = new ArrayList<Long[]>();
		for (int i = 0; i < aux.length; i++) {
			lista.add(aux[i]);
		}

		Set<Solicitud> solicitudes = cargarSolicitudes(lista);
		c.setSolicitudes(solicitudes);
		em.getTransaction().begin();
		em.persist(c);
		em.getTransaction().commit();
	}

	/**
	 * Función que carga el Caso 2 que corresponde a 20 Solicitudes de Oro.
	 */
	private static void prueba_CNnunez20() {
		Caso c = new Caso(em.find(Red.class, 1), "CasoCNunez_20");

		// (3,14,2) (8,15,2) (5,13,2) (2,10,2) (6,13,2)
		// (5,15,2) (3,15,2) (4,13,2) (10,8,2) (11,2,2)
		// (7,15,2) (14,7,2) (9,7,1) (5,14,1) (14,6,1)
		// (3,15,1) (3,15,0) (4,13,0) (8,15,0) (11,2,0)
		Long[][] aux = { { 3L, 14L }, { 8L, 15L }, { 5L, 13L }, { 2L, 10L },
				{ 6L, 13L }, { 5L, 15L }, { 3L, 15L }, { 4L, 13L },
				{ 10L, 8L }, { 11L, 2L }, { 7L, 15L }, { 14L, 7L }, { 9L, 7L },
				{ 5L, 14L }, { 14L, 6L }, { 3L, 15L }, { 3L, 15L },
				{ 4L, 13L }, { 8L, 15L }, { 11L, 2L } };

		List<Long[]> lista = new ArrayList<Long[]>();
		for (int i = 0; i < aux.length; i++) {
			lista.add(aux[i]);
		}
		c.setSolicitudes(cargarSolicitudes(lista));

		em.getTransaction().begin();
		em.persist(c);
		em.getTransaction().commit();

	}

	/**
	 * Función que carga el Caso 3 que corresponde a 30 Solicitudes de Oro.
	 */
	private static void prueba_CNnunez30() {
		Caso c = new Caso(em.find(Red.class, 1), "CasoCNunez_30");
		// (3,14,2) (8,15,2) (5,13,2) (2,10,2) (6,13,2)
		// (5,15,2) (3,15,2) (4,13,2) (10,8,2) (11,2,2)
		// (7,2,2) (14,7,2) (12,9,2) (4,11,2) (6,11,2)
		// (7,15,2) (10,5,2) (12,10,2) (3,15,1) (5,14,1)
		// (9,7,1) (12,2,1) (14,6,1) (6,5,1) (7,9,1)
		// (3,15,0) (4,13,0) (8,15,0) (11,2,0) (9,14,0)
		Long[][] aux = { { 3L, 14L }, { 8L, 15L }, { 5L, 13L }, { 2L, 10L },
				{ 6L, 13L }, { 5L, 15L }, { 3L, 15L }, { 4L, 13L },
				{ 10L, 8L }, { 11L, 2L }, { 7L, 2L }, { 14L, 7L }, { 12L, 9L },
				{ 4L, 11L }, { 6L, 11L }, { 7L, 15L }, { 10L, 5L },
				{ 12L, 10L }, { 3L, 15L }, { 5L, 14L }, { 9L, 7L },
				{ 12L, 2L }, { 14L, 6L }, { 6L, 5L }, { 7L, 9L }, { 3L, 15L },
				{ 4L, 13L }, { 8L, 15L }, { 11L, 2L }, { 9L, 14L } };

		List<Long[]> lista = new ArrayList<Long[]>();
		for (int i = 0; i < aux.length; i++) {
			lista.add(aux[i]);
		}

		c.setSolicitudes(cargarSolicitudes(lista));

		em.getTransaction().begin();
		em.persist(c);
		em.getTransaction().commit();
	}

	/**
	 * Función que carga el Caso 4 que corresponde a 40 Solicitudes de Oro.
	 */
	private static void prueba_CNnunez40() {
		Caso c = new Caso(em.find(Red.class, 1), "CasoCNunez_40");
		// (3,14,2) (8,15,2) (5,13,2) (2,10,2) (6,13,2) (5,15,2) (3,15,2)
		// (4,13,2) (10,8,2) (11,2,2) (7,2,2) (14,7,2) (12,9,2) (4,11,2)
		// (6,11,2)
		// (7,15,2) (10,5,2) (12,10,2) (10,14,2) (8,9,2) (2,6,2) (4,3,2)
		// (7,10,2)
		// (6,9,2) (3,15,1) (5,14,1) (9,7,1) (12,2,1) (14,6,1) (6,5,1) (7,9,1)
		// (8,12,1) (4,11,1) (12,5,1) (3,15,0) (4,13,0) (8,15,0) (11,2,0)
		// (9,14,0)
		// (13,9,0)
		Long[][] aux = { { 3L, 14L }, { 8L, 15L }, { 5L, 13L }, { 2L, 10L },
				{ 6L, 13L }, { 5L, 15L }, { 3L, 15L }, { 4L, 13L },
				{ 10L, 8L }, { 11L, 2L }, { 7L, 2L }, { 14L, 7L }, { 12L, 9L },
				{ 4L, 11L }, { 6L, 11L }, { 7L, 15L }, { 10L, 5L },
				{ 12L, 10L }, { 10L, 14L }, { 8L, 9L }, { 2L, 6L }, { 4L, 3L },
				{ 7L, 10L }, { 6L, 9L }, { 3L, 15L }, { 5L, 14L }, { 9L, 7L },
				{ 12L, 2L }, { 14L, 6L }, { 6L, 5L }, { 7L, 9L }, { 8L, 12L },
				{ 4L, 11L }, { 12L, 5L }, { 3L, 15L }, { 4L, 13L },
				{ 8L, 15L }, { 11L, 2L }, { 9L, 14L }, { 13L, 9L } };

		List<Long[]> lista = new ArrayList<Long[]>();
		for (int i = 0; i < aux.length; i++) {
			lista.add(aux[i]);
		}

		c.setSolicitudes(cargarSolicitudes(lista));

		em.getTransaction().begin();
		em.persist(c);
		em.getTransaction().commit();
	}
}
