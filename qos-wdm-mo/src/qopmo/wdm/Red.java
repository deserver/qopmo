package qopmo.wdm;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import qopmo.wdm.qop.EsquemaRestauracion;
import qopmo.wdm.qop.Servicio;

/**
 * Clase que representa la red sobre la que se simularan las solicitudes.
 * <p>
 * Descripcion: Red cuyos componentes son: una instancia de la misma Red, un
 * conjunto de nodos y un conjunto de canales.
 * </p>
 * 
 * @author aamadeo
 * 
 */
@Entity
@Table(name = "Red")
public class Red {

	@OneToMany(cascade = CascadeType.ALL)
	private Set<Nodo> nodos = new HashSet<Nodo>();

	@OneToMany(cascade = CascadeType.ALL)
	private Set<CanalOptico> canales = new HashSet<CanalOptico>();

	@Id
	@GeneratedValue
	private int id;

	private String nombre;

	/**
	 * Constructor principal
	 */
	public Red() {
		nodos.clear();
		canales.clear();
	}

	/* -----------------------Metodos delegados del conjunto----------------- */

	/**
	 * Función que controla la existencia de un nodo en la red a partir de la
	 * Instancia del Nodo buscado. Retorna true si el nodo ya forma parte de la
	 * red.
	 * 
	 * @param nodo
	 *            Nodo a ser buscado
	 * @return Existencia del nodo
	 */
	public boolean existeNodo(Nodo nodo) {
		return nodos.contains(nodo);
	}

	/**
	 * Agrega un nodo a la red
	 * 
	 * @param key
	 *            Etiqueta del nodo
	 * @param value
	 *            Nodo
	 * @return true si cambio la red
	 */
	public boolean addNodo(Nodo value) {
		return nodos.add(value);
	}

	/**
	 * Elimina el nodo de la red, a partir de su clave.
	 * 
	 * @param nodo
	 *            Nodo a eliminar
	 * @return true si cambio la red
	 */
	public boolean removeNodo(Nodo nodo) {
		if (nodo != null) {
			nodo.romperEnlaces(this);
		}

		return nodos.remove(nodo);
	}

	/**
	 * Retorna el numero de nodos que posee la red.
	 * 
	 * @return
	 */
	public int cantidadNodos() {
		return nodos.size();
	}

	/**
	 * Agrega un canal a la red
	 * 
	 * @param canal
	 * @return true si pudo insertar el canal
	 */
	public boolean addCanal(CanalOptico canal) {
		return canales.add(canal);
	}

	/**
	 * Retorna true si el canal existe
	 * 
	 * @param canal
	 *            CanalOptico que se busca
	 * @return true si el canal existe
	 */
	public boolean existeCanal(CanalOptico canal) {
		return canales.contains(canal);
	}

	/**
	 * Elimina un canal de la red
	 * 
	 * @param canal
	 *            Canal de la red
	 * @return true si pudo eliminar de la red
	 */
	public boolean removeCanal(CanalOptico canal) {
		return canales.remove(canal);
	}

	/**
	 * Retorna la cantidad de canales de la red
	 * 
	 * @return Numero de canales
	 */
	public int cantidadCanales() {
		return canales.size();
	}

	public Set<Nodo> getNodos() {
		return nodos;
	}

	public void setNodos(Set<Nodo> nodos) {
		this.nodos = nodos;
	}

	public Set<CanalOptico> getCanales() {
		return canales;
	}

