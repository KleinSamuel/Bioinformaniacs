package dennis.similarities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeSet;

import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;
import javafx.util.Pair;

public class SimilarityHandler {

	/**
	 * key = species1.species2
	 */
	private HashMap<String, GeneSimilarities> sims;
	private String simPath;

	public SimilarityHandler() {
		simPath = UtilityManager.getConfig("gene_similarities");
		sims = new HashMap<>();
	}

	/**
	 * 
	 * @param query_species
	 * @param target_species
	 * @return GeneSimilarities containing all similarities between the two
	 *         species
	 */
	public GeneSimilarities getSimilarities(Species query_species, Species target_species) {
		if (sims == null) {
			sims = new HashMap<>();
		}
		GeneSimilarities gs = sims.get(query_species.getId() + "." + target_species.getId());
		if (gs == null) {
			addSimilarityFile(query_species, target_species);
		}
		return sims.get(query_species.getId() + "." + target_species.getId());
	}

	/**
	 * @return all similarities of all species for the given gene id; species
	 *         can be null -> is calculated by gene_id
	 */
	public HashMap<String, SimilarityObject> getAllSimilarities(Species query_species, String geneId) {
		HashMap<String, SimilarityObject> ret = new HashMap<>();
		if (query_species == null) {
			query_species = UtilityManager.getSpecies(UtilityManager.getSpeciesIDFromGeneID(geneId));
		}
		for (Iterator<Species> s = UtilityManager.speciesIterator(); s.hasNext();) {
			HashMap<String, SimilarityObject> sim = getSimilarities(query_species, s.next()).getSimilarities(geneId);
			if (sim != null) {
				ret.putAll(sim);
			}
		}
		return ret;
	}

	/**
	 * 
	 * @param query
	 * @param target
	 * @return all geneIds in query_species that have an orthologue in the
	 *         target_species
	 */
	public HashSet<String> getAllGenesWithAnOrtholog(Species query, Species target) {
		return getSimilarities(query, target).getGenesWithPartner();
	}

	/**
	 * 
	 * @param query_species:
	 *            can be null -> calculated by query_geneId
	 * @param target_species:
	 *            can be null -> calculated by first target_geneId
	 * @param query_species
	 *            can be null -> calculated by query_geneId
	 * @param target_species
	 *            can be null -> calculated by first target_geneId branch
	 *            'master' of
	 * @param query_geneId
	 * @param target_geneIds
	 * @return SimilarityObject between query_gene and target_gene with highest
	 *         similarity score; null if no target_gene is similar to query_gene
	 */
	public SimilarityObject checkForHighestSimilarity(Species query_species, Species target_species,
			String query_geneId, HashSet<String> target_geneIds) {
		if (query_species == null)
			query_species = UtilityManager.getSpecies(UtilityManager.getSpeciesIDFromGeneID(query_geneId));
		if (target_species == null)
			target_species = UtilityManager
					.getSpecies(UtilityManager.getSpeciesIDFromGeneID(target_geneIds.iterator().next()));

		GeneSimilarities gs = getSimilarities(query_species, target_species);
		SimilarityObject highestSim = null;

		HashMap<String, SimilarityObject> query_sims = gs.getSimilarities(query_geneId);
		if (query_sims != null) {
			for (Entry<String, SimilarityObject> e : query_sims.entrySet()) {
				if (target_geneIds.contains(e.getKey())) {
					if (highestSim == null) {
						highestSim = e.getValue();
					} else {
						if (highestSim.getMaximumIdentityScore() < e.getValue().getMaximumIdentityScore()) {
							highestSim = e.getValue();
						}
					}
				}
			}
		}

		return highestSim;

	}

	public void addSimilarityFile(Species s1, Species s2) {
		if (sims.containsKey(s1.getId() + "." + s2.getId()))
			return;
		File f = new File(simPath + s1.getId() + "." + s2.getId() + ".genesimilarities");
		if (!f.exists()) {
			f = new File(simPath + s2.getId() + "." + s1.getId() + ".genesimilarities");
			addSimilaritiesFile(f, s2, s1);
		} else {
			addSimilaritiesFile(f, s1, s2);
		}
	}

