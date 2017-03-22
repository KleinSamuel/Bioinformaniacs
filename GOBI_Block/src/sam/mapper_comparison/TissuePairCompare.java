package sam.mapper_comparison;

import dennis.tissues.Tissue;
import dennis.tissues.TissuePair;
import dennis.utility_manager.Species;

public class TissuePairCompare extends TissuePair{
	
	private Species species;

	public TissuePairCompare(Tissue key, Tissue value, Species species) {
		super(key, value);
		this.species = species;
	}
	
	public Species getSpecies(){
		return this.species;
	}

}
