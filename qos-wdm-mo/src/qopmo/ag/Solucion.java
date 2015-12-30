package qopmo.ag;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import qopmo.wdm.Camino;
import qopmo.wdm.CanalOptico;
import qopmo.wdm.Enlace;
import qopmo.wdm.qop.EsquemaRestauracion;
import qopmo.wdm.qop.Nivel;
import qopmo.wdm.qop.Servicio;
import qopmo.wdm.qop.Solicitud;

/**
 * Clase Solución que implementa al Individuo.
 * <p>
 * Conceptualmente esta clase es el Cromosoma del Algoritmo Genético. Tiene el
 * conjunto de genes que representan las partes de la solución: genes (Conjunto
 * de Servicios (tiene la solicitud, el camino primario y el camino
 * secundario)), su fitness y su costo.
 * </p>
 */
@Entity
@Table(name = "Solucion")
public class Solucion  {

	@Id
	@GeneratedValue
	private long id;

	// Genes de la solución (Conjunto de Servicios)
	@ManyToMany(cascade = CascadeType.ALL)
	private Set<Servicio> genes;

	// Fitness de la Solución
	private double fitness;

	// Costo de la Solución
	private double costo;

	// Valor por kilometro.
	public static double a = 0.1;
	// Valor por cambio de longitud de onda
	public static double b = 2;

	@Transient
	private int contadorFailOroPrimario = 0;
	@Transient
	private int contadorFailPlataPrimario = 0;
	@Transient
	private int contadorFailBroncePrimario = 0;
	@Transient
	private int contadorFailOroAlternativo = 0;
	@Transient
	private int contadorFailPlataAlternativo = 0;
	@Transient
	private Set<Enlace> enlacesContado;

	public Solucion() {
		super();
		this.genes = new TreeSet<Servicio>();
		this.fitness = Double.MAX_VALUE;
		this.costo = Double.MAX_VALUE;
	}

	public Solucion(Set<Solicitud> solicitudes) {
		super();

		Set<Servicio> servicios = new TreeSet<Servicio>();
		for (Solicitud s : solicitudes) {
			Servicio servicio = new Servicio(s);
			servicio.setDisponible(true);
			servicios.add(servicio);
		}

		this.genes = new TreeSet<Servicio>(servicios);
		this.fitness = Double.MAX_VALUE;
		this.costo = Double.MAX_VALUE;
	}

	/**
	 * Obtener Fitness.
	 * 
	 * @return the fitness
	 */
	public double getFitness() {
		return fitness;
	}

	/**
	 * Asignar Fitness.
	 * 
	 * @param fitness
	 *            de la solución
	 */
	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	/**
	 * @return the costo
	 */
	public double getCosto() {
		return costo;
	}

