package dennis.similarities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;

import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;

public class SimilarityHandler {

	// species
	private HashMap<String, GeneSimilarities> sims;
	private String simPath;

	public SimilarityHandler() {
		simPath = UtilityManager.getConfig("gene_similarities");
		sims = new HashMap<>();
	}

	public GeneSimilarities getSimilarities(Species s1, Species s2) {
		if (sims == null) {
			sims = new HashMap<>();
		}
		GeneSimilarities gs = sims.get(s1.getId() + "." + s2.getId());
		if (gs == null) {
			addSimilarityFile(s1, s2);
		}
		return sims.get(s1.getId() + "." + s2.getId());
	}

	/*
	 * returns all similarities of all species for the given gene id species can
	 * be null -> is calculated
	 */
	public HashMap<String, SimilarityObject> getAllSimilarities(Species sp, String geneId) {
		HashMap<String, SimilarityObject> ret = new HashMap<>();
		for (Iterator<Species> s = UtilityManager.speciesIterator(); s.hasNext();) {
			HashMap<String, SimilarityObject> sim = getSimilarities(sp, s.next()).getSimilarities(geneId);
			if (sim != null) {
				ret.putAll(sim);
			}
		}
		return ret;
	}

	public void addSimilarityFile(Species s1, Species s2) {
		File f = new File(simPath + s1.getId() + "." + s2.getId() + ".genesimilarities");
		if (!f.exists()) {
			f = new File(simPath + s2.getId() + "." + s1.getId() + ".genesimilarities");
		}
		addSimilaritiesFile(f, s1, s2);
	}

	public void addSimilaritiesFile(File similarityFile, Species sp1, Species sp2) {

		if (sp1.equals(sp2)) {
			addParalogFile(similarityFile, sp1);
			return;
		}
		GeneSimilarities gs1 = new GeneSimilarities(sp1, sp2), gs2 = new GeneSimilarities(sp2, sp1);
		try {
			BufferedReader br = new BufferedReader(new FileReader(similarityFile));
			String line = null;
			// skip header
			br.readLine();
			while ((line = br.readLine()) != null) {
				parseLine(line, gs1, gs2);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		sims.put(gs1.getId(), gs1);
		sims.put(gs2.getId(), gs2);
	}

	public void parseLine(String line, GeneSimilarities gs1, GeneSimilarities gs2) {
		String[] split = line.split("\t");
		SimilarityObject so1 = new SimilarityObject(Double.parseDouble(split[2])),
				so2 = new SimilarityObject(Double.parseDouble(split[2]));
		gs1.addSimilarity(split[0], split[1], so1);
		gs2.addSimilarity(split[1], split[0], so2);
		split = split[3].split(",");
		for (String s : split) {
			String[] prots = s.split(":");
			so1.addProteinSimilarity(prots[0], prots[1], Double.parseDouble(prots[2]));
			so2.addProteinSimilarity(prots[1], prots[0], Double.parseDouble(prots[2]));
		}
	}

	public void addParalogFile(File paralogFile, Species s) {
		GeneSimilarities gs = new GeneSimilarities(s, s);
		try {
			BufferedReader br = new BufferedReader(new FileReader(paralogFile));
			String line = null;
			// skip header
			br.readLine();
			while ((line = br.readLine()) != null) {
				parseParalogLine(line, gs);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		sims.put(gs.getId(), gs);
	}

	public void parseParalogLine(String line, GeneSimilarities g) {
		String[] split = line.split("\t");
		SimilarityObject so = new SimilarityObject(Double.parseDouble(split[2]));
		g.addSimilarity(split[0], split[1], so);
		g.addSimilarity(split[1], split[0], so);
		split = split[3].split(",");
		for (String s : split) {
			String[] prots = s.split(":");
			so.addProteinSimilarity(prots[0], prots[1], Double.parseDouble(prots[2]));
		}
	}

}