	public void addSimilaritiesFile(File similarityFile, Species sp1, Species sp2) {
		System.out.println("adding similarity " + sp1.getId() + ":" + sp2.getId() + " [needed memory atm: "
				+ ((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024d / 1024d
						/ 1024d * 1000d)) / 1000d
				+ "GB]");
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
		SimilarityObject so1 = new SimilarityObject(Double.parseDouble(split[2]), split[0], split[1]),
				so2 = new SimilarityObject(Double.parseDouble(split[2]), split[1], split[0]);
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
		SimilarityObject so = new SimilarityObject(Double.parseDouble(split[2]), split[0], split[1]);
		g.addSimilarity(split[0], split[1], so);
		g.addSimilarity(split[1], split[0], so);
		split = split[3].split(",");
		for (String s : split) {
			String[] prots = s.split(":");
			so.addProteinSimilarity(prots[0], prots[1], Double.parseDouble(prots[2]));
		}
	}

	/**
	 * 
	 * @param sp1
	 * @param sp2
	 * @return all NxMmappings in the two species
	 */
	public LinkedList<NxMmapping> getNxMmappings(Species sp1, Species sp2) {
		GeneSimilarities gs1 = getSimilarities(sp1, sp2), gs2 = getSimilarities(sp2, sp1);
		TreeSet<String> geneWithPartnerSp1 = new TreeSet<>(), geneWithPartnerSp2 = new TreeSet<>();
		geneWithPartnerSp1.addAll(gs1.getGenesWithPartner());
		geneWithPartnerSp2.addAll(gs2.getGenesWithPartner());

		int l = geneWithPartnerSp1.size();

		LinkedList<NxMmapping> nXm = new LinkedList<>();

		while (l > 0) {

			NxMmapping n = getNxMmapping(sp1, sp2, gs1, gs2, geneWithPartnerSp1.first());
			nXm.add(n);
			// System.out.println(n);
			geneWithPartnerSp1.removeAll(n.getGenesFromSpecies(true));
			geneWithPartnerSp2.removeAll(n.getGenesFromSpecies(false));

			l = geneWithPartnerSp1.size();
		}
		System.out.println(nXm.size());
		return nXm;
	}

	/**
	 * 
	 * @param currentGenesSp1
	 * @param currentGenesSp2
	 * @param newGenes
	 * @param gs1
	 * @param gs2
	 * @return the mapping cluster for genes given in newGenes
	 */
	public Pair<TreeSet<String>, TreeSet<String>> getNewMappingGenes(TreeSet<String> currentGenesSp1,
			TreeSet<String> currentGenesSp2, TreeSet<String> newGenes, GeneSimilarities gs1, GeneSimilarities gs2) {

		if (newGenes.isEmpty()) {
			return new Pair<TreeSet<String>, TreeSet<String>>(currentGenesSp1, currentGenesSp2);
		}

		TreeSet<String> newerGenes = new TreeSet<>();

		for (String s : newGenes) {
			HashMap<String, SimilarityObject> mappedToS = gs1.getSimilarities(s);
			for (String geneMappedToS : mappedToS.keySet()) {
				if (!currentGenesSp2.contains(geneMappedToS)) {
					newerGenes.add(geneMappedToS);
				}
			}
		}

		currentGenesSp1.addAll(newGenes);

		return getNewMappingGenes(currentGenesSp2, currentGenesSp1, newerGenes, gs2, gs1);
	}

	/**
	 * 
	 * @param species1
	 * @param species2
	 * @param gs1
	 * @param gs2
	 * @param startId
	 * @return NxMmapping for the two species containing gene startId and the
	 *         whole cluster mapped to it
	 */
	public NxMmapping getNxMmapping(Species species1, Species species2, GeneSimilarities gs1, GeneSimilarities gs2,
			String startId) {

		TreeSet<String> in = new TreeSet<>();
		in.add(startId);
		Pair<TreeSet<String>, TreeSet<String>> cluster = getNewMappingGenes(new TreeSet<String>(),
				new TreeSet<String>(), in, gs1, gs2);

		if (UtilityManager.getSpeciesIDFromGeneID(cluster.getKey().first()) != species1.getId()) {
			cluster = new Pair<TreeSet<String>, TreeSet<String>>(cluster.getValue(), cluster.getKey());
		}

		return new NxMmapping(species1, species2, cluster.getKey(), cluster.getValue());
	}

}
