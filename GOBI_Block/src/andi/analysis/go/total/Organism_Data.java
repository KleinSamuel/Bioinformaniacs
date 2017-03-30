package andi.analysis.go.total;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import andi.analysis.go.total.Organism_Data.Distance_measurement;
import andi.tree.Node_Data;
import dennis.GO.GOHandler;
import dennis.counter.CounterUtils;
import dennis.similarities.SimilarityObject;
import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;

public class Organism_Data implements Node_Data {

	public enum Distance_measurement {
		Avg_seq_id_max, Avg_seq_id_all, GO_tissue_basic, GO_tissue_xgsa, DE_count;
	};

	public enum Gene_focus {
		All_genes, orthologues_only, nonorthologues_only, de_only, nonde_only;
	};

	private ArrayList<String> all_orthologues;
	private TreeSet<String> orthologues_self;
	private TreeSet<String> orthologues_paired;
	private String org_name;
	private int id;
	private Vector<Boolean> shared;
	private Species org;
	private String tissue;
	private String count_file;
	private HashMap<String, Double> counts;
	private TreeMap<String, Integer> go_counts;
	private Distance_measurement dm = Distance_measurement.Avg_seq_id_max;
	private Gene_focus gf = Gene_focus.All_genes;
	private int go_comparison_top = 20;
	private boolean all_gos_to_root = false;

	public Organism_Data(int id, String name, Species org) {
		this.id = id;
		this.org_name = name;
		this.org = org;
		init();
	}

	public Organism_Data(int id, String name, Species org, String tissue, String count_file, Distance_measurement dm) {
		this.id = id;
		this.org_name = name;
		this.org = org;
		this.tissue = tissue;
		this.dm = dm;
		this.count_file = count_file;
		init();
	}

	private void init() {
		init_dm();
	}

	private void init_dm() {
		switch (dm) {
		case GO_tissue_basic:
			counts = CounterUtils.readCountFile(count_file, false, false, true, true);
			break;
		case GO_tissue_xgsa:

			break;
		case DE_count:
			counts = CounterUtils.readCountFile(count_file, false, false, true, true);
			break;
		default:
			break;
		}
	}

	private void count_gos(Organism_Data other) {
		go_counts = new TreeMap<>();
		for (String gene : counts.keySet()) {
			if (!other.is_relevant_gene(orthologue(gene,other.get_Species())))
				continue;
			if (!all_gos_to_root) {
				TreeSet<String> goterms = GOHandler.getMappedGOterms(this.get_Species(), gene);
				if (goterms != null)
					for (String go_term : goterms) {
						int count = counts.get(gene).intValue();
						if (go_counts.containsKey(go_term))
							count += go_counts.get(go_term);
						go_counts.put(go_term, count);
					}
			} else {
				HashMap<String, LinkedList<String>> go_terms = GOHandler.getAllMappedGOs(this.get_Species(), gene);
				if (go_terms != null)
					for (String go_term : go_terms.keySet()) {
						for (String terms : go_terms.get(go_term)) {
							int count = counts.get(gene).intValue();
							if (go_counts.containsKey(terms))
								count += go_counts.get(terms);
							go_counts.put(terms, count);
						}
					}

			}
		}
	}

	public void set_all_go_terms(boolean b) {
		if (all_gos_to_root != b)
			go_counts = null;
		all_gos_to_root = b;
	}
	
	public boolean is_all_go_terms() {
		return all_gos_to_root;
	}

	public void set_all_orthologues(Collection<String> all) {
		all_orthologues = new ArrayList<>();
		all_orthologues.addAll(all);
	}

	public boolean is_relevant_gene(String g) {
		switch (gf) {
		case orthologues_only:
			if (g.equals("")||!orthologues_self.contains(g))
				return false;
			return true;
		case nonorthologues_only:
			if (!g.equals("")||orthologues_self.contains(g))
				return false;
			return true;
		case de_only:
			if (counts != null && counts.containsKey(g))
				return false;
			return true;
		case nonde_only:
			if (counts != null && counts.containsKey(g))
				return true;
			return false;
		default:
			return true;
		}
	}
	
	public String orthologue(String g, Species other) {
		String ortho = UtilityManager.getSimilarityHandler().getSimilarities(this.get_Species(), other).getGeneWithHighestIdentity(g);
		return ortho==null ? "" : ortho;
	}

	public void set_own_genes(Collection<String> genes) {
		orthologues_self = new TreeSet<>();
		orthologues_self.addAll(genes);
	}

	public void set_orthologue_genes(Collection<String> genes) {
		orthologues_paired = new TreeSet<>();
		orthologues_paired.addAll(genes);
	}

	public void set_shared_vector(Vector<Boolean> shared) {
		this.shared = shared;
	}

