package dennis.analysis.scoring;

import java.util.Comparator;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import dennis.analysis.BipartiteMatching;
import dennis.analysis.InputDataPreparator;
import dennis.similarities.NxMmapping;
import dennis.similarities.SimilarityObject;
import dennis.util.GenePair;
import dennis.utility_manager.Species;

public class GreedyScoring extends ScoringFunction {

	public GreedyScoring(Species query_species, Species target_species) {
		super(query_species, target_species);
	}

	public static final String NAME = "greedy";

	public BipartiteMatching getGreedyMapping(NxMmapping inputCluster, ScoringFunction scoringFunction) {
		System.out.println("greedy scoring after " + scoringFunction.getScoringFunctionName());
		TreeMap<GenePair, ScoringObject> ret = new TreeMap<>();

		TreeSet<SimilarityObject> objects = new TreeSet<>(new Comparator<SimilarityObject>() {

			@Override
			public int compare(SimilarityObject o1, SimilarityObject o2) {
				return -Double.compare(o1.getMaximumIdentityScore(), o2.getMaximumIdentityScore());
			}
		});
		for (String s : inputCluster.getSims().keySet()) {
			for (SimilarityObject so : inputCluster.getSims().get(s).values()) {
				if (scoringFunction.getScoringFunctionName().equals("fuzzy")) {
					double score = scoringFunction.score(new GenePair(so.getQuery_geneId(), so.getTarget_geneId()));
					objects.add(new SimilarityObject(score, so.getQuery_geneId(), so.getTarget_geneId()));
				} else {
					objects.add(so);
				}
			}
		}

		TreeMap<String, TreeMap<String, SimilarityObject>> copySims = copySims(inputCluster.getSims());

		double score = 0d;

		TreeSet<String> geneIds1 = new TreeSet<>(), geneIds2 = new TreeSet<>();

		boolean goOn = objects.size() > 0;
		while (goOn) {
			SimilarityObject highest = objects.first();
			if (InputDataPreparator.IGNORE_NEGATIVE_SCORES && highest.getScore() < 0) {
				break;
			}
			objects.remove(highest);
			goOn = objects.size() > 0;
			TreeMap<String, SimilarityObject> tree = copySims.get(highest.getQuery_geneId());
			if (tree == null) {
				continue;
			} else {
				if (!tree.containsKey(highest.getTarget_geneId())) {
					continue;
				} else {
					ret.put(new GenePair(highest.getQuery_geneId(), highest.getTarget_geneId()), highest);
					score += highest.getMaximumIdentityScore();
					geneIds1.add(highest.getQuery_geneId());
					geneIds2.add(highest.getTarget_geneId());
					copySims.remove(highest.getQuery_geneId());
					copySims.remove(highest.getTarget_geneId());
				}
			}
		}

		TreeSet<String> unmatched_query = new TreeSet<>(), unmatched_target = new TreeSet<>();
		for (String s : inputCluster.getGenesFromSpecies(true)) {
			if (!geneIds1.contains(s)) {
				unmatched_query.add(s);
			}
		}
		for (String s : inputCluster.getGenesFromSpecies(false)) {
			if (!geneIds2.contains(s)) {
				unmatched_target.add(s);
			}
		}

		return new BipartiteMatching(inputCluster, score, ret, unmatched_query, unmatched_target, geneIds1, geneIds2);

	}

	public static TreeMap<String, TreeMap<String, SimilarityObject>> copySims(
			TreeMap<String, TreeMap<String, SimilarityObject>> simsObjects) {
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

	@Override
	public String getScoringFunctionName() {
		return NAME;
	}

}
