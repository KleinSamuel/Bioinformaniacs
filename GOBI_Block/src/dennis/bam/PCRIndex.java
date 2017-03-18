package dennis.bam;

import java.util.Iterator;
import java.util.TreeMap;

public class PCRIndex {

	// store genomic region vectors that implement comparable
	TreeMap<GenomicRegionVector, Integer> pcrIndices;
	String currentReferenceName = null;

	public PCRIndex() {
		pcrIndices = new TreeMap<>();
	}

	public int addRead(ReadObject read) {
		if (currentReferenceName == null) {
			currentReferenceName = read.getReferenceName();
		} else {
			if (!currentReferenceName.equals(read.getReferenceName())) {
				currentReferenceName = read.getReferenceName();
				pcrIndices = new TreeMap<>();
			}
		}
		GenomicRegionVector grv = read.getGenomicRegionVector();
		// delete some regions if they can't appear anymore
		if (pcrIndices.size() > 500000) {
			Iterator<GenomicRegionVector> iter = pcrIndices.keySet().iterator();

			while (iter.hasNext() && iter.next().getStart() < grv.getStart() / 2) {
				iter.remove();
			}
		}

		Integer pcrIndex = null;
		pcrIndex = pcrIndices.get(grv);
		if (pcrIndex == null) {
			pcrIndices.put(grv, 0);
			return 0;
		}
		pcrIndices.put(grv, pcrIndex + 1);
		return pcrIndex + 1;
	}

}
