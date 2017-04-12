package kikky.objects;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import dennis.tissues.Tissue;
import dennis.tissues.TissuePair;
import dennis.utility_manager.Species;
import kikky.heatmap.Sample_Data;

public class DE_Single extends Sample implements Sample_Data {
	private Tissue tissue;
	
	public DE_Single(Species species, Tissue tissue, HashMap<String, Double> gene_fc, String filter) {
		super(species, gene_fc, filter);
		this.tissue = tissue;
	}

	public DE_Single(Species species, Tissue tissue, String gene_after_de, String filter) {
		super(species, read_de(gene_after_de), filter);
		this.tissue = tissue;
	}

	public static HashMap<String, Double> read_de(String file) {
		HashMap<String, Double> gene_data = new HashMap<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			while ((line = br.readLine()) != null) {
				;
				String[] split = line.split("\t");
				if (!split[3].equals("NA"))
					if (Double.parseDouble(split[3]) < 0.05)
						gene_data.put(split[0], Double.parseDouble(split[1]));
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return gene_data;
	}

	public Tissue get_tissue() {
		return tissue;
	}

	@Override
	public String get_name() {
		return get_species_name() + "|" + tissue.getName();
	}

	@Override
	public double get_value(Sample_Data sd) {
		DE_Single fs = (DE_Single) sd;
		HashMap<String, String> mates = get_mates((Sample) fs);
		return get_mates_value(mates, fs.get_gene_data());
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
		if (this.get_species().equals(other.get_species()) && this.tissue.equals(other.tissue))
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + super.hashCode();
		result = prime * result + ((tissue == null) ? 0 : tissue.getName().hashCode());
		return result;
	}

	public String get_init_info() {
		return super.get_species_info() + "\t" + tissue.getName() + "\t" + get_file_path();
	}

	public String get_file_path() {
		return "/home/proj/biocluster/praktikum/genprakt-ws16/bioinformaniacs/data_update/" + get_species_ID() + "/"
				+ tissue.getName() + "/vsTissuemix.DESeq";
	}

}
