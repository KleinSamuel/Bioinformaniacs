package dennis.bam;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import dennis.util.GenRegVecUtil;
import dennis.util.Interval;

public class GenomicRegionVector implements Comparable<GenomicRegionVector> {

	private LinkedList<Interval> genomicRegions;

	public GenomicRegionVector(Collection<Interval> fwRegions, Collection<Interval> rvRegions) {
		TreeSet<Interval> genomicRegionsSorted = new TreeSet<>();

		genomicRegionsSorted.addAll(fwRegions);
		genomicRegionsSorted.addAll(rvRegions);
		genomicRegions = GenRegVecUtil.merge(genomicRegionsSorted);
		// }
	}

	public GenomicRegionVector(Collection<Interval> blocks) {
		TreeSet<Interval> genomicRegionsSorted = new TreeSet<>();

		genomicRegionsSorted.addAll(blocks);
		genomicRegions = GenRegVecUtil.merge(genomicRegionsSorted);
	}

	public int getStart() {
		return genomicRegions.getFirst().getStart();
	}

	public int getStop() {
		return genomicRegions.getLast().getStop();
	}

	public LinkedList<Interval> getGenomicRegions() {
		return genomicRegions;
	}

	@Override
	public int compareTo(GenomicRegionVector comp) {
		Iterator<Interval> genomicRegs = genomicRegions.iterator(),
				compGenomicRegs = comp.getGenomicRegions().iterator();
		Interval reg = null, compReg = null;
		while (genomicRegs.hasNext() && compGenomicRegs.hasNext()) {
			reg = genomicRegs.next();
			compReg = compGenomicRegs.next();
			int comparison = reg.compareTo(compReg);
			if (comparison != 0) {
				return comparison;
			}
		}
		if (genomicRegs.hasNext()) {
			return 1;
		}
		if (compGenomicRegs.hasNext()) {
			return -1;
		}
		return 0;
	}

	@Override
	public String toString() {
		String ret = "";
		for (Interval i : genomicRegions) {
			ret += i.toString() + "|";
		}
		ret = ret.substring(0, ret.length() - 1);
		return ret;
	}

}
