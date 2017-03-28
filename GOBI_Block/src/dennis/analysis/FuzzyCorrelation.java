package dennis.analysis;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import dennis.enrichment.EnrichmentAnalysisUtils;
import dennis.enrichment.GeneObject;
import dennis.fuzzy.Fuzzy;
import dennis.tissues.Tissue;
import dennis.tissues.TissueHandler;
import dennis.tissues.TissuePair;
import dennis.util.GenePair;
import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;
import javafx.util.Pair;

public class FuzzyCorrelation extends ScoringFunction {

	public static void main(String[] args) {
		LinkedList<TissuePair> s = new LinkedList<>(), t = new LinkedList<>();
		Tissue a = new Tissue("a");
		Tissue b = new Tissue("b");
		Tissue c = new Tissue("c");
		Tissue d = new Tissue("d");
		Tissue e = new Tissue("e");

		s.add(new TissuePair(a, b));
		s.add(new TissuePair(a, c));
		s.add(new TissuePair(a, d));
		s.add(new TissuePair(a, b));
		s.add(new TissuePair(b, c));
		s.add(new TissuePair(b, d));
		s.add(new TissuePair(c, d));
		t.add(new TissuePair(a, b));
		t.add(new TissuePair(a, c));
		t.add(new TissuePair(e, d));
		t.add(new TissuePair(e, b));
		t.add(new TissuePair(b, c));
		t.add(new TissuePair(b, d));
		t.add(new TissuePair(c, d));

		TreeSet<TissuePair> tree = new TreeSet<>(s);
		tree.retainAll(t);

		for (TissuePair st : tree)
			System.out.println(st.getKey() + "\t" + st.getValue());
	}

	private TreeMap<TissuePair, TreeMap<String, GeneObject>> deFilesSpecies1 = null, deFilesSpecies2 = null;
	private TreeSet<TissuePair> tissuePairs = null;
	private String mapper, deMethod;
	private Fuzzy fuzzyCalculator;

	public FuzzyCorrelation(Species query_species, Species target_species, String mapper, String deMethod) {
		super(query_species, target_species);
		this.mapper = mapper;
		this.deMethod = deMethod;
		fuzzyCalculator = new Fuzzy();
		tissuePairs = new TreeSet<>(TissueHandler.tissuePairIterator(query_species));
		tissuePairs.retainAll(TissueHandler.tissuePairIterator(target_species));
		deFilesSpecies1 = new TreeMap<>();
		deFilesSpecies2 = new TreeMap<>();
		readDEfiles();
	}

	public void readDEfiles() {
		for (TissuePair tp : tissuePairs) {
			TreeMap<String, GeneObject> in = new TreeMap<>();
			for (GeneObject go : EnrichmentAnalysisUtils
					.readDEfile(UtilityManager.getConfig("enrichment_output") + getQuerySpecies().getId() + "/"
							+ tp.toString() + "/" + mapper + "/" + tp.toString() + "." + deMethod)) {
				in.put(go.getName(), go);
			}

			deFilesSpecies1.put(tp, in);

			in = new TreeMap<>();

			for (GeneObject go : EnrichmentAnalysisUtils
					.readDEfile(UtilityManager.getConfig("enrichment_output") + getQuerySpecies().getId() + "/"
							+ tp.toString() + "/" + mapper + "/" + tp.toString() + "." + deMethod)) {
				in.put(go.getName(), go);
			}

			deFilesSpecies2.put(tp, in);
		}
	}

	public double[] getFuzzyArr(TreeMap<TissuePair, TreeMap<String, GeneObject>> deFiles, TissuePair tp,
			String geneId) {
		GeneObject ob = deFiles.get(tp).get(geneId);
		return fuzzyCalculator.getFuzzyArray(ob.getLog2fc(), ob.getAdj_pval());
	}

	public Pair<double[], double[]> getFuzzyValues(TissuePair tp, GenePair gp) {
		return new Pair<double[], double[]>(getFuzzyArr(deFilesSpecies1, tp, gp.getKey()),
				getFuzzyArr(deFilesSpecies2, tp, gp.getValue()));
	}

	public double correlation(GenePair gp) {
		ArrayList<Pair<double[], double[]>> fuzzyValues = new ArrayList<>(tissuePairs.size());

		for (TissuePair tp : tissuePairs) {
			fuzzyValues.add(getFuzzyValues(tp, gp));
		}

		// number of categories
		int size = fuzzyCalculator.getCategories().size();

		double[] corrInputGene1 = new double[fuzzyValues.size() * size],
				corrInputGene2 = new double[fuzzyValues.size() * size];

		for (int i = 0; i < fuzzyValues.size(); i++) {
			for (int j = 0; j < fuzzyCalculator.getCategories().size(); j++) {
				corrInputGene1[i * size + j] = fuzzyValues.get(i).getKey()[j];
				corrInputGene2[i * size + j] = fuzzyValues.get(i).getValue()[j];
			}
		}

		PearsonsCorrelation ps = new PearsonsCorrelation();

		double corr = ps.correlation(corrInputGene1, corrInputGene2);

		return corr;
	}

	@Override
	public double score(GenePair genePair) {
		return correlation(genePair);
	}

}
