package dennis.similarities;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;

public class NxMmapping {

	private Species species1, species2;
	private TreeSet<String> geneIdsSpecies1, geneIdsSpecies2;
	private TreeMap<String, TreeMap<String, SimilarityObject>> simsObjects;
	private HashMap<String, Integer> indices;

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
			for (Entry<String, SimilarityObject> e : UtilityManager.getSimilarityHandler()
					.getSimilarities(species1, species2).getSimilarities(s).entrySet()) {
				if (geneIdsSpecies2.contains(e.getKey())) {
					x.put(e.getKey(), e.getValue());
				}
			}
			simsObjects.put(s, x);
		}
	}

	public TreeMap<String, TreeMap<String, SimilarityObject>> getSims() {
		return simsObjects;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(geneIdsSpecies1.size() + "\t" + geneIdsSpecies2.size() + "\t");
		for (String s : geneIdsSpecies1) {
			sb.append(s + ",");
		}

		sb.deleteCharAt(sb.length() - 1);
		sb.append("\t");

		for (String s : geneIdsSpecies2) {
			sb.append(s + ",");
		}
		sb.deleteCharAt(sb.length() - 1);
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

	public int getOverallSims() {
		int i = 0;

		for (String s : simsObjects.keySet()) {
			i += simsObjects.get(s).size();
		}

		return i;
	}

	public HashMap<String, Integer> getIndicesOfGenes() {
		if (indices == null) {
			indices = new HashMap<>();
			int i = 0;
			for (String geneId : geneIdsSpecies1) {
				indices.put(geneId, i++);
			}
			i = 0;
			for (String geneId : geneIdsSpecies2) {
				indices.put(geneId, i++);
			}
		}
		return indices;
	}

}
