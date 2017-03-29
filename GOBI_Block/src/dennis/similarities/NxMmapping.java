package dennis.similarities;

import java.util.Comparator;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import dennis.util.GenePair;
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

	public TreeMap<String, TreeMap<String, SimilarityObject>> getSims() {
		return simsObjects;
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

	public TreeMap<GenePair, SimilarityObject> getGreedyMapping() {
		TreeMap<GenePair, SimilarityObject> ret = new TreeMap<>();

		TreeSet<SimilarityObject> objects = new TreeSet<>(new Comparator<SimilarityObject>() {

			@Override
			public int compare(SimilarityObject o1, SimilarityObject o2) {
				double comp = o1.getMaximumIdentityScore() - o2.getMaximumIdentityScore();
				if (comp < 0)
					return -1;
				if (comp > 0)
					return 1;
				return 0;
			}
		});
		for (String s : simsObjects.keySet()) {
			for (SimilarityObject so : simsObjects.get(s).values()) {
				objects.add(so);
			}
		}

		TreeMap<String, TreeMap<String, SimilarityObject>> copySims = copySims();

		boolean goOn = objects.size() > 0;
		while (goOn) {
			SimilarityObject highest = objects.first();
			objects.remove(highest);
			TreeMap<String, SimilarityObject> tree = copySims.get(highest.getQuery_geneId());
			if (tree == null) {
				continue;
			} else {
				if (!tree.containsKey(highest.getTarget_geneId())) {
					continue;
				} else {
					ret.put(new GenePair(highest.getQuery_geneId(), highest.getTarget_geneId()), highest);
					copySims.remove(highest.getQuery_geneId());
					copySims.remove(highest.getTarget_geneId());
				}
			}
			goOn = objects.size() > 0;
		}

		return ret;

	}

	public TreeMap<String, TreeMap<String, SimilarityObject>> copySims() {
		TreeMap<String, TreeMap<String, SimilarityObject>> sims = new TreeMap<>();
		for (String s : simsObjects.keySet()) {
			TreeMap<String, SimilarityObject> sim = new TreeMap<>();
			for (Entry<String, SimilarityObject> so : simsObjects.get(s).entrySet()) {
				sim.put(so.getKey(), so.getValue());
			}
			sims.put(s, sim);
		}
		return sims;
	}

}
