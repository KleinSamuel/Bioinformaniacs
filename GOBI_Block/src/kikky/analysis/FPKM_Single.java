package kikky.analysis;

import java.util.HashMap;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

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

	public FPKM_Single(Species species, String tissue, String exp_number) {
		this.species = species;
		this.tissue = tissue;
		this.exp_number = exp_number;

	}

	public FPKM_Single(Species species, String tissue, String exp_number, HashMap<String, Double> gene_rawcount,
			String gene_file) {
		this(species, tissue, exp_number);
		gene_data = Calculator.FPKM_generator(gene_rawcount, gene_file);
	}

	public FPKM_Single(Species species, String tissue, String exp_number, String gene_rawcount, String gene_file) {
		this(species, tissue, exp_number);
		gene_data = Calculator.FPKM_generator(gene_rawcount, gene_file);
	}

	@Override
	public String get_Name() {
		return species.getName() + "|" + tissue + "|" + exp_number;
	}

	public int get_organism_ID() {
		return species.getId();
	}

	public String get_tissue() {
		return tissue;
	}

	public String get_experiment() {
		return exp_number;
	}

	@Override
	public double get_value(Sample_Data sd) {
		FPKM_Single fs = (FPKM_Single) sd;
		HashMap<String, String> mates = get_mates(fs);
		PearsonsCorrelation pc = new PearsonsCorrelation();
		double[] x = new double[mates.size()];
		double[] y = new double[mates.size()];
		int index = 0;
		if (mates.size() > 0) {
			for (String gene_id_x : mates.keySet()) {
				x[index] = this.gene_data.get(gene_id_x);
				y[index] = fs.gene_data.get(mates.get(gene_id_x));
				index++;
			}
			return pc.correlation(x, y);
		}
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
			for (String gene_id : gene_data.keySet()) {
				SimilarityObject so = sh.checkForHighestSimilarity(this.species, fs.species, gene_id,
						fs.gene_data.keySet());
				if (so != null)
					mates.put(gene_id, so.getTarget_geneId());
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

}
