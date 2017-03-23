package dennis.similarities;

import java.util.TreeMap;
import java.util.TreeSet;

import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;

public class NxMmapping {

	private Species species1, species2;
	private TreeSet<String> geneIdsSpecies1, geneIdsSpecies2;
	private TreeMap<String, TreeMap<String, SimilarityObject>> simsObjects;

	public NxMmapping(Species sp1, Species sp2, TreeSet<String> idsSp1, TreeSet<String> idsSp2) {
		species1 = sp1;
		species2 = sp2;
		geneIdsSpecies1 = idsSp1;
		geneIdsSpecies2 = idsSp2;
		simsObjects = new TreeMap<>();
		init();
	}

	public void init() {
		for (String s : geneIdsSpecies1) {
			TreeMap<String, SimilarityObject> x = new TreeMap<>();
			x.putAll(UtilityManager.getSimilarityHandler().getSimilarities(species1, species2).getSimilarities(s));
			simsObjects.put(s, x);
		}
		for (String s : geneIdsSpecies2) {
			TreeMap<String, SimilarityObject> x = new TreeMap<>();
			x.putAll(UtilityManager.getSimilarityHandler().getSimilarities(species2, species1).getSimilarities(s));
			simsObjects.put(s, x);
		}

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String s : simsObjects.keySet()) {
			sb.append(s + ":\n");
			for (String e : simsObjects.get(s).keySet()) {
				sb.append("\t" + e + "\n");
			}
		}
		return sb.toString();
	}

	public TreeSet<String> getGenesFromSpecies(boolean species1) {
		if (species1)
			return geneIdsSpecies1;
		return geneIdsSpecies2;
	}

	public Species getSpecies(boolean species1) {
		if (species1)
			return this.species1;
		return species2;
	}

}