	public Vector<String> get_top_x_gos(int top, Organism_Data other) {
		if (go_counts == null | (gf == Gene_focus.de_only | gf == Gene_focus.nonde_only))
			count_gos(other);
		Vector<String> top_x = new Vector<>();
		TreeSet<String> rest = new TreeSet<>();
		rest.addAll(go_counts.keySet());

		while (top_x.size() < top && !rest.isEmpty()) {
			int max = 0;
			String term = "";
			for (String go_term : rest) {
				if (max < go_counts.get(go_term)) {
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
		if (!(o instanceof Organism_Data))
			return -1;
		Organism_Data other = (Organism_Data) o;
		return this.id - other.id;
	}

	public void set_distance_measurement(Distance_measurement dm) {
		if (this.dm != dm) {
			this.dm = dm;
			init_dm();
		} else
			this.dm = dm;
	}

	public void set_gene_focus(Gene_focus gf) {
		if (gf != this.gf)
			go_counts = null;
		this.gf = gf;
	}

	public TreeMap<String, Integer> get_go_counts() {
		return go_counts;
	}

	@Override
	public double compute_distance(Node_Data nd) {
		if (nd == null || !(nd instanceof Organism_Data))
			return Double.MAX_VALUE;
		Organism_Data other = (Organism_Data) nd;
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

			return evaluate(this.get_top_x_gos(go_comparison_top,other), other.get_top_x_gos(go_comparison_top,this), other);
		case GO_tissue_xgsa:

			return 0;
		case DE_count:
			return evaluate(this.get_counts(), other.get_counts(), other);
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

	private double evaluate(HashMap<String, Double> counts_this, HashMap<String, Double> counts_other,
			Organism_Data other) {
		double dist = 0;
		for (String this_g : counts_this.keySet()) {
			String orthologue = this.orthologue(this_g,other.get_Species());
			if (!counts_other.containsKey(orthologue) & other.is_relevant_gene(orthologue)) {
				dist += 1;
			}
		}
		for (String other_g : counts_other.keySet()) {
			String orthologue = other.orthologue(other_g,this.get_Species());
			if (!counts_this.containsKey(orthologue) & this.is_relevant_gene(orthologue))
				dist += 1;
		}
		return dist / (counts_this.size()+counts_other.size()) ;
	}

	public HashMap<String, Double> get_counts() {
		if (counts == null)
			init();
		return counts;
	}

	public String get_tissue() {
		return tissue;
	}

	private double evaluate(Vector<String> this_top, Vector<String> other_top, Organism_Data other) {
		double dist = 0;

		Iterator<String> it_this = this_top.iterator();
		Iterator<String> it_other = other_top.iterator();
		while (it_this.hasNext() && it_other.hasNext()) {
			String this_term = it_this.next();
			String other_term = it_other.next();
			if (!other_top.contains(this_term))
				dist += 1;
			if (!this_top.contains(other_term))
				dist += 1;
		}
		return dist / (go_comparison_top * 2);
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
		return data_title(gf, dm);
	}

	public String data_title(Gene_focus gf, Distance_measurement dm) {
		String gene_foc = " of ";
		switch (gf) {
		case nonorthologues_only:
			gene_foc += "all Non-Orthologues";
			break;
		case orthologues_only:
			gene_foc += "all Orthologues";
			break;
		case de_only:
			gene_foc += "all pairwise-DE-Genes";
			break;
		case nonde_only:
			gene_foc += "all pairwise-NonDE-Genes";
			break;
		default:
			gene_foc += "all Genes";
			break;
		}
		switch (dm) {
		case Avg_seq_id_all:
			return "All possible Orthologue Pairs";
		case GO_tissue_basic:
			return "GO similarity in " + tissue + gene_foc;
		case GO_tissue_xgsa:
			return "GO similarity in " + tissue + gene_foc;
		case DE_count:
			return "DE-Difference in " + tissue + gene_foc;
		default:
			return "Highest identity Othologue Pairs";
		}
	}
	
	public String get_characteristic() {
		return tissue;
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
		return get_distance_measurement(dm);
	}
	
	

	public String get_distance_measurement(Distance_measurement dm2) {
		switch (dm2) {
		case Avg_seq_id_all:
			return "1 - Average Sequence Identity";
		case GO_tissue_basic:
			return "GSE-Difference Top " + go_comparison_top + " of " + (all_gos_to_root ? "all" : "most specific")
					+ " GO-Terms";
		case GO_tissue_xgsa:
			return "GSE_Difference using XGSA Top " + go_comparison_top + " terms";
		case DE_count:
			return "Count of Pairwise DE-Genes / All Expressed Genes";
		default:
			return "1 - Average Sequence Identity";
		}
	}

}
