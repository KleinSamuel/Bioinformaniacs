package dennis.bam;

import java.util.LinkedList;

import dennis.util.GenRegVecUtil;
import dennis.util.Interval;
import htsjdk.samtools.AlignmentBlock;
import htsjdk.samtools.SAMRecord;

public class UpdatedReadPair extends ReadObject {

	private SAMRecord forward, reverse;
	private LinkedList<Interval> blocksForward, blocksReverse;
	private LinkedList<Interval> intronsForward, intronsReverse;
	private int splitCount = -2;

	public UpdatedReadPair(SAMRecord forward, SAMRecord reverse) {
		super();
		this.forward = forward;
		this.reverse = reverse;
		blocksForward = new LinkedList<>();
		for (AlignmentBlock ab : forward.getAlignmentBlocks()) {
			blocksForward.add(new Interval(ab.getReferenceStart() - 1, ab.getReferenceStart() + ab.getLength() - 2));
		}

		blocksForward = GenRegVecUtil.merge(blocksForward);
		intronsForward = GenRegVecUtil.getIntrons(blocksForward);
		blocksReverse = new LinkedList<>();
		for (AlignmentBlock ab : reverse.getAlignmentBlocks()) {
			blocksReverse.add(new Interval(ab.getReferenceStart() - 1, ab.getReferenceStart() + ab.getLength() - 2));
		}

		blocksReverse = GenRegVecUtil.merge(blocksReverse);
		intronsReverse = GenRegVecUtil.getIntrons(blocksReverse);

		blocks = new GenomicRegionVector(blocksForward, blocksReverse);
		introns = new GenomicRegionVector(intronsForward, intronsReverse);

		genomicRegion = new Interval(Math.min(forward.getAlignmentStart(), reverse.getAlignmentStart()) - 1,
				Math.max(forward.getAlignmentEnd(), reverse.getAlignmentEnd()) - 1);
		calcSplitCount();
		if (splitCount == -1) {
			return;
		}

		if (splitCount > -1) {
			ambigous = getMatchedGenes().size() > 1;
		}
	}

	public int getSplitCount() {
		if (splitCount == -2) {
			calcSplitCount();
		}
		return splitCount;
	}

	/**
	 * returns -1 if splitInconsistent
	 */
	public void calcSplitCount() {
		if (checkIfSplitInconsistent()) {
			splitCount = -1;
		} else {
			splitCount = introns.getGenomicRegions().size();
		}
	}

	public boolean checkIfSplitInconsistent() {
		for (Interval intronFw : intronsForward) {
			for (Interval i : blocksReverse) {
				if (i.overlaps(intronFw)) {
					return true;
				}
			}
		}
		for (Interval intronRv : intronsReverse) {
			for (Interval i : blocksForward) {
				if (i.overlaps(intronRv)) {
					return true;
				}
			}
		}
		return false;
	}

	public SAMRecord getForward() {
		return forward;
	}

	public SAMRecord getReverse() {
		return reverse;
	}

	public String getReadName() {
		return forward.getReadName();
	}

	public String getReferenceName() {
		return forward.getReferenceName();
	}
}
