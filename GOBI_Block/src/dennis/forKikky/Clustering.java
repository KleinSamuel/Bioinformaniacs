package dennis.forKikky;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeSet;

import dennis.similarities.GeneSimilarities;
import dennis.forKikky.KikkyNxMmapping;
import dennis.similarities.SimilarityObject;
import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;
import javafx.util.Pair;

public class Clustering {

	private Species sp1, sp2;
	// genes that have a count
	private HashSet<String> allowedGeneIdsInBothSpecies;

	public Clustering(Species sp1, Species sp2, HashSet<String> allowedGenes) {
		this.sp1 = sp1;
		this.sp2 = sp2;
		this.allowedGeneIdsInBothSpecies = allowedGenes;
	}

	/**
	 * 
	 * @param sp1
	 * @param sp2
	 * @return all NxMmappings in the two species only containing genesWith a
	 *         count > 0
	 */
	public LinkedList<KikkyNxMmapping> getNxMmappings() {
		GeneSimilarities gs1 = UtilityManager.getSimilarityHandler().getSimilarities(sp1, sp2),
				gs2 = UtilityManager.getSimilarityHandler().getSimilarities(sp2, sp1);
		TreeSet<String> geneWithPartnerSp1 = new TreeSet<>(), geneWithPartnerSp2 = new TreeSet<>();
		for (String s : gs1.getGenesWithPartner()) {
			// wenn gen erlaubt und einer der partner erlaubt
			boolean ok = false;
			if (allowedGeneIdsInBothSpecies.contains(s)) {
				HashMap<String, SimilarityObject> sims = gs1.getSimilarities(s);
				for (String partner : sims.keySet()) {
					if (allowedGeneIdsInBothSpecies.contains(partner)) {
						ok = true;
						geneWithPartnerSp2.add(partner);
					}
				}
				if (ok) {
					geneWithPartnerSp1.add(s);
				}
			}
		}

		int l = geneWithPartnerSp1.size();

		LinkedList<KikkyNxMmapping> nXm = new LinkedList<>();

		while (l > 0) {

			KikkyNxMmapping n = getNxMmapping(sp1, sp2, gs1, gs2, geneWithPartnerSp1.first());
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
	 * @return the mapping cluster for genes given in newGenes only allowing
	 *         genes with count
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
				if (!currentGenesSp2.contains(geneMappedToS) && allowedGeneIdsInBothSpecies.contains(geneMappedToS)) {
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
	public KikkyNxMmapping getNxMmapping(Species species1, Species species2, GeneSimilarities gs1, GeneSimilarities gs2,
			String startId) {

		TreeSet<String> in = new TreeSet<>();
		in.add(startId);
		Pair<TreeSet<String>, TreeSet<String>> cluster = getNewMappingGenes(new TreeSet<String>(),
				new TreeSet<String>(), in, gs1, gs2);

		if (UtilityManager.getSpeciesIDFromGeneID(cluster.getKey().first()) != species1.getId()) {
			cluster = new Pair<TreeSet<String>, TreeSet<String>>(cluster.getValue(), cluster.getKey());
		}

		return new KikkyNxMmapping(species1, species2, cluster.getKey(), cluster.getValue());
	}

}
