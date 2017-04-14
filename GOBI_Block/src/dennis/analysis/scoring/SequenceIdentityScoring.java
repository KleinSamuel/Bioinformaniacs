package dennis.analysis.scoring;

import dennis.analysis.BipartiteMatching;
import dennis.analysis.HungarianAlgorithm;
import dennis.similarities.NxMmapping;
import dennis.similarities.SimilarityObject;
import dennis.util.GenePair;
import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;

public class SequenceIdentityScoring extends ScoringFunction {

	public static final String NAME = "seqId";

	public SequenceIdentityScoring(Species query_species, Species target_species) {
		super(query_species, target_species);
	}

	public BipartiteMatching calculateBestBipartiteMatching(NxMmapping inputCluster) {

		System.out.println("seqId scoring");

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

	/**
	 * scoring by identity
	 * 
	 * @param query
	 * @param target
	 * @param query_gene
	 * @param target_gene
	 * @return
	 */
	@Override
	public double score(GenePair genePair) {
		SimilarityObject sim = UtilityManager.getSimilarityHandler()
				.getSimilarities(getQuery_species(), getTarget_species())
				.getSimilarity(genePair.getKey(), genePair.getValue());
		if (sim == null) {
			return 0;
		}
		return sim.getScore();
	}

	@Override
	public String getScoringFunctionName() {
		return NAME;
	}

}
