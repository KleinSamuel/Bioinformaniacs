package dennis.analysis;

import dennis.enrichment.EnrichmentAnalysisUtils;
import dennis.util.GenePair;
import dennis.utility_manager.Species;

public class FuzzyCorrelation extends ScoringFunction {

	public FuzzyCorrelation(Species query_species, target_species) {
		super(query_species, target_species);
	}

	public static double correlation() {
		EnrichmentAnalysisUtils.readDEfile(filePath)
	}

	@Override
	public double score(GenePair genePair) {
		// TODO Auto-generated method stub
		return super.score(genePair);
	}

}
