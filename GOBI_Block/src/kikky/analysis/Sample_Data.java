package kikky.analysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

public class Sample_Data {
	private int organism_ID;
	private String organism_name;
	private ArrayList<String> tissues;
	private int exp_number;
	private HashMap<String, Integer> gene_data = new HashMap<>();

	public Sample_Data(int organism_id, String organism_name, ArrayList<String> tissues, int exp_number,
			HashMap<String, Integer> gene_data) {
		this.organism_ID = organism_id;
		this.organism_name = organism_name;
		this.tissues = tissues;
		this.exp_number = exp_number;
		this.gene_data = gene_data;
	}

	public Sample_Data(int organism_id, String organism_name, ArrayList<String> tissues, int exp_number, String file) {
		try {
			this.organism_ID = organism_id;
			this.organism_name = organism_name;
			this.tissues = tissues;
			this.exp_number = exp_number;
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				String[] split = line.split("\t");
				gene_data.put(split[0], Integer.parseInt(split[1]));
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Sample_Data(int organism_id, String organism_name, ArrayList<String> tissues,
			HashMap<String, Integer> gene_data) {
		this(organism_id, organism_name, tissues, 0, gene_data);
	}

	public Sample_Data(int organism_id, String organism_name, String tissue, int exp_number,
			HashMap<String, Integer> gene_data) {
		this(organism_id, organism_name, new ArrayList<String>(Arrays.asList(tissue)), exp_number, gene_data);
	}

	public Sample_Data(int organism_id, String organism_name, String tissue, HashMap<String, Integer> gene_data) {
		this(organism_id, organism_name, new ArrayList<String>(Arrays.asList(tissue)), 0, gene_data);
	}

	public Sample_Data(int organism_id, String organism_name, ArrayList<String> tissues, String file) {
		this(organism_id, organism_name, tissues, 0, file);
	}

	public Sample_Data(int organism_id, String organism_name, String tissue, int exp_number, String file) {
		this(organism_id, organism_name, new ArrayList<String>(Arrays.asList(tissue)), exp_number, file);
	}

	public Sample_Data(int organism_id, String organism_name, String tissue, String file) {
		this(organism_id, organism_name, new ArrayList<String>(Arrays.asList(tissue)), 0, file);
	}

	public int getOrganism_ID() {
		return organism_ID;
	}

	public String getOrganism_name() {
		return organism_name;
	}

	public ArrayList<String> getTissues() {
		return tissues;
	}
	
	public String getTissue_names() {
		String tissue_names ="";
		for(String cur_tissue : tissues)
			tissue_names += " X " + cur_tissue;
		return tissue_names.substring(3);
	}
	
	public int getExp_number() {
		return exp_number;
	}

	public HashMap<String, Integer> getGene_data() {
		return gene_data;
	}

	public Set<String> getGenes() {
		return gene_data.keySet();
	}

	public int getGene_value(String gene_id) {
		return gene_data.get(gene_id);
	}

}
