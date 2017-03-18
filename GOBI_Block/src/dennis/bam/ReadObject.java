package dennis.bam;

import java.util.HashSet;
import java.util.LinkedList;

import augmentedTree.IntervalTree;
import dennis.genomeAnnotation.Exon;
import dennis.genomeAnnotation.Gene;
import dennis.genomeAnnotation.Transcript;
import dennis.util.Interval;

public abstract class ReadObject {

	protected LinkedList<Gene> spanningGenes = new LinkedList<>();
	protected LinkedList<Gene> matchedGenes = null;
	protected int pcrIndex = 0;
	protected boolean intronic = false, merged = false, transcriptomic = false, ambigous = false;
	protected Interval genomicRegion;

	// updates
	protected GenomicRegionVector blocks, introns;
	// end

	public ReadObject() {

	}

	public LinkedList<Gene> getMatchedGenes() {
		if (matchedGenes == null) {
			// get genes spanning forward read and genes spanning reverse reads
			// spanning means: gene.start <= start && gene.stop >= stop
			LinkedList<Gene> possibleGenes = BamFileReader.ga.getChromosome(getReferenceName()).getAllGenesSorted()
					.getIntervalsSpanning(genomicRegion.getStart(), genomicRegion.getStop(), new LinkedList<>());
			spanningGenes = possibleGenes;
			if (possibleGenes.isEmpty()) {
				matchedGenes = new LinkedList<>();
				return matchedGenes;
			}

			HashSet<Exon> nextExons = null;
			HashSet<Transcript> possibleTrs = new HashSet<>();

			HashSet<Transcript> possiblePerfectTrs = new HashSet<>();
			for (Gene g : possibleGenes) {
				HashSet<Transcript> trOverlaps = new HashSet<>();
				possibleTrs = new HashSet<>();
				for (Interval block : this.blocks.getGenomicRegions()) {
					nextExons = g.getAllExonsSorted().getIntervalsSpanning(block.getStart(), block.getStop(),
							new HashSet<>());
					if (!nextExons.isEmpty()) {
						if (possibleTrs.isEmpty()) {
							for (Exon e : nextExons) {
								possibleTrs.addAll(e.getParentalTranscripts());
							}
						} else {
							for (Exon e : nextExons) {
								for (Transcript t : e.getParentalTranscripts()) {
									if (possibleTrs.contains(t)) {
										trOverlaps.add(t);
									}
								}
							}
							possibleTrs = new HashSet<>(trOverlaps);
							trOverlaps = new HashSet<>();
						}
					} else {
						trOverlaps = new HashSet<>();
						possibleTrs = new HashSet<>();
						break;
					}
					if (possibleTrs.isEmpty()) {
						break;
					}
				}
				if (!possibleTrs.isEmpty()) {
					possiblePerfectTrs.addAll(possibleTrs);
				}
			}

			// matching genes merged contains merged; possiblePerfectTrs
			// contains possibly
			// perfect hit transcripts --> check if perfect hit
			// intronic genes contains intronic genes
			HashSet<Gene> matchedGenesAndTrs = new HashSet<>();
			LinkedList<Transcript> matchedTranscripts = checkTranscripts(possiblePerfectTrs);
			for (Transcript t : matchedTranscripts) {
				matchedGenesAndTrs.add(t.getParentalGene());
			}
			matchedGenes = new LinkedList<>();
			matchedGenes.addAll(matchedGenesAndTrs);
			if (matchedGenes.isEmpty()) {
				for (Gene g : possibleGenes) {
					if (checkIfMerged(g)) {
						matchedGenes.add(g);
						merged = true;
					}
				}
				if (merged) {
					return matchedGenes;
				}
			} else {
				transcriptomic = true;
				return matchedGenes;
			}
			// no transcript can map because merged is false --> intronic
			intronic = true;
			matchedGenes.addAll(possibleGenes);
			return matchedGenes;
		}
		return matchedGenes;

	}

	public boolean checkIfMerged(Gene g) {
		IntervalTree<Interval> mergedExons = g.getUnionTranscript();
		if (genomicRegion.getStart() < mergedExons.getStart() || genomicRegion.getStop() > mergedExons.getStop()) {
			return false;
		}
		for (Interval exon : blocks.getGenomicRegions()) {
			if (mergedExons.getIntervalsSpanning(exon.getStart(), exon.getStop(), new HashSet<>()).isEmpty())
				return false;
		}
		return true;
	}

	public LinkedList<Transcript> checkTranscripts(HashSet<Transcript> possibleTrs) {
		LinkedList<Transcript> ret = new LinkedList<>();
		for (Transcript tr : possibleTrs) {
			if (checkTranscript(tr)) {
				ret.add(tr);
			}
		}
		return ret;
	}

	public boolean checkTranscript(Transcript tr) {
		if (blocks.getGenomicRegions().size() == 1) {
			return true;
		} else {
			for (Interval i : introns.getGenomicRegions()) {
				if (tr.getIntrons().getIntervalsEqual(i.getStart(), i.getStop(), new LinkedList<>()).isEmpty())
					return false;
			}
		}
		return true;
	}

	public LinkedList<Gene> getSpanningGenes() {
		return spanningGenes;
	}

	public Interval getGenomicRegion() {
		return genomicRegion;
	}

	public void setPCRindex(int index) {
		this.pcrIndex = index;
	}

	public int getPcrIndex() {
		return pcrIndex;
	}

	public boolean isIntronic() {
		return intronic;
	}

	public boolean isMerged() {
		return merged;
	}

	public boolean isTranscriptomic() {
		return transcriptomic;
	}

	public GenomicRegionVector getGenomicRegionVector() {
		return blocks;
	}

	public String getReadName() {
		return null;
	}

	public String getReferenceName() {
		return null;
	}

}
