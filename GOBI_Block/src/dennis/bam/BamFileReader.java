package dennis.bam;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import dateNtimeStuff.DateFactory;
import dennis.genomeAnnotation.GenomeAnnotation;
import dennis.utility_manager.Experiment;
import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;

public class BamFileReader {

	private String bamFile;
	public static GenomeAnnotation ga;
	private Species sp;
	private String tissue, experiment, mapper;
	private boolean paired = true, strandSpec = true;

	public BamFileReader(Species species, String tissue, Experiment experiment, String mapper) {
		bamFile = UtilityManager.getConfig("main_bam_directory") + "/" + species.getId() + "/" + experiment.getName()
				+ "/" + mapper + ".bam";
		sp = species;
		this.tissue = tissue;
		this.experiment = experiment.getName();
		this.mapper = mapper;
		this.paired = experiment.isPaired();
		this.strandSpec = experiment.isStrandSpec();
		ga = species.getGenomeAnnotation();
	}

	public void readBAMFile() {
		if (paired) {
			// readId, waitingRead
			HashMap<String, SAMRecord> waitingRecords;
			waitingRecords = new HashMap<>();
			SamReaderFactory.setDefaultValidationStringency(ValidationStringency.SILENT);
			SamReader sr = SamReaderFactory.makeDefault().open(new File(bamFile));
			Iterator<SAMRecord> it = sr.iterator();
			SAMRecord sam = null, possibleMate = null;
			// reads are sorted by start --> so if new chromosome clear map
			String chromId = null;
			UpdatedReadPair rp = null;
			int validRecords = 0, validPairs = 0, invalidRecords = 0, nonValidPairs = 0, checkedRecords = 0;
			int splitInconsistent = 0, wrong = 0;
			PCRIndex pcrIndices = new PCRIndex();
			GeneCounter geneCounts = new GeneCounter();
			try {
				File f = new File(UtilityManager.getConfig("output_directory") + "/" + sp.getId() + "/" + tissue + "/"
						+ experiment + "/" + mapper + "/");
				f.mkdirs();
				f = new File(UtilityManager.getConfig("output_directory") + "/" + sp.getId() + "/" + tissue + "/"
						+ experiment + "/" + mapper + "/logFile.txt");
				BufferedWriter bw = new BufferedWriter(new FileWriter(f));
				bw.write(
						"[" + DateFactory.getDateAsString() + " "
								+ ((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
										/ 1024d / 1024d / 1024d * 1000d)) / 1000d
								+ "GB]\tstarted reading\t" + bamFile + "\n");
				while (it.hasNext()) {
					sam = it.next();
					checkedRecords++;
					if (validRecord(sam)) {
						validRecords++;
						// check if new chromosome
						if (chromId == null) {
							chromId = sam.getReferenceName();
						} else {
							if (!sam.getReferenceName().equals(chromId)) {
								waitingRecords = new HashMap<>();
								chromId = sam.getReferenceName();
							}
						}
						// look for waiting record in map
						possibleMate = waitingRecords.get(sam.getReadName());
						if (possibleMate == null) {
							waitingRecords.put(sam.getReadName(), sam);
						} else {
							// check if valid pair --> but what to do if not -->
							// both
							// reads valid?? possible??
							rp = validPair(sam, possibleMate);
							if (rp == null) {
								rp = validPair(possibleMate, sam);
							}
							if (rp == null) {
								nonValidPairs++;
								continue;
							} else {
								waitingRecords.remove(sam.getReadName());
								validPairs++;
								if (rp.getSplitCount() < 0) {
									splitInconsistent++;
								} else {
									int pcrIndex = pcrIndices.addRead(rp);
									rp.setPCRindex(pcrIndex);

									geneCounts.addReadObject(rp);
								}
							}
						}
					} else {
						invalidRecords++;
					}
					if (checkedRecords % 1000000 == 0) {
						bw.write(
								"[" + DateFactory.getDateAsString() + " "
										+ ((int) ((Runtime.getRuntime().totalMemory()
												- Runtime.getRuntime().freeMemory()) / 1024d / 1024d / 1024d * 1000d))
												/ 1000d
										+ "GB]\tcheckedRecords: " + checkedRecords + "\tvalidRecords: " + validRecords
										+ "\tinvalidRecords: " + invalidRecords + "\tvalidPairs: " + validPairs
										+ "\tnonValidPairs: " + nonValidPairs + "\tinconsistent: " + splitInconsistent
										+ "\twrong: " + wrong + "\n");
					}
				}
				bw.write("[" + DateFactory.getDateAsString() + " "
						+ ((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024d
								/ 1024d / 1024d * 1000d)) / 1000d
						+ "GB]\tcheckedRecords: " + checkedRecords + "\tvalidRecords: " + validRecords
						+ "\tinvalidRecords: " + invalidRecords + "\tvalidPairs: " + validPairs + "\tnonValidPairs: "
						+ nonValidPairs + "\tinconsistent: " + splitInconsistent + "\twrong: " + wrong + "\n");
				bw.write("[" + DateFactory.getDateAsString() + " "
						+ ((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024d
								/ 1024d / 1024d * 1000d)) / 1000d
						+ "GB]\tFinished reading\n");
				geneCounts.writeOutput(UtilityManager.getConfig("output_directory") + "/" + sp.getId() + "/" + tissue
						+ "/" + experiment + "/" + mapper + "/");
				bw.write("[" + DateFactory.getDateAsString() + " "
						+ ((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024d
								/ 1024d / 1024d * 1000d)) / 1000d
						+ "GB]\twrote output to\t" + UtilityManager.getConfig("output_directory") + "/" + sp.getId()
						+ "/" + tissue + "/" + experiment + "/" + mapper + "/" + "\n");
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		} else {
			readUnpairedBamFile();
		}
	}

	public void readUnpairedBamFile() {
		SamReaderFactory.setDefaultValidationStringency(ValidationStringency.SILENT);
		SamReader sr = SamReaderFactory.makeDefault().open(new File(bamFile));
		Iterator<SAMRecord> it = sr.iterator();
		SAMRecord sam = null;
		SingleRead read = null;
		int validRecords = 0, checkedRecords = 0, invalidRecords = 0;
		PCRIndex pcrIndices = new PCRIndex();
		GeneCounter geneCounts = new GeneCounter();
		try {
			File f = new File(UtilityManager.getConfig("output_directory") + "/" + sp.getId() + "/" + tissue + "/"
					+ experiment + "/" + mapper + "/");
			f.mkdirs();
			f = new File(UtilityManager.getConfig("output_directory") + "/" + sp.getId() + "/" + tissue + "/"
					+ experiment + "/" + mapper + "/logFile.txt");
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write(
					"[" + DateFactory.getDateAsString()
							+ " " + ((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
									/ 1024d / 1024d / 1024d * 1000d)) / 1000d
							+ "GB]\tstarted reading\t" + bamFile + "\n");
			while (it.hasNext()) {
				sam = it.next();
				checkedRecords++;
				if (validRecord(sam)) {
					validRecords++;
					read = new SingleRead(sam);
					int pcrIndex = pcrIndices.addRead(read);
					read.setPCRindex(pcrIndex);

					geneCounts.addReadObject(read);

				} else {
					invalidRecords++;
				}
				if (checkedRecords % 1000000 == 0) {
					bw.write("[" + DateFactory.getDateAsString() + " "
							+ ((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024d
									/ 1024d / 1024d * 1000d)) / 1000d
							+ "GB]\tcheckedRecords: " + checkedRecords + "\tvalidRecords: " + validRecords
							+ "\tinvalidRecords: " + invalidRecords + "\n");
				}
			}
			bw.write("[" + DateFactory.getDateAsString() + " "
					+ ((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024d / 1024d
							/ 1024d * 1000d)) / 1000d
					+ "GB]\tcheckedRecords: " + checkedRecords + "\tvalidRecords: " + validRecords
					+ "\tinvalidRecords: " + invalidRecords + "\n");
			bw.write("[" + DateFactory.getDateAsString() + " "
					+ ((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024d / 1024d
							/ 1024d * 1000d)) / 1000d
					+ "GB]\tFinished reading\n");
			geneCounts.writeOutput(UtilityManager.getConfig("output_directory") + "/" + sp.getId() + "/" + tissue + "/"
					+ experiment + "/" + mapper + "/");
			bw.write("[" + DateFactory.getDateAsString() + " "
					+ ((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024d / 1024d
							/ 1024d * 1000d)) / 1000d
					+ "GB]\twrote output to\t" + UtilityManager.getConfig("output_directory") + "/" + sp.getId() + "/"
					+ tissue + "/" + experiment + "/" + mapper + "/" + "\n");
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public boolean validRecord(SAMRecord sam) {
		if (paired) {
			return (ga.getChromosome(sam.getReferenceName()) != null && !sam.getReadUnmappedFlag()
					&& !sam.getMateUnmappedFlag() && !sam.getNotPrimaryAlignmentFlag()
					&& sam.getReferenceName().equals(sam.getMateReferenceName())
					&& sam.getReadNegativeStrandFlag() != sam.getMateNegativeStrandFlag());
		}
		if (!paired) {
			return (ga.getChromosome(sam.getReferenceName()) != null && !sam.getReadUnmappedFlag());
		}
		return false;
	}

	public UpdatedReadPair validPair(SAMRecord first, SAMRecord second) {
		if (!first.getReferenceName().equals(second.getReferenceName()))
			return null;
		if (first.getFirstOfPairFlag() && second.getSecondOfPairFlag()
				&& first.getAlignmentStart() == second.getMateAlignmentStart()
				&& first.getMateAlignmentStart() == second.getAlignmentStart()) {
			return new UpdatedReadPair(first, second);
		}
		return null;
	}

}
