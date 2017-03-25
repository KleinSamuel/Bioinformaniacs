package kikky.analysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import dennis.forKikky.Clustering;
import dennis.forKikky.KikkyNxMmapping;
import dennis.forKikky.MatesScoring;
import dennis.tissues.TissuePair;
import dennis.utility_manager.Species;
import kikky.heatmap.Sample_Data;

public class DE_Pairs implements Sample_Data {
	private Species species;
	private TissuePair tissues;
	private HashMap<String, Double> gene_data = new HashMap<>();

	private Point_Info pi;

	public DE_Pairs(Species species, TissuePair tissues) {
		this.species = species;
		this.tissues = tissues;
	}

	public DE_Pairs(Species species, TissuePair tissues, HashMap<String, Double> gene_fc) {
		this(species, tissues);
		this.gene_data = gene_fc;
	}

	public DE_Pairs(Species species, TissuePair tissues, String gene_after_de) {
		this(species, tissues);
		try {
			BufferedReader br = new BufferedReader(new FileReader(gene_after_de));
			String line = br.readLine();
			while ((line = br.readLine()) != null) {
				String[] split = line.split("\t");
				if (Double.parseDouble(split[3]) < 0.05) {
					gene_data.put(split[0], Double.parseDouble(split[1]));
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String get_name() {
		return species.getName() + "|" + tissues.getKey() + "-" + tissues.getValue();
	}

	public Species get_species() {
		return species;
	}

	@Override
	public double get_value(Sample_Data sd) {
		DE_Pairs fs = (DE_Pairs) sd;
		HashMap<String, String> mates = get_mates(fs);
		PearsonsCorrelation pc = new PearsonsCorrelation();
		double[] x = new double[mates.size()];
		double[] y = new double[mates.size()];
		int index = 0;
		StringBuilder x_asString = new StringBuilder(), y_asString = new StringBuilder(), x_genes = new StringBuilder(),
				y_genes = new StringBuilder();
		if (mates.size() > 0) {
			for (String gene_id_x : mates.keySet()) {
				x[index] = this.gene_data.get(gene_id_x);
				y[index] = fs.gene_data.get(mates.get(gene_id_x));
				x_asString.append(",").append(x[index]);
				y_asString.append(",").append(y[index]);
				x_genes.append(",").append(gene_id_x);
				y_genes.append(",").append(mates.get(gene_id_x));
				index++;
			}
			pi = new Point_Info(mates, x, y);
			pi.scatter_plot(x_asString.substring(1), y_asString.substring(1), x_genes.substring(1),
					y_genes.substring(1));
			pi.percentage_mates_to_all(this.gene_data.size(), fs.gene_data.size());
			return pc.correlation(x, y);
		}
		pi = new Point_Info(mates, x, y);
		return 0;
	}

	private HashMap<String, String> get_mates(DE_Pairs fs) {
		HashMap<String, String> mates = new HashMap<>();
		if (this.species.getId() == fs.species.getId()) {
			for (String gene_id : this.gene_data.keySet())
				if (fs.gene_data.containsKey(gene_id))
					mates.put(gene_id, gene_id);
		} else {
			HashSet<String> allowed_geneids = new HashSet<String>();
			allowed_geneids.addAll(this.gene_data.keySet());
			allowed_geneids.addAll(fs.gene_data.keySet());
			Clustering cl = new Clustering(this.species, fs.species, allowed_geneids);
			LinkedList<KikkyNxMmapping> cluster = cl.getNxMmappings();
			mates = MatesScoring.greedy_score(cluster);
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
		DE_Pairs other = (DE_Pairs) obj;
		if (this.species.equals(other.species) && this.tissues.getKey().equals(other.tissues.getKey())
				&& this.tissues.getValue().equals(other.tissues.getValue()))
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + species.getId();
		result = prime * result + ((tissues == null) ? 0 : tissues.getKey().hashCode());
		result = prime * result + ((tissues == null) ? 0 : tissues.getValue().hashCode());
		result = prime * result + ((gene_data == null) ? 0 : gene_data.hashCode());
		return result;
	}

	@Override
	public Point_Info get_point_info() {
		return pi;
	}

	public String get_init_info() {
		return species.getId() + "\t" + species.getName() + "\t" + species.getGtf() + "\t" + species.getChrs() + "\t"
				+ tissues.getKey() + "\t" + tissues.getValue() + "\t" + get_file_path();
	}

	public String get_file_path() {
		return "/home/proj/biocluster/praktikum/genprakt-ws16/bioinformaniacs/EB/" + species.getId() + "/"
				+ tissues.getKey() + "_" + tissues.getValue() + "/star/" + tissues.getKey() + "_" + tissues.getValue()
				+ ".DESeq";
	}

}
