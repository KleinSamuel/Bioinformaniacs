package dennis.analysis;

import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;

public class ScoringFunction {

	public ScoringFunction() {

	}

	/**
	 * standard scoring
	 * 
	 * @param query
	 * @param target
	 * @param query_gene
	 * @param target_gene
	 * @return
	 */
	public double score(Species query, Species target, String query_gene, String target_gene) {
		return UtilityManager.getSimilarityHandler().getSimilarities(query, target)
				.getSimilarity(query_gene, target_gene).getMaximumIdentityScore();
	}

}
