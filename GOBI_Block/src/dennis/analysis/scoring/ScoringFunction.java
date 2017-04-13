package dennis.analysis.scoring;

import dennis.util.GenePair;
import dennis.utility_manager.Species;

public class ScoringFunction {

	protected Species query_species, target_species;

	public ScoringFunction(Species query_species, Species target_species) {
		this.query_species = query_species;
		this.target_species = target_species;
	}

	public Species getQuery_species() {
		return query_species;
	}

	public Species getTarget_species() {
		return target_species;
	}

	public double score(GenePair genePair) {
		return 0;
	}

	public String getScoringFunctionName() {
		return "default";
	}
}
