package qopmo.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVWriter {

	List<List<String>> valores;
	List<String> mejor;

	public CSVWriter() {
		valores = new ArrayList<List<String>>();
	}

	public List<List<String>> getValores() {
		return valores;
	}

	public void setValores(List<List<String>> valores) {
		this.valores = valores;
	}

	public void addValor(List<String> valor) {
		this.valores.add(valor);
	}

	public void generateCsvFile(String nombre, int cantidad) {
		if (nombre == null)
			nombre = "output2.csv";

		/*
		 * Se agregan los valores de las N generaciones de una corrida.
		 */
		String dir = "/home/sergio/Documentos/Resultados-Tesis/prueba1/";
		/*
		 * String csv = dir + nombre + ".csv"; try { FileWriter writer = new
		 * FileWriter(csv);
		 * 
		 * // Se agrega el titulo writer.append("Generación");
		 * writer.append(';'); writer.append("Costo"); writer.append(';');
		 * writer.append("Fallas Oro Primario"); writer.append(';');
		 * writer.append("Fallas Oro Alternativo"); writer.append('\n');
		 * 
		 * this.writeAll(writer, this.valores);
		 * 
		 * writer.flush(); writer.close(); } catch (IOException e) {
		 * e.printStackTrace(); }
		 */

		/*
		 * Se agrega el mejor de las N generaciones.
		 */
		this.mejor = valores.get(valores.size() - 1);

		String mejores = dir;

		if (nombre.contains("Link")) {
			mejores += "Link_Bests_"+cantidad+".csv";
			this.mejor.set(0, "Link_Best_" + this.mejor.get(0));
		} else if (nombre.contains("Segment")) {
			mejores += "Segment_Bests_"+cantidad+".csv";
			this.mejor.set(0, "Segment_Best_" + this.mejor.get(0));
		} else if (nombre.contains("Path")) {
			mejores += "Path_Bests_"+cantidad+".csv";
			this.mejor.set(0, "Path_Best_" + this.mejor.get(0));
		} else {
			mejores += "ERROR_Bests_10.csv";
			this.mejor.set(0, "");
		}

		try {
			FileWriter writer = new FileWriter(mejores, true);

			// Se agrega el titulo
			if (nombre.contains("1_")) {
				writer.append("Corrida");
				writer.append(';');
				writer.append("Costo");
				writer.append(';');
				writer.append("Fallas Oro Primario");
				writer.append(';');
				writer.append("Fallas Oro Alternativo");
				writer.append('\n');
			}

			this.write(writer, this.mejor);

			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void writeAll(FileWriter writer, List<List<String>> valores)
			throws IOException {

		for (List<String> s : valores) {
			write(writer, s);
		}
	}

	private void write(FileWriter writer, List<String> list) throws IOException {
		int i = 1;
		for (String valor : list) {
			writer.append(valor);
			if (i < 4)
				writer.append(';');
			else
				writer.append('\n');
			i++;
		}
	}
}
