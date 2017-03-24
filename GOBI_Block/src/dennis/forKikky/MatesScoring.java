package dennis.forKikky;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeMap;

import dennis.similarities.NxMmapping;
import dennis.similarities.SimilarityObject;

public class MatesScoring {

	public static HashMap<String, String> greedy_score(LinkedList<KikkyNxMmapping> cluster) {
		HashMap<String, String> mates = new HashMap<>();
		for (KikkyNxMmapping single_cluster : cluster) {
			ArrayList<SimilarityObject> all_objects = new ArrayList<>();
			HashSet<String> used_ids = new HashSet<>();
			TreeMap<String, TreeMap<String, SimilarityObject>> curr_cluster = single_cluster.getSims();
			for (String s : curr_cluster.keySet()) {
				all_objects.addAll(curr_cluster.get(s).values());
			}
			all_objects.sort(new SimilarityObjectComp());
			for (SimilarityObject so : all_objects)
				if ((!used_ids.contains(so.getQuery_geneId())) && (!used_ids.contains(so.getTarget_geneId()))) {
					mates.put(so.getQuery_geneId(), so.getTarget_geneId());
					used_ids.add(so.getQuery_geneId());
					used_ids.add(so.getTarget_geneId());
				}
		}
		System.out.println(mates.size());
		return mates;
	}

}
