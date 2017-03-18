package kikky.analysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import kikky.heatmap.Sample_Data;

public class DE_Single implements Sample_Data {
	private int organism_ID;
	private String organism_name;
	private String tissue;
	private HashMap<String, Double> gene_data = new HashMap<>();

	public DE_Single(int organism_id, String organism_name, String tissue) {
		this.organism_ID = organism_id;
		this.organism_name = organism_name;
		this.tissue = tissue;
	}

	public DE_Single(int organism_id, String organism_name, String tissue, HashMap<String, Double> gene_rawcount,
			String gene_file) {
		this(organism_id, organism_name, tissue);
		Calculator.FPKM_generator(gene_rawcount, gene_file);
	}

	public DE_Single(int organism_id, String organism_name, String tissue, String gene_rawcount, String gene_file) {
		this(organism_id, organism_name, tissue);
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
		return organism_name + "|" + tissue;
	}

	@Override
	public double get_value(Sample_Data sd) {
		DE_Single fs = (DE_Single) sd;
		// TODO: Real calculation
		return 0;
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
		DE_Single other = (DE_Single) obj;
		if (this.organism_ID == other.organism_ID && this.organism_name.equals(other.organism_name)
				&& this.tissue.equals(other.tissue))
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + organism_ID;
		result = prime * result + ((tissue == null) ? 0 : tissue.hashCode());
		result = prime * result + ((gene_data == null) ? 0 : gene_data.hashCode());
		return result;
	}

}
