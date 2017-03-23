package andi.analysis.go.total;

import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import andi.tree.Node_Data;
import dennis.GO.GOHandler;
import dennis.counter.CounterUtils;
import dennis.similarities.SimilarityObject;
import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;

public class Total_Organism implements Node_Data {

	public enum Distance_measurement {
		Avg_seq_id_max, Avg_seq_id_all, GO_tissue_basic, GO_tissue_xgsa;
	};

	private Vector<String> all_orthologues;
	private Vector<String> self_genes;
	private Vector<String> orthologue_genes;
	private String org_name;
	private int id;
	private Vector<Boolean> shared;
	private Species org;
	private String tissue;
	private String count_file;
	private HashMap<String, Double> counts;
	private TreeMap<String, Integer> go_counts;
	private Distance_measurement dm = Distance_measurement.Avg_seq_id_max;
	private int go_comparison_top = 10;

	public Total_Organism(int id, String name, Species org) {
		this.id = id;
		this.org_name = name;
		this.org = org;
		init();
	}
	

	public Total_Organism(int id, String name, Species org, String tissue, String count_file, Distance_measurement dm) {
		this.id = id;
		this.org_name = name;
		this.org = org;
		this.tissue = tissue;
		this.dm = dm;
		this.count_file = count_file;
		init();
	}

	private void init() {
		switch (dm) {
		case GO_tissue_basic:
			counts = CounterUtils.readCountFile(count_file, false, false, true, true);
			go_counts = new TreeMap<>();
			count_gos();
			break;
		case GO_tissue_xgsa:

			break;
		default:
			break;
		}

	}

	private void count_gos() {
		for (String gene : counts.keySet()) {
			for (String go_term : GOHandler.getMappedGOterms(this.get_Species(), gene)) {
				int count = counts.get(gene).intValue();
				if (go_counts.containsKey(go_term))
					count += go_counts.get(go_term);
				go_counts.put(go_term, count);
			}
		}
	}

	public void set_all_orthologues(Vector<String> all) {
		all_orthologues = all;
		Iterator<String> it_all = all.iterator();
		shared = new Vector<>();
		for (String g = ""; it_all.hasNext(); g = it_all.next())
			shared.add(self_genes.contains(g) | orthologue_genes.contains(g));
	}

	public void set_self_genes(Vector<String> genes) {
		this.self_genes = genes;
	}

	public void set_orthologue_genes(Vector<String> genes) {
		this.orthologue_genes = genes;
	}
	
	public Vector<String> get_top_x_gos(int top){
		Vector<String> top_x = new Vector<>();
		TreeSet<String> rest = new TreeSet<>();
		rest.addAll(go_counts.keySet());
		
		while(top_x.size()<top&&!top_x.isEmpty()) {
			int max = 0;
			String term = "";
			for(String go_term:rest) {
				if(max<go_counts.get(go_term)) {
					max = go_counts.get(go_term);
					term = go_term;
				}
			}
			top_x.add(term);
			rest.remove(term);
		}		
		return top_x;
	}

	@Override
	public int compareTo(Node_Data o) {
		if (!(o instanceof Total_Organism))
			return -1;
		Total_Organism other = (Total_Organism) o;
		return this.id - other.id;
	}

	public void set_distance_measurement(Distance_measurement dm) {
		this.dm = dm;
	}
	public TreeMap<String,Integer> get_go_counts(){
		return go_counts;
	}

	@Override
	public double compute_distance(Node_Data nd) {
		if (nd == null || !(nd instanceof Total_Organism))
			return Double.MAX_VALUE;
		Total_Organism other = (Total_Organism) nd;
		double sim_score = 0;
		double count = 0;
		switch (dm) {
		case Avg_seq_id_all:
			for (String gene : UtilityManager.getSimilarityHandler().getAllGenesWithAnOrtholog(this.get_Species(),
					other.get_Species())) {
				HashMap<String, SimilarityObject> sims = UtilityManager.getSimilarityHandler()
						.getSimilarities(this.get_Species(), other.get_Species()).getSimilarities(gene);
				for (SimilarityObject so : sims.values()) {
					sim_score += so.getMaximumIdentityScore();
					count++;
				}
			}
			return 1 - (sim_score / count);
		case GO_tissue_basic:
				
			return evaluate(this.get_top_x_gos(go_comparison_top),other.get_top_x_gos(go_comparison_top),other);

		case GO_tissue_xgsa:

			return 0;
		default:
			for (String gene : UtilityManager.getSimilarityHandler().getAllGenesWithAnOrtholog(this.get_Species(),
					other.get_Species())) {
				HashMap<String, SimilarityObject> sims = UtilityManager.getSimilarityHandler()
						.getSimilarities(this.get_Species(), other.get_Species()).getSimilarities(gene);
				for (SimilarityObject so : sims.values()) {
					sim_score += so.getMaximumIdentityScore();
					count++;
					break;
				}
			}
			return 1 - (sim_score / count);
		}
	}

	private double evaluate(Vector<String> this_top, Vector<String> other_top, Total_Organism other) {
		double dist = 0;
		Iterator<String> it_this = this_top.iterator();
		Iterator<String> it_other = other_top.iterator();
		while(it_this.hasNext()&&it_other.hasNext()) {
			String this_term = it_this.next();
			String other_term = it_other.next();
			if(!this_term.equals(other_term)) {
				if(other_top.contains(this_term))
				dist+=go_comparison_top/5;
				else dist+=go_comparison_top/2;
				if(this_top.contains(other_term))
					dist+=go_comparison_top/5;
				else
					dist+=go_comparison_top/2;
			}
		}
		return dist;
	}


	public Species get_Species() {
		return org;
	}

	@Override
	public String get_Name() {
		return org_name;
	}

	@Override
	public String data_title() {
		switch (dm) {
		case Avg_seq_id_all:
			return "All possible Orthologue Pairs";
		case GO_tissue_basic:
			return "GO similarity in " + tissue;
		case GO_tissue_xgsa:
			return "GO similarity in " + tissue;
		default:
			return "Highest identity Othologue Pairs";
		}
	}

	@Override
	public Vector<?> get_shared(Vector<Boolean> shared) {
		Iterator<Boolean> s = shared.iterator();
		Iterator<String> all = all_orthologues.iterator();
		Vector<String> out = new Vector<>();
		while (s.hasNext())
			if (s.next())
				out.add(all.next());
			else
				all.next();
		return out;
	}

	@Override
	public String shared_type() {
		return "Othologues";
	}

	@Override
	public Vector<Boolean> get_share_vector() {
		return shared;
	}

	public String toString() {
		return this.get_Name() + "(" + this.id + ")";
	}

	@Override
	public String get_distance_measurement() {
		switch (dm) {
		case Avg_seq_id_all:
			return "1 - Average Sequence Identity";
		case GO_tissue_basic:
			return "GSE-Difference Top "+go_comparison_top+" terms";
		case GO_tissue_xgsa:
			return "GSE_Difference using XGSA Top "+go_comparison_top+" terms";
		default:
			return "1 - Average Sequence Identity";
		}
	}

}
