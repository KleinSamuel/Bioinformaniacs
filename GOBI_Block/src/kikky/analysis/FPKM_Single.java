package kikky.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import dennis.counter.CounterUtils;
import dennis.forKikky.Clustering;
import dennis.forKikky.KikkyNxMmapping;
import dennis.forKikky.MatesScoring;
import dennis.tissues.Tissue;
import dennis.utility_manager.Species;
import kikky.heatmap.Sample_Data;

public class FPKM_Single extends Sample implements Sample_Data {

	private Tissue tissue;
	private String exp_number;

	public FPKM_Single(Species species, Tissue tissue, String exp_number, HashMap<String, Double> fpkm, String filter) {
		super(species, fpkm, filter);
		this.tissue = tissue;
		this.exp_number = exp_number;
	}

	public FPKM_Single(Species species, Tissue tissue, String exp_number, String fpkm_file, String filter) {
		super(species, CounterUtils.readCountFile(fpkm_file, false, false, false, false), filter);
		this.tissue = tissue;
		this.exp_number = exp_number;
	}

	@Override
	public String get_name() {
		return get_species_name() + "|" + tissue.getName() + "|" + exp_number;
	}

	public Tissue get_tissue() {
		return tissue;
	}

	public String get_experiment() {
		return exp_number;
	}

	public String get_init_info() {
		return get_species_info() + "\t" + tissue + "\t" + exp_number;
	}

	@Override
	public double get_value(Sample_Data sd) {
		FPKM_Single fs = (FPKM_Single) sd;
		HashMap<String, String> mates = get_mates((Sample)fs);
		return get_mates_value(mates, fs.get_gene_data());
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
		if (this.get_species().equals(other.get_species()) && this.tissue.equals(other.tissue)
				&& this.exp_number == other.exp_number)
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + super.hashCode();
		result = prime * result + ((tissue == null) ? 0 : tissue.hashCode());
		result = prime * result + exp_number.hashCode();
		return result;
	}

}
