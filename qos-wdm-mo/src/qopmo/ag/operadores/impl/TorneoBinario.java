package qopmo.ag.operadores.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import qopmo.ag.Individuo;
import qopmo.ag.Poblacion;
import qopmo.ag.operadores.OperadorSeleccion;

public class TorneoBinario implements OperadorSeleccion {

	@Override
	public Collection<Individuo> seleccionar(Poblacion poblacion) {

		if (poblacion == null)
			throw new Error("La poblacion no existe.");

		// Tamaño de población seleccionada
		int cantMejores = poblacion.getTamanho();

		// Auxiliar de Individuos
		ArrayList<Individuo> individuos = poblacion.getIndividuosToArray();

		// Cromosomas seleccionados
		List<Individuo> respuesta = new ArrayList<Individuo>();

		// Se inicializa la clase Random
		Random rand = new Random();
		rand.nextInt();

		for (int i = 0; i < cantMejores; i++) {

			// Se eligen a dos individuos (torneo "binario")
			int ind1 = rand.nextInt(cantMejores);
			int ind2 = rand.nextInt(cantMejores);

			// Nos aseguramos que no sean del mismo indice.
			int limite = 1;
			while (ind1 == ind2 && limite < 5) {
				ind2 = rand.nextInt(cantMejores);
				limite++;
			}

			Individuo individuo1 = individuos.get(ind1);
			Individuo individuo2 = individuos.get(ind2);

			// Se calculan las variables de evaluación correspondientes a los
			// individuos
			individuo1.evaluar();
			individuo2.evaluar();

			// Competencia
			boolean valor = true;
			boolean SegundoEsMejor = individuo1.comparar(individuo2);

			if (SegundoEsMejor) {
				// Ganó el segundo
				valor = respuesta.add(individuo2);
			} else {
				// Gano el primero
				valor = respuesta.add(individuo1);
			}

			if (!valor) {
				System.out.println("$$1#" + individuo1);
				System.out.println("$$2#" + individuo2);
				throw new Error("No funciona el equals de Solucion."
						+ respuesta);
			}
		}

		return respuesta;
	}

}
