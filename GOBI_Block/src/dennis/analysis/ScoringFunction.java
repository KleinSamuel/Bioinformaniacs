package dennis.analysis;

import dennis.similarities.SimilarityObject;
import dennis.util.GenePair;
import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;

public class ScoringFunction {

	private Species querySpecies, targetSpecies;

	public ScoringFunction(Species sp1, Species sp2) {
		querySpecies = sp1;
		targetSpecies = sp2;
	}

	/**
	 * standard scoring by identity
	 * 
	 * @param query
	 * @param target
	 * @param query_gene
	 * @param target_gene
	 * @return
	 */
	public double score(GenePair genePair) {
		SimilarityObject sim = UtilityManager.getSimilarityHandler().getSimilarities(querySpecies, targetSpecies)
				.getSimilarity(genePair.getKey(), genePair.getValue());
		if (sim == null) {
			return 0;
		}
		return sim.getScore();
	}

	public Species getQuerySpecies() {
		return querySpecies;
	}

	public Species getTargetSpecies() {
		return targetSpecies;
	}

}
