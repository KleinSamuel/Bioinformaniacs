package kikky.analysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import kikky.heatmap.Sample_Data;

public class FPKM_Single implements Sample_Data {
	private int organism_ID;
	private String organism_name;
	private String tissue;
	private int exp_number;
	private HashMap<String, Double> gene_data = new HashMap<>();

	public FPKM_Single(int organism_id, String organism_name, String tissue, int exp_number) {
		this.organism_ID = organism_id;
		this.organism_name = organism_name;
		this.tissue = tissue;
		this.exp_number = exp_number;

	}

	public FPKM_Single(int organism_id, String organism_name, String tissue, int exp_number,
			HashMap<String, Double> gene_rawcount, String gene_file) {
		this(organism_id, organism_name, tissue, exp_number);
		Calculator.FPKM_generator(gene_rawcount, gene_file);
	}

	public FPKM_Single(int organism_id, String organism_name, String tissue, int exp_number, String gene_rawcount,
			String gene_file) {
		this(organism_id, organism_name, tissue, exp_number);
		try {
			BufferedReader br = new BufferedReader(new FileReader(gene_rawcount));
			String line;
			while ((line = br.readLine()) != null) {
				String[] split = line.split("\t");
				gene_data.put(split[0], Double.parseDouble(split[1]));
			}
			br.close();
			Calculator.FPKM_generator(gene_rawcount, gene_file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String get_Name() {
		return organism_name + "|" + tissue + "|" + exp_number;
	}

	@Override
	public double get_value(Sample_Data sd) {
		FPKM_Single fs = (FPKM_Single) sd;
		HashMap<String, String> mates = get_mates(fs);
		PearsonsCorrelation pc = new PearsonsCorrelation();
		double[] x = new double[mates.size()];
		double[] y = new double[mates.size()];
		int index = 0;
		for (String gene_id_x : mates.keySet()) {
			x[index] = this.gene_data.get(gene_id_x);
			y[index] = fs.gene_data.get(mates.get(gene_id_x));
		}
		return pc.correlation(x, y);
	}

	private HashMap<String, String> get_mates(FPKM_Single fs) {
		HashMap<String, String> mates = new HashMap<>();
		if (this.organism_ID == fs.organism_ID) {
			for (String gene_id : gene_data.keySet())
				if (fs.gene_data.containsKey(gene_id))
					mates.put(gene_id, gene_id);
		} else {
			// TODO: get ortholog partner if exists
		}
		return mates;
	}

	@Override
	public int compareTo(Sample_Data o) {
		return this.hashCode() - o.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FPKM_Single other = (FPKM_Single) obj;
		if (this.organism_ID == other.organism_ID && this.organism_name.equals(other.organism_name)
				&& this.tissue.equals(other.tissue) && this.exp_number == other.exp_number)
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + organism_ID;
		result = prime * result + ((tissue == null) ? 0 : tissue.hashCode());
		result = prime * result + exp_number;
		result = prime * result + ((gene_data == null) ? 0 : gene_data.hashCode());
		return result;
	}

}
