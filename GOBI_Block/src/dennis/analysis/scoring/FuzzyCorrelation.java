package dennis.analysis.scoring;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import dennis.analysis.BipartiteMatching;
import dennis.analysis.HungarianAlgorithm;
import dennis.enrichment.GeneObject;
import dennis.similarities.NxMmapping;
import dennis.tissues.TissueHandler;
import dennis.tissues.TissuePair;
import dennis.util.GenePair;
import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;
import javafx.util.Pair;

public class FuzzyCorrelation extends ScoringFunction {

	public static final String NAME = "fuzzy";

	private TreeMap<TissuePair, TreeMap<String, GeneObject>> deFilesSpecies1 = null, deFilesSpecies2 = null;
	private TreeSet<TissuePair> tissuePairs = null;
	private Fuzzy fuzzyCalculator;
	private PearsonsCorrelation ps;

	public FuzzyCorrelation(Species query_species, Species target_species,
			TreeMap<TissuePair, TreeMap<String, GeneObject>> deFilesQuerySpecies,
			TreeMap<TissuePair, TreeMap<String, GeneObject>> deFilesTargetSpecies) {
		super(query_species, target_species);
		fuzzyCalculator = new Fuzzy();
		tissuePairs = new TreeSet<>(TissueHandler.tissuePairIterator(query_species));
		tissuePairs.retainAll(TissueHandler.tissuePairIterator(target_species));
		deFilesSpecies1 = deFilesQuerySpecies;
		deFilesSpecies2 = deFilesTargetSpecies;
		ps = new PearsonsCorrelation();
	}

	//
	public BipartiteMatching calculateBestBipartiteMatching(NxMmapping inputCluster) {

		System.out.println("fuzzy scoring");

		String[] genes1 = inputCluster.getGenesFromSpecies(true)
				.toArray(new String[inputCluster.getGenesFromSpecies(true).size()]),
				genes2 = inputCluster.getGenesFromSpecies(false)
						.toArray(new String[inputCluster.getGenesFromSpecies(false).size()]);

		double[][] costMatrix = new double[genes1.length][genes2.length];
		for (int i = 0; i < genes1.length; i++) {
			for (int j = 0; j < genes2.length; j++) {
				costMatrix[i][j] = score(new GenePair(genes1[i], genes2[j])) * -1d;
			}
		}

		return new BipartiteMatching(inputCluster, new HungarianAlgorithm(costMatrix).execute(), costMatrix);

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

		double corr = ps.correlation(corrInputGene1, corrInputGene2);

		return corr;
	}

	/**
	 * scoring by de correlation
	 * 
	 * @param genePair
	 * @return
	 */
	@Override
	public double score(GenePair genePair) {
		if (!UtilityManager.getSimilarityHandler().getSimilarities(query_species, target_species)
				.isSimilar(genePair.getKey(), genePair.getValue())) {
			return -2d;
		}
		return correlation(genePair);
	}

}
