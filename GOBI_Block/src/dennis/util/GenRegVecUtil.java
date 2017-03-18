package dennis.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import augmentedTree.IntervalTree;
import dennis.genomeAnnotation.Exon;

public class GenRegVecUtil {

	public static LinkedList<Interval> getIntrons(Collection<Interval> exons) {
		LinkedList<Interval> introns = new LinkedList<>();
		if (exons.size() <= 1) {
			return introns;
		}
		Iterator<Interval> exonIt = exons.iterator();
		Interval exon, nextExon;
		exon = exonIt.next();
		while (exonIt.hasNext()) {
			nextExon = exonIt.next();
			introns.add(new Interval(exon.getStop() + 1, nextExon.getStart() - 1));
			exon = nextExon;
		}
		return introns;
	}

	public static IntervalTree<Interval> getExonsAsTree(Collection<Exon> exons) {
		IntervalTree<Interval> introns = new IntervalTree<>();
		if (exons.size() <= 0) {
			return introns;
		}
		Iterator<Exon> exonIt = exons.iterator();
		Exon exon, nextExon;
		exon = exonIt.next();
		while (exonIt.hasNext()) {
			nextExon = exonIt.next();
			introns.add(new Interval(exon.getStop() + 1, nextExon.getStart() - 1));
			exon = nextExon;
		}
		return introns;
	}

	public static LinkedList<Interval> merge(Collection<Interval> vectors) {
		LinkedList<Interval> mergedTree = new LinkedList<>();
		if (vectors.size() == 0) {
			return mergedTree;
		}
		Iterator<Interval> vectorIt = vectors.iterator();
		Interval merged = null, nextVector = null;
		merged = vectorIt.next();
		while (vectorIt.hasNext()) {
			nextVector = vectorIt.next();
			if (nextVector.getStart() > merged.getStop() + 1) {
				mergedTree.add(merged);
				merged = nextVector;
			} else {
				merged.setStop(Math.max(nextVector.getStop(), merged.getStop()));
			}
		}
		if (merged != null) {
			mergedTree.add(merged);
		}
		return mergedTree;
	}

}