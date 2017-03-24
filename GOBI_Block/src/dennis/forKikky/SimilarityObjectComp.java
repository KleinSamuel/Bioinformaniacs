package dennis.forKikky;

import java.util.Comparator;

import dennis.similarities.SimilarityObject;

public class SimilarityObjectComp implements Comparator<SimilarityObject> {

	@Override
	public int compare(SimilarityObject s1, SimilarityObject s2) {
		return (int) ((s2.getMaximumIdentityScore() - s1.getMaximumIdentityScore()) * 1000);
	}

}
