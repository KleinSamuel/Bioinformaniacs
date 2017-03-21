package kikky.analysis;

import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import dennis.counter.CounterUtils;
import dennis.similarities.SimilarityHandler;
import dennis.similarities.SimilarityObject;
import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;
import kikky.heatmap.Sample_Data;

public class FPKM_Single implements Sample_Data {
	private Species species;
	private String tissue;
	private String exp_number;
	private HashMap<String, Double> gene_data = new HashMap<>();
	private Point_Info pi;

	private long start;

	public FPKM_Single(Species species, String tissue, String exp_number) {
		this.species = species;
		this.tissue = tissue;
		this.exp_number = exp_number;

	}

	public FPKM_Single(Species species, String tissue, String exp_number, HashMap<String, Double> fpkm) {
		this(species, tissue, exp_number);
		gene_data = fpkm;
	}

	public FPKM_Single(Species species, String tissue, String exp_number, String fpkm_file) {
		this(species, tissue, exp_number);
		gene_data = CounterUtils.readCountFile(fpkm_file, false, false, false, false);
	}

	@Override
	public String get_name() {
		return species.getName() + "|" + tissue + "|" + exp_number;
	}

	public int get_organism_ID() {
		return species.getId();
	}

	public String get_organism_name() {
		return species.getName();
	}

	public String get_tissue() {
		return tissue;
	}

	public String get_experiment() {
		return exp_number;
	}

	public Point_Info get_point_info() {
		return pi;
	}

	public String get_init_info() {
		return species.getId() + "\t" + species.getName() + "\t" + species.getGtf() + "\t" + species.getChrs() + "\t"
				+ tissue + "\t" + exp_number;
	}

	@Override
	public double get_value(Sample_Data sd) {
		start = System.currentTimeMillis();
		FPKM_Single fs = (FPKM_Single) sd;
		System.out.println(systemInfoString() + "Getting mates");
		HashMap<String, String> mates = get_mates(fs);
		System.out.println(systemInfoString() + "Calculate");
		PearsonsCorrelation pc = new PearsonsCorrelation();
		double[] x = new double[mates.size()];
		double[] y = new double[mates.size()];
		int index = 0;
		String x_asString = "", y_asString = "", query_genes = "", target_genes = "";
		if (mates.size() > 0) {
			for (String gene_id_x : mates.keySet()) {
				x[index] = this.gene_data.get(gene_id_x);
				y[index] = fs.gene_data.get(mates.get(gene_id_x));
				x_asString += "," + x[index];
				y_asString += "," + y[index];
				query_genes += "," + gene_id_x;
				target_genes += "," + mates.get(gene_id_x);
				index++;
			}
			System.out.println(systemInfoString() + "Save Infos");
			pi = new Point_Info(mates, x, y);
			pi.scatter_plot(x_asString.substring(1), y_asString.substring(1), query_genes.substring(1),
					target_genes.substring(1));
			pi.percentage_mates_to_all(this.gene_data.size(), fs.gene_data.size());
			System.out.println(systemInfoString() + "Calculate correlation");
			return pc.correlation(x, y);
		}
		pi = new Point_Info(mates, x, y);
		return 0;
	}

	private HashMap<String, String> get_mates(FPKM_Single fs) {
		HashMap<String, String> mates = new HashMap<>();
		if (this.species.getId() == fs.species.getId()) {
			for (String gene_id : this.gene_data.keySet())
				if (fs.gene_data.containsKey(gene_id))
					mates.put(gene_id, gene_id);
		} else {
			SimilarityHandler sh = UtilityManager.getSimilarityHandler();
			HashSet<String> query = sh.getAllGenesWithAnOrtholog(this.species, fs.species);
			for (String gene_id : query) {
				if (this.gene_data.containsKey(gene_id)) {
					HashSet<String> hs = new HashSet<>();
					hs.addAll(fs.gene_data.keySet());
					SimilarityObject so = sh.checkForHighestSimilarity(this.species, fs.species, gene_id, hs);
					if (so != null)
						mates.put(gene_id, so.getTarget_geneId());
				}
			}
		}
		return mates;
	}

	@Override
	public int compareTo(Sample_Data obj) {
		return this.hashCode() - obj.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FPKM_Single other = (FPKM_Single) obj;
		if (this.species.equals(other.species) && this.tissue.equals(other.tissue)
				&& this.exp_number == other.exp_number)
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + species.getId();
		result = prime * result + ((tissue == null) ? 0 : tissue.hashCode());
		result = prime * result + exp_number.hashCode();
		result = prime * result + ((gene_data == null) ? 0 : gene_data.hashCode());
		return result;
	}

	public String systemInfoString() {
		String out = "[";
		out += (System.currentTimeMillis() - start) / 1000 + "." + (System.currentTimeMillis() - start) % 1000 + "s";
		out += "|" + ((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024d / 1024d
				/ 1024d * 1000d)) / 1000d + "GB]";
		return out;
	}

}