	/**
	 * @param costo
	 *            the costo to set
	 */
	public void setCosto(Double costo) {
		this.costo = costo;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public TreeSet<Servicio> getGenes() {
		return (TreeSet<Servicio>) genes;
	}

	public void setGenes(Collection<Servicio> hijoAux) {
		this.genes = (TreeSet<Servicio>) hijoAux;
	}

	public int getContadorFailOro() {
		return contadorFailOroPrimario;
	}

	public void setContadorFailOro(int contadorFailOro) {
		this.contadorFailOroPrimario = contadorFailOro;
	}

	public int getContadorFailPlata() {
		return contadorFailPlataPrimario;
	}

	public void setContadorFailPlata(int contadorFailPlata) {
		this.contadorFailPlataPrimario = contadorFailPlata;
	}

	public int getContadorFailBronce() {
		return contadorFailBroncePrimario;
	}

	public void setContadorFailBronce(int contadorFailBronce) {
		this.contadorFailBroncePrimario = contadorFailBronce;
	}

	public int getContadorFailOroAlternativo() {
		return contadorFailOroAlternativo;
	}

	public void setContadorFailOroAlternativo(int contadorFailOroAlternativo) {
		this.contadorFailOroAlternativo = contadorFailOroAlternativo;
	}

	public int getContadorFailPlataAlternativo() {
		return contadorFailPlataAlternativo;
	}

	public void setContadorFailPlataAlternativo(int contadorFailPlataAlternativo) {
		this.contadorFailPlataAlternativo = contadorFailPlataAlternativo;
	}

	/*
	 * OPERACIONES SOBRE LA SOLUCION
	 */

	/**
	 * Función para generar Servicios Randómicos.
	 * 
	 * @param esquema
	 */
	public void random(EsquemaRestauracion esquema) {
		for (Servicio s : this.genes) {
			s.getSolicitud().setEsquema(esquema);
			s.random();
		}
	}

	public void extremos(int i) {
		EsquemaRestauracion e = null;
		if (i == 1) {
			e = EsquemaRestauracion.FullPath;
		} else if (i == 2) {
			e = EsquemaRestauracion.Link;
		}

		for (Servicio s : this.genes) {
			s.getSolicitud().setEsquema(e);
			s.extremos();
			s.getSolicitud().setEsquema(EsquemaRestauracion.Segment);
		}
	}

	/**
	 * Calcula el costo en función de la Fórmula de Evaluación Definida. Tambien
	 * mantiene contadores de Alternativos no existentes, cuando deberían
	 * existir.
	 * <p>
	 * Costo = suma_de_distancia x a + suma_de_cambios_LDO x b
	 * </p>
	 */
	@Deprecated
	public double evaluar() {

		this.contadorFailOroPrimario = 0;
		this.contadorFailOroAlternativo = 0;
		this.contadorFailPlataPrimario = 0;
		this.contadorFailPlataAlternativo = 0;
		this.contadorFailBroncePrimario = 0;

		// Costo de una Solucion
		this.costo = this.costoTotalCanales2();
		// Fitness de la Solución
		this.fitness = 1 / this.costo;

		return this.fitness;
	}

	/*
	 * Obtiene el costo total de los canales utilizados en la solución.
	 * 
	 * @return
	 */
	public int contadorCosto;
	public int cambiosLDO;

	/*
	 * Obtiene el costo total de los canales utilizados en la solución.
	 * 
	 * @return
	 */
	@Deprecated
	private double costoTotalCanales() {
		contadorCosto = 0;
		cambiosLDO = 0;
		// Set<CanalOptico> auxiliar = new HashSet<CanalOptico>();
		/*
		 * El cálculo de las variables del costo se suman para cada gen
		 * (Servicio) del individuo (Solucion).
		 */
		for (Servicio gen : this.genes) {

			Enlace e1 = null;
			Enlace e2 = null;
			int ldo1 = 0;
			int ldo2 = 0;

			if (gen == null)
				continue;

			// Se cuenta cada Oro que no tiene un alternativo.
			if (!gen.oroTieneAlternativo())
				this.contadorFailOroAlternativo++;

			// Se cuenta cada Plata que no tiene un alternativo.
			if (!gen.plataTieneAlternativo())
				this.contadorFailPlataAlternativo++;

			/*
			 * Si no tiene primario se cuenta. Si tiene primario se suman los
			 * costos de Canales Opticos utilizados (no se repite) y se cuantan
			 * los cambios de Longitud de Onda realizados.
			 */
			Camino primario = gen.getPrimario();

			if (primario == null) {
				if (gen.getSolicitud().getNivel() == Nivel.Oro)
					this.contadorFailOroPrimario++;
				else if (gen.getSolicitud().getNivel() == Nivel.Plata1)
					this.contadorFailPlataPrimario++;
				else
					this.contadorFailBroncePrimario++;
			} else {

				boolean primero = true;

				for (Enlace s : primario.getEnlaces()) {

					CanalOptico ca = s.getCanal();

					if (primero) {
						e1 = s;
						primero = false;
					} else {
						e1 = e2;
					}
					ldo1 = e1.getLongitudDeOnda();
					e2 = s;
					ldo2 = e2.getLongitudDeOnda();

					// Si existe un cambio de longitud de onda, se suman en 1.
					if (ldo1 != ldo2)
						cambiosLDO++;

					// inserto es false cuando ya existía (no suma)
					// boolean inserto = auxiliar.add(ca);
					// se suman costos de Canales Opticos utilizados.
					// if (inserto)
					contadorCosto += ca.getCosto();
				}
			}

			if (gen.getSolicitud().getEsquema() != EsquemaRestauracion.Link) {
				Camino alternativo = gen.getAlternativo();
				if (alternativo != null) {
					boolean primero = true;

					for (Enlace s : alternativo.getEnlaces()) {

						CanalOptico ca = s.getCanal();

						if (primero) {
							e1 = s;
							primero = false;
						} else {
							e1 = e2;
						}
						ldo1 = e1.getLongitudDeOnda();
						e2 = s;
						ldo2 = e2.getLongitudDeOnda();

						// Si existe un cambio de longitud de onda, se suman en
						// 1.
						if (ldo1 != ldo2)
							cambiosLDO++;

						// inserto es false cuando ya existía (no suma)
						// boolean inserto = auxiliar.add(ca);
						// se suman costos de Canales Opticos utilizados.
						// if (inserto)
						contadorCosto += ca.getCosto();
					}

				}
			} else {
				if (gen.getAlternativoLink() != null) {
					for (Camino alternativo : gen.getAlternativoLink()) {

						if (alternativo != null) {
							boolean primero = true;

							for (Enlace s : alternativo.getEnlaces()) {
								if (s == null)
									continue;
								CanalOptico ca = s.getCanal();

								if (primero) {
									e1 = s;
									primero = false;
								} else {
									e1 = e2;
								}
								ldo1 = e1.getLongitudDeOnda();
								e2 = s;
								ldo2 = e2.getLongitudDeOnda();

								// Si existe un cambio de longitud de onda, se
								// suman en 1.
								if (ldo1 != ldo2)
									cambiosLDO++;

								// inserto es false cuando ya existía (no suma)
								// boolean inserto = auxiliar.add(ca);
								// se suman costos de Canales Opticos
								// utilizados.
								// if (inserto)
								contadorCosto += ca.getCosto();
							}
						}
					}
				}
			}
		}

		// Fórmula de Costo de una Solucion
		double costo = (contadorCosto * a) + (cambiosLDO * b);
		return costo;
	}

	/*
	 * Obtiene el costo total de los canales utilizados en la solución.
	 * 
	 * @return
	 */
	private double costoTotalCanales2() {
		contadorCosto = 0;
		cambiosLDO = 0;
		enlacesContado = new HashSet<Enlace>();
		/*
		 * El cálculo de las variables del costo se suman para cada gen
		 * (Servicio) del individuo (Solucion).
		 */
		for (Servicio gen : this.genes) {

			if (gen == null)
				continue;

			// Se cuenta cada Oro que no tiene un alternativo.
			if (!gen.oroTieneAlternativo())
				this.contadorFailOroAlternativo++;

			// Se cuenta cada Plata que no tiene un alternativo.
			if (!gen.plataTieneAlternativo())
				this.contadorFailPlataAlternativo++;

			/*
			 * Evaluacion Primario: Si no tiene primario se cuenta como Error.
			 * Si tiene primario se suman sus costos de Canales Opticos
			 * utilizados y se cuentan los cambios de Longitud de Onda
			 * realizados.
			 */
			Camino primario = gen.getPrimario();

			if (primario == null) {
				if (gen.getSolicitud().getNivel() == Nivel.Oro)
					this.contadorFailOroPrimario++;
				else if (gen.getSolicitud().getNivel() == Nivel.Plata1)
					this.contadorFailPlataPrimario++;
				else if (gen.getSolicitud().getNivel() == Nivel.Bronce)
					this.contadorFailBroncePrimario++;
			} else {
				// Se cuentan y suman los enlaces y cambios de longitud de onda
				// del primario.
				contadorInterno(primario.getEnlaces());
			}

			/*
			 * Evaluación Alternativo: Si tiene alternativo se suman los costos
			 * de Canales Opticos utilizados y se cuentan los cambios de
			 * Longitud de Onda realizados. Link-Oriented es un caso especial.
			 */
			if (gen.getSolicitud().getEsquema() != EsquemaRestauracion.Link) {
				Camino alternativo = gen.getAlternativo();
				if (alternativo != null) {
					contadorInterno(alternativo.getEnlaces());
				}
			} else {
				if (gen.getAlternativoLink() != null) {
					for (Camino alternativo : gen.getAlternativoLink()) {
						if (alternativo != null) {
							contadorInterno(alternativo.getEnlaces());
						}
					}
				}
			}
		}

		// Fórmula de Costo de una Solución
		double costo = (contadorCosto * a) + (cambiosLDO * b);
		return costo;
	}

	/**
	 * Cuenta la cantidad de Enlaces y los cambios de longitud de onda de los
	 * enlaces obtenidos como parámetro. Se suman a los atributos locales
	 * contadorCosto y cambiosLDO.
	 */
	private void contadorInterno(Set<Enlace> enlaces) {
		// Si se utiliza el auxiliar debe definirse como variable global.
		// Set<CanalOptico> auxiliar = new HashSet<CanalOptico>();
		
		Enlace e1 = null;
		Enlace e2 = null;
		int ldo1 = 0;
		int ldo2 = 0;
		boolean primero = true;

		for (Enlace s : enlaces) {
			if (s == null)
				continue;
			CanalOptico ca = s.getCanal();

			if (primero) {
				e1 = s;
				primero = false;
			} else {
				e1 = e2;
			}
			ldo1 = e1.getLongitudDeOnda();
			e2 = s;
			ldo2 = e2.getLongitudDeOnda();

			// Si existe un cambio de longitud de onda, se suman en 1.
			if (ldo1 != ldo2)
				cambiosLDO = 0;

			// inserto es false cuando ya existía (no suma)
			// boolean inserto = auxiliar.add(ca);
			// se suman costos de Canales Opticos utilizados.
			// if (inserto)
			if (!enlacesContado.contains(s)) {
				enlacesContado.add(s);
				contadorCosto += ca.getCosto();
			}
		}

	}

	/**
	 * Función que compara la solucion con otra. Si los valores resultantes son
	 * 0, entonces las soluciones son iguales, si los valores resultantes son
	 * menores a 0, esta solucion es mejor; y si son mayores a 0 el parametro
	 * recibido es mejor. Las prioridades siguen el siguiente orden:
	 * Primario_Oro, Secundario_Oro, Primario_Plata, Secundario_Plata, Bronce.
	 * 
	 * @param s
	 * @return
	 */
	public boolean comparar(Individuo i) {
		Solucion s = (Solucion) i;
		boolean retorno = false;
		int oroP = this.contadorFailOroPrimario;
		oroP -= s.contadorFailOroPrimario;
		int oroA = this.contadorFailOroAlternativo;
		oroA -= s.contadorFailOroAlternativo;
		/*
		 * int plataP = this.contadorFailPlataPrimario; plataP -=
		 * s.contadorFailPlataPrimario; int plataA =
		 * this.contadorFailPlataAlternativo; plataA -=
		 * s.contadorFailPlataAlternativo; int bronce =
		 * this.contadorFailBroncePrimario; bronce -=
		 * s.contadorFailBroncePrimario;
		 */
		double costoResultante = this.costo - s.costo;

		if (oroP == 0) {
			if (oroA == 0) {
				if (costoResultante == 0) {
					retorno = false;
				} else {
					if (costoResultante > 0)
						retorno = true;
					else
						retorno = false;
				}
			} else {
				if (oroA > 0)
					retorno = true;
				else
					retorno = false;
			}
		} else {
			if (oroP > 0)
				retorno = true;
			else
				retorno = false;
		}

		return retorno;
	}

	public void imprimirCosto() {
		System.out.println("+ Costo: " + this.costo + "; FallasOroPrimario: "
				+ this.contadorFailOroPrimario + "; FallasOroAlternativo: "
				+ this.contadorFailOroAlternativo);
	}

	public String obtenerDetalleCosto() {
		String retorno = "_" + this.contadorCosto + "_" + this.cambiosLDO + "";
		return retorno;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Solucion other = (Solucion) obj;
		if (genes == null) {
			if (other.genes != null)
				return false;
		} else if (!genes.equals(other.genes))
			return false;
		return true;
	}

	@Override
	public String toString() {
		final int maxLen = genes.size();
		return "[Solucion(" + this.id + "):\n [fitness=" + fitness + ", costo="
				+ costo + "(" + this.contadorCosto + "#" + this.cambiosLDO
				+ "), genes="
				+ (genes != null ? toString(genes, maxLen) : "Vacio.") + "]";
	}

	private String toString(Set<Servicio> collection, int maxLen) {
		StringBuilder builder = new StringBuilder();
		builder.append(" [\n");
		int i = 0;
		for (Iterator<Servicio> iterator = collection.iterator(); iterator
				.hasNext(); i++) {
			if (i > 0)
				builder.append(", \n");
			builder.append(iterator.next().toString());
		}
		builder.append("]");
		return builder.toString();
	}
}
