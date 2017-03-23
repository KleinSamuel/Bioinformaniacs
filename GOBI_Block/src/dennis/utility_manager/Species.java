package dennis.utility_manager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

import dennis.genomeAnnotation.GTFParser;
import dennis.genomeAnnotation.Gene;
import dennis.genomeAnnotation.GenomeAnnotation;

public class Species implements Comparable<Species> {

	private int id;
	private String name, gtf, sChrs;

	private HashSet<String> standardChromosomes = null;

	private GenomeAnnotation ga = null;

	public Species(int id, String name, String gtf, String standardChrs, String tissueMapping) {
		this.id = id;
		this.name = name;
		this.gtf = gtf;
		this.sChrs = standardChrs;
		readStandardChromosomes(standardChrs);
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getChrs() {
		return sChrs;
	}

	/**
	 * 
	 * @return name of gtf file full file:
	 *         UtilityManager.getConfig("gtfs")+getGtf
	 */
	public String getGtf() {
		return gtf;
	}

	@Override
	public boolean equals(Object obj) {
		return id == ((Species) obj).getId();
	}

	@Override
	public int hashCode() {
		return id;
	}

	public void readStandardChromosomes(String standardChrs) {
		standardChromosomes = new HashSet<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(standardChrs)));
			String line = null;
			while ((line = br.readLine()) != null) {
				standardChromosomes.add(line);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isStandardChromosome(String chrId) {
		return standardChromosomes.contains(chrId);
	}

	public GenomeAnnotation getGenomeAnnotation() {
		if (ga == null) {
			ga = GTFParser.readGtfFile(name, gtf, standardChromosomes);
		}
		return ga;
	}

	@Override
	public int compareTo(Species o) {
		return Integer.compare(id, o.getId());
	}

	@Override
	public String toString() {
		return id + ": " + name;
	}

	public void writeGeneLenghts() {
		try {
			File f = new File(UtilityManager.getConfig("output_directory") + "geneLengths/");
			f.mkdirs();
			BufferedWriter bw = new BufferedWriter(
					new FileWriter(new File(f.getAbsolutePath() + "/" + id + ".geneLengths")));
			bw.write("geneId\tmerged_transcript_length\tgene_length\n");
			for (Iterator<Gene> genes = this.getGenomeAnnotation().iterator(); genes.hasNext();) {
				Gene g = genes.next();
				bw.write(g.getId() + "\t" + g.getMergedTranscript().getExonicLength() + "\t" + g.getLength() + "\n");
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
