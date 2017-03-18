package dennis.bam;

import java.util.LinkedList;

import dennis.util.GenRegVecUtil;
import dennis.util.Interval;
import htsjdk.samtools.AlignmentBlock;
import htsjdk.samtools.SAMRecord;

public class SingleRead extends ReadObject {

	private SAMRecord rec;

	public SingleRead(SAMRecord rec) {
		super();
		this.rec = rec;
		LinkedList<Interval> blocksRec = new LinkedList<>();
		for (AlignmentBlock ab : rec.getAlignmentBlocks()) {
			blocksRec.add(new Interval(ab.getReferenceStart() - 1, ab.getReferenceStart() + ab.getLength() - 2));
		}
		LinkedList<Interval> intronsRec = GenRegVecUtil.getIntrons(blocksRec);

		blocks = new GenomicRegionVector(blocksRec);
		introns = new GenomicRegionVector(intronsRec);

		genomicRegion = new Interval(rec.getAlignmentStart() - 1, rec.getAlignmentEnd() - 1);

		ambigous = getMatchedGenes().size() > 1;
	}

	public String getReadName() {
		return rec.getReadName();
	}

	public String getReferenceName() {
		return rec.getReferenceName();
	}

}