	public void setCanales(Set<CanalOptico> canales) {
		this.canales = canales;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Nodo randomNodo() {
		double i = Math.random() * ((double) nodos.size());
		int j = 1;

		Iterator<Nodo> iter = nodos.iterator();

		while (j < i) {
			iter.next();
			j++;
		}

		return iter.next();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void inicializar() {

		for (CanalOptico canal : canales) {
			canal.inicializar();
		}

		for (Nodo nodo : nodos) {
			nodo.inicializar();
		}
	}

	public void imprimirRed() {

		System.out.println("[RED: " + this.nombre + " [");
		for (CanalOptico co : this.getCanales()) {
			System.out.print("[" + co.getExtremoA().getLabel() + "->"
					+ co.getExtremoB().getLabel() + "] ");
		}
		System.out.println("] \n]");
	}

	/**
	 * Grafica el grafo de utilizacion de la red. Donde : a) El color verde :
	 * indica baja utilizacion del canal (uso <=33%) b) El color azul : indica
	 * utilizacion media del canal (33% < uso <= 66%) c) El color rojo : indica
	 * utilizacion alta del canal (66% < uso < 100%) d) El color negro : indica
	 * total utilizacion del canal (uso = 100%)
	 * 
	 * Las lineas solidas indican que el canal tiene enlaces sin reservas. Las
	 * lineas cortadas indica que el canal no tiene enlaces sin reservas, es
	 * decir que no tiene enlaces exclusivos.
	 * 
	 * @param dir
	 *            Directorio (existente) donde crear la imagen.
	 * @param dif
	 *            Cadena utilizada para diferenciar a varias imagenes el mismo
	 *            grafo
	 */
	public void utilizacion(String dir, String dif) {
		String graphName = this.nombre + "_utilizacion" + dif;
		String fileName = graphName + ".gv";
		// String cmd ="\"C:\\Program Files (x86)\\Graphviz 2.28\\bin\\dot.exe\"";
		String cmd = "\"C:\\Program Files (x86)\\Graphviz2.30\\bin\\dot.exe\"";

		cmd += " -Ksfdp -Goverlap=prism -Tpng -o \"" + dir + "\\" + graphName
				+ ".png\" \"";
		cmd += dir + "\\" + fileName + "\"";

		try {
			FileWriter fw = new FileWriter(new File(dir + "/" + fileName));

			fw.write("graph " + graphName + " {\n");

			for (Nodo nodo : nodos) {
				String spec = " [penwidth=1 "
						+ (nodo.estaBloqueado() ? ", style=filled, fillcolor=\"#000000\")"
								: "") + " ];\n";
				fw.write(nodo + spec);
			}

			for (CanalOptico canal : canales) {
				int uso = canal.getUso();
				int grosor = 1 + uso / 15;

				String spec = "[penwidth=" + grosor + ", weight=2";

				if (!canal.tieneEnlacesExclusivos()) {
					spec += " style=\"dashed\"";
				}

				if (0 == uso) {
					spec += ", color=\"#AAAAAA\"];";
				} else if (0 < uso && uso <= 33) {
					spec += ", color=\"#11AA11\"];";
				} else if (33 < uso && uso <= 66) {
					spec += ", color=\"#AAAA11\"];";
				} else if (66 < uso && uso < 100) {
					spec += ", color=\"#AA1111\"];";
				} else {
					spec += ", color=\"#000000\"];";
				}

				fw.write(canal.getExtremoA() + " -- " + canal.getExtremoB()
						+ spec);
			}

			fw.write("}");
			fw.flush();
			fw.close();

			Process p = Runtime.getRuntime().exec(cmd);
			p.waitFor();

			File dotFile = new File(dir + "\\" + fileName);
			dotFile.delete();

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (InterruptedException ie) {
			ie.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Indica los canales del grafo utilizados por el servicio <b>s</b>.
	 * 
	 * @param s
	 *            Servicio a graficar
	 * @param dir
	 *            Directorio donde crear la imagen
	 * @param nombre
	 *            Nombre de la imagen a crear. Por defecto se utiliza el nombre
	 *            del grafo + "_" + el valor toString() del servicio.
	 */
	public void drawServicio(Servicio s, String dir, String nombre) {
		String graphName = nombre == null ? this.nombre + "_" + s : nombre;
		String fileName = graphName + ".gv";
		String cmd = "\"C:\\Program Files (x86)\\Graphviz2.30\\bin\\dot.exe\"";
		
		// String cmd =
		// "\"C:\\Program Files (x86)\\Graphviz 2.28\\bin\\dot.exe\"";
		cmd += " -Ksfdp -Goverlap=prism -Gsplines=true -Tpng -o \"" + dir
				+ "\\" + graphName + ".png\" \"";
		cmd += dir + "\\" + fileName + "\"";

		Nodo origen = s.getSolicitud().getOrigen();
		Nodo destino = s.getSolicitud().getDestino();
		EsquemaRestauracion esquema = s.getSolicitud().getEsquema();

		HashSet<Nodo> nodosPrimarios = new HashSet<Nodo>();
		HashSet<Nodo> nodosAlternativos = new HashSet<Nodo>();
		List<CanalOptico> canalesPrimarios = new ArrayList<CanalOptico>();
		List<CanalOptico> canalesAlternativos = new ArrayList<CanalOptico>();
		List<Enlace> enlacesPrimarios = new ArrayList<Enlace>();
		List<Enlace> enlacesAlternativos = new ArrayList<Enlace>();

		/*
		 * Se cargan y separan en listas los conjuntos de nodos y enlaces
		 * primarios y alternativos.
		 */
		if (s.getPrimario() != null) {
			Nodo actual = s.getPrimario().getOrigen();
			nodosPrimarios.add(actual);
			for (Salto salto : s.getPrimario().getSaltos()) {
				CanalOptico canal = salto.getCanal();
				actual = canal.getOtroExtremo(actual);

				nodosPrimarios.add(actual);
				canalesPrimarios.add(canal);
				enlacesPrimarios.add(salto.getEnlace());
			}

			Camino alternativo = s.getAlternativo();
			if (alternativo != null) {
				actual = s.getAlternativo().getOrigen();
				nodosAlternativos.add(actual);

				for (Salto salto : s.getAlternativo().getSaltos()) {
					CanalOptico canal = salto.getCanal();

					actual = canal.getOtroExtremo(actual);
					nodosAlternativos.add(actual);

					canalesAlternativos.add(canal);
					enlacesAlternativos.add(salto.getEnlace());
				}
			} else {
				if (esquema == EsquemaRestauracion.Link) {
					List<Camino> alternativos = s.getAlternativoLink();

					if (alternativos != null) {
						for (Camino c : alternativos) {
							actual = c.getOrigen();
							nodosAlternativos.add(actual);

							for (Salto salto : c.getSaltos()) {
								CanalOptico canal = salto.getCanal();

								actual = canal.getOtroExtremo(actual);
								nodosAlternativos.add(actual);

								canalesAlternativos.add(canal);
								enlacesAlternativos.add(salto.getEnlace());
							}
						}
					}
				}
			}
		}

		try {
			FileWriter fw = new FileWriter(new File(dir + "\\" + fileName));

			fw.write("graph " + graphName + " {\n");

			/*
			 * Se carga los Nodos al archivo para Graficar.
			 */
			for (Nodo nodo : nodos) {
				String spec = " ";

				if (origen.equals(nodo)) {
					spec += " [penwidth=3, style=filled, fillcolor=\"#AA1111\"];\n";
				} else if (destino.equals(nodo)) {
					spec += " [penwidth=4, style=filled, fillcolor=\"#AABB11\"];\n";
				} else if (nodosPrimarios.contains(nodo)) {
					spec += " [penwidth=3, style=filled, fillcolor=\"#11AA11\"];\n";
				} else if (nodosAlternativos.contains(nodo)) {
					spec += " [penwidth=3, style=filled, fillcolor=\"#1111AA\"];\n";
				} else {
					spec += "[penwidth=1];\n";
				}

				fw.write(nodo + spec);
			}

			/*
			 * Se carga los canales al archivo para Graficar.
			 */
			for (CanalOptico canal : canales) {
				String spec = " ";
				int ind = 0;
				String label = "";
				if (canalesPrimarios.contains(canal)) {
					ind = canalesPrimarios.indexOf(canal);
					label = enlacesPrimarios.get(ind).toString2();
					spec += "[penwidth=6, weight=2, color=\"#11AA11\"";
				} else if (canalesAlternativos.contains(canal)) {
					ind = canalesAlternativos.indexOf(canal);
					label = enlacesAlternativos.get(ind).toString2();
					spec += "[penwidth=3, weight=2, color=\"#1111AA\"";
				} else {
					spec += "[penwidth=1";
				}

				spec += ", label=\"" + label + "\"];";

				fw.write(canal.getExtremoA() + " -- " + canal.getExtremoB()
						+ spec);
			}

			fw.write("}");
			fw.flush();
			fw.close();

			Process p = Runtime.getRuntime().exec(cmd);
			p.waitFor();

			File dotFile = new File(dir + "\\" + fileName);
			dotFile.delete();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (InterruptedException ie) {
			ie.printStackTrace();
			System.exit(1);
		}
	}
}
