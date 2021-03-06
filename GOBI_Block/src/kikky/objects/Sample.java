package kikky.objects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeSet;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import dennis.GO.GOHandler;
import dennis.forKikky.Clustering;
import dennis.forKikky.KikkyNxMmapping;
import dennis.forKikky.MatesScoring;
import dennis.utility_manager.Species;
import kikky.analysis.Point_Info;

public class Sample {
	private Species species;
	private HashMap<String, Double> gene_data = new HashMap<>();
	private Point_Info pi;
	private String filter;
	private HashMap<String, Double> goterms = new HashMap<String, Double>();
	private int matessize = 0;

	private boolean all_info = false;

	public Sample(Species species, HashMap<String, Double> values, String filter) {
		this.species = species;
		if (filter.equals("all"))
			this.gene_data = values;
		else
			this.gene_data = generate_vals(values, filter);
		this.filter = filter;
	}

	private HashMap<String, Double> generate_vals(HashMap<String, Double> values, String filter) {
		HashMap<String, Double> vals = new HashMap<>();
		for (String key : values.keySet()) {
			HashMap<String, LinkedList<String>> all_gos = GOHandler.getAllMappedGOs(null, key);
			TreeSet<String> gos = new TreeSet<>();
			for (String key2 : all_gos.keySet())
				gos.addAll(all_gos.get(key2));
			if (gos != null && gos.contains(filter))
				vals.put(key, values.get(key));
		}
		return vals;
	}

	public int get_species_ID() {
		return species.getId();
	}

	public String get_species_name() {
		return species.getName();
	}

	public Point_Info get_point_info() {
		return pi;
	}

	public Species get_species() {
		return species;
	}

	public HashMap<String, Double> get_gene_data() {
		return gene_data;
	}

	public void set_info(boolean info) {
		all_info = info;
	}

	public String gos_asString() {
		if (goterms.size() < 1)
			return "";
		StringBuilder sb = new StringBuilder();
		for (String go : goterms.keySet())
			sb.append(",").append(go).append("\t").append(goterms.get(go));
		return sb.substring(1);
	}

	public String get_species_info() {
		return species.getId() + "\t" + species.getName() + "\t" + species.getGtf() + "\t" + species.getChrs();
	}

	public double get_mates_value(HashMap<String, String> mates, HashMap<String, Double> partner) {
		PearsonsCorrelation pc = new PearsonsCorrelation();
		double[] x = new double[mates.size()];
		double[] y = new double[mates.size()];
		int index = 0;
		StringBuilder x_asString = new StringBuilder(), y_asString = new StringBuilder(), x_genes = new StringBuilder(),
				y_genes = new StringBuilder();
		matessize = mates.size();
		if (mates.size() > 0) {
			HashMap<String, Double> tmp = new HashMap<>();
			for (String gene_id_x : mates.keySet()) {
				x[index] = this.gene_data.get(gene_id_x);
				y[index] = partner.get(mates.get(gene_id_x));
				if (filter.equals("all")) {
					HashMap<String, LinkedList<String>> all = GOHandler.getAllMappedGOs(null, gene_id_x);
					if (all != null)
						for (String key : all.keySet())
							for (String g : all.get(key)) {
								if (!tmp.containsKey(g))
									tmp.put(g, 0.0);
								tmp.put(g, tmp.get(g) + 1);
							}
					all = GOHandler.getAllMappedGOs(null, mates.get(gene_id_x));
					if (all != null)
						for (String key : all.keySet())
							for (String g : all.get(key)) {
								if (!tmp.containsKey(g))
									tmp.put(g, 0.0);
								tmp.put(g, tmp.get(g) + 1);
							}
				}
				if (all_info) {
					x_asString.append(",").append(x[index]);
					y_asString.append(",").append(y[index]);
					x_genes.append(",").append(gene_id_x);
					y_genes.append(",").append(mates.get(gene_id_x));
				}
				index++;
			}
			if (filter.equals("all")) {
				for (String go : tmp.keySet())
					if (tmp.get(go) < (mates.size() / 2))
						goterms.put(go, tmp.get(go));
			}
			pi = new Point_Info(mates, x, y);
			if (all_info) {
				pi.scatter_plot(x_asString.substring(1), y_asString.substring(1), x_genes.substring(1),
						y_genes.substring(1));
				pi.percentage_mates_to_all(this.gene_data.size(), partner.size());
			}
			if (x.length > 1)
				return pc.correlation(x, y);
			else
				return ((x[0] + y[0]) / 2);
		}
		pi = new Point_Info(mates, x, y);
		return 0;
	}

	public HashMap<String, String> get_mates(Sample fs) {
		HashMap<String, String> mates = new HashMap<>();
		if (species.getId() == fs.get_species_ID()) {
			for (String gene_id : gene_data.keySet())
				if (fs.get_gene_data().containsKey(gene_id))
					mates.put(gene_id, gene_id);
		} else {
			HashSet<String> allowed_geneids = new HashSet<String>();
			allowed_geneids.addAll(gene_data.keySet());
			allowed_geneids.addAll(fs.get_gene_data().keySet());
			Clustering cl = new Clustering(species, fs.get_species(), allowed_geneids);
			LinkedList<KikkyNxMmapping> cluster = cl.getNxMmappings();
			mates = MatesScoring.greedy_score(cluster);
		}
		return mates;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + species.getId();
		result = prime * result + ((gene_data == null) ? 0 : gene_data.hashCode());
		return result;
	}
}
