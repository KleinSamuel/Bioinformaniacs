package dennis.analysis;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import dennis.analysis.scoring.Scoring;
import dennis.analysis.scoring.ScoringObject;
import dennis.enrichment.EnrichmentAnalysisUtils;
import dennis.enrichment.GeneObject;
import dennis.similarities.NxMmapping;
import dennis.tissues.TissueHandler;
import dennis.tissues.TissuePair;
import dennis.util.GenePair;
import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;

public class InputDataPreparator {

	public static boolean IGNORE_NEGATIVE_SCORES = true;
	public static boolean IGNORE_GENES_WITHOUT_ORTHOLOGUE = true;
	public static double MIN_PERCENTAGE_OF_GENE_OCCURENCES_IN_TPS = 0d;
	public static int MIN_OCCURENCES_IN_TPS;
	public static int minSize = 2;

	private Species query_species, target_species;
	private TreeMap<TissuePair, TreeMap<String, GeneObject>> deFilesSpecies1 = null, deFilesSpecies2 = null;
	private TreeSet<TissuePair> tissuePairs = null;
	private String mapper, deMethod;

	private static HashSet<String> genesToBeAnalyzed = null;

	private Clustering cluster;

	private Scoring scoring;

	private SeqIdVsFuzzyComparison comp;

	public int clusterCount = 0;

	/**
	 * 
	 * @param query_species
	 * @param target_species
	 * @param mapper
	 * @param deMethod
	 * @param ignoreNegativeScores
	 *            default: true
	 * @param minimalGeneOccurencePercentage
	 *            if==null: 50%; example:0.8d or 1d
	 */
	public InputDataPreparator(Species query_species, Species target_species, String mapper, String deMethod,
			Boolean ignoreNegativeScores, Double minimalGeneOccurencePercentage) {
		this.query_species = query_species;
		this.target_species = target_species;
		this.mapper = mapper;
		this.deMethod = deMethod;
		if (ignoreNegativeScores != null)
			IGNORE_NEGATIVE_SCORES = ignoreNegativeScores;
		if (minimalGeneOccurencePercentage != null)
			MIN_PERCENTAGE_OF_GENE_OCCURENCES_IN_TPS = minimalGeneOccurencePercentage;
		tissuePairs = new TreeSet<>();
		tissuePairs.addAll(TissueHandler.tissuePairIterator(query_species));
		tissuePairs.retainAll(TissueHandler.tissuePairIterator(target_species));

		MIN_OCCURENCES_IN_TPS = (int) (tissuePairs.size() * MIN_PERCENTAGE_OF_GENE_OCCURENCES_IN_TPS);

		deFilesSpecies1 = new TreeMap<>();
		deFilesSpecies2 = new TreeMap<>();
		readDEfiles(IGNORE_GENES_WITHOUT_ORTHOLOGUE);

		if (deFilesSpecies1.isEmpty() || deFilesSpecies2.isEmpty()) {
			System.out.println("ERRRRROOOOR");
			System.exit(1);
		}

		genesToBeAnalyzed = calculateAllGenesInAtLeastXTpsInBothSpecies();

		// genesToBeAnalyzed = new HashSet<>();
		// calculateAllGenesInEB(deFilesSpecies1);
		// calculateAllGenesInEB(deFilesSpecies2);
		System.out.println(
				"genes to be analyzed: " + genesToBeAnalyzed.size() + " in " + tissuePairs.size() + " tissuePairs");

		cluster = new Clustering(query_species, target_species, genesToBeAnalyzed);

		scoring = new Scoring(this);

		comp = new SeqIdVsFuzzyComparison(UtilityManager.getConfig("clustering_output") + query_species.getId() + "_"
				+ target_species.getId() + "/" + mapper + "/" + deMethod + "/").open();

		compareScoringMethods();

		comp.close();
	}

	public TreeMap<TissuePair, TreeMap<String, GeneObject>> getDEfilesSpecies1() {
		return deFilesSpecies1;
	}

	public TreeMap<TissuePair, TreeMap<String, GeneObject>> getDEfilesSpecies2() {
		return deFilesSpecies2;
	}

	private void compareScoringMethods() {
		LinkedList<NxMmapping> mappings = cluster.getNxMmappings();
		clusterCount = mappings.size();
		int analyzedClusters = 1;
		for (NxMmapping map : mappings) {
			if (map.getGenesFromSpecies(true).size() < minSize && map.getGenesFromSpecies(false).size() < minSize)
				continue;
			System.out.println("[" + analyzedClusters + " | " + clusterCount + "] clusters analyzed");

			// HashMap<String, BipartiteMatching> matchingsForScoringFunctions =
			// score
			// .getBipartiteMatchingsForAllScoringFunctions();
			BipartiteMatching seqId = scoring.getBestBipartiteMatching("seqId", map, false);
			BipartiteMatching fuzzy = scoring.getBestBipartiteMatching("fuzzy", map, false);
			BipartiteMatching greedySeqId = scoring.getBestBipartiteMatching("seqId", map, true);
			BipartiteMatching greedyFuzzy = scoring.getBestBipartiteMatching("fuzzy", map, true);

			// differenceStats.addMatchingDifferences(map,
			// matchingsForScoringFunctions);
			for (Entry<GenePair, ScoringObject> gp : seqId.getMatches().entrySet()) {
				comp.writeSequenceIdentityOptimizedGenePair(analyzedClusters, gp.getKey(), gp.getValue().getScore(),
						fuzzy.getCostMatrix()[map.getIndicesOfGenes().get(gp.getKey().getKey())][map.getIndicesOfGenes()
								.get(gp.getKey().getValue())] * -1);
			}

			for (Entry<GenePair, ScoringObject> gp : fuzzy.getMatches().entrySet()) {
				comp.writeExpressionOptimizedGenePair(analyzedClusters, gp.getKey(),
						seqId.getCostMatrix()[map.getIndicesOfGenes().get(gp.getKey().getKey())][map.getIndicesOfGenes()
								.get(gp.getKey().getValue())] * -1,
						gp.getValue().getScore());
			}

			for (Entry<GenePair, ScoringObject> gp : greedySeqId.getMatches().entrySet()) {
				comp.writeGreedySequenceIdentityOptimizedGenePair(analyzedClusters, gp.getKey(),
						gp.getValue().getScore(), scoring.getFuzzyScoring().score(gp.getKey()));
			}

			for (Entry<GenePair, ScoringObject> gp : greedyFuzzy.getMatches().entrySet()) {
				comp.writeGreedyExpressionOptimizedGenePair(analyzedClusters, gp.getKey(),
						scoring.getSeqIdScoring().score(gp.getKey()), gp.getValue().getScore());
			}
			analyzedClusters++;
		}
	}

	public HashSet<String> calculateAllGenesInAtLeastXTpsInBothSpecies() {
		HashSet<String> genesOfInterest = new HashSet<>();
		genesOfInterest.addAll(calculateAllGenesInAtLeastXTps(query_species, target_species, deFilesSpecies1));
		genesOfInterest.addAll(calculateAllGenesInAtLeastXTps(target_species, query_species, deFilesSpecies2));
		return genesOfInterest;
	}

	/**
	 * if MIN_PERCENTAGE = 1: all genes that are not in all tps will be removed
	 * 
	 * @param sp_query
	 * @param sp_target
	 * @param deFilesSpecies
	 * @return
	 */
	public TreeSet<String> calculateAllGenesInAtLeastXTps(Species sp_query, Species sp_target,
			TreeMap<TissuePair, TreeMap<String, GeneObject>> deFilesSpecies) {

		TreeSet<String> genes = new TreeSet<>();

		LinkedList<String> geneIds = null;
		if (IGNORE_GENES_WITHOUT_ORTHOLOGUE) {
			geneIds = new LinkedList<>(UtilityManager.getSimilarityHandler().getSimilarities(sp_query, sp_target)
					.getGenesWithPartnerSorted());
		} else {
			geneIds = sp_query.getGeneIds();
		}

		for (String gene : geneIds) {
			int tps = 0;
			LinkedList<TissuePair> geneIsMissingIn = new LinkedList<>();
			for (Entry<TissuePair, TreeMap<String, GeneObject>> e : deFilesSpecies.entrySet()) {
				GeneObject go = e.getValue().get(gene);
				if (go == null) {
					geneIsMissingIn.add(e.getKey());
				} else {
					tps++;
				}
			}
			if (tps >= MIN_OCCURENCES_IN_TPS) {
				for (TissuePair tp : geneIsMissingIn) {

					deFilesSpecies.get(tp).put(gene, new GeneObject(gene, -1, -1, -1));
				}
				genes.add(gene);
			} else {
				for (TissuePair tp : tissuePairs) {
					deFilesSpecies.get(tp).remove(gene);
				}
			}
		}

		return genes;

	}

	private void calculateAllGenesInEB(TreeMap<TissuePair, TreeMap<String, GeneObject>> deFilesSpecies) {

		HashSet<String> givenSpeciesGenes = null;

		for (TreeMap<String, GeneObject> tree : deFilesSpecies.values()) {
			if (givenSpeciesGenes == null) {
				givenSpeciesGenes = new HashSet<>();
				givenSpeciesGenes.addAll(tree.keySet());
			} else {
				givenSpeciesGenes.retainAll(tree.keySet());
			}
		}

		genesToBeAnalyzed.addAll(givenSpeciesGenes);
	}

	public void readDEfiles(boolean eliminateGenesWithoutOrthologue) {
		for (TissuePair tp : tissuePairs) {
			TreeMap<String, GeneObject> in = new TreeMap<>();
			for (GeneObject go : EnrichmentAnalysisUtils
					.readDEfile(UtilityManager.getConfig("enrichment_output") + query_species.getId() + "/"
							+ tp.toString() + "/" + mapper + "/" + tp.toString() + "." + deMethod)) {
				if (!eliminateGenesWithoutOrthologue || UtilityManager.getSimilarityHandler()
						.getSimilarities(query_species, target_species).getSimilarities(go.getName()) != null)
					in.put(go.getName(), go);
			}

			deFilesSpecies1.put(tp, in);

			in = new TreeMap<>();

			for (GeneObject go : EnrichmentAnalysisUtils
					.readDEfile(UtilityManager.getConfig("enrichment_output") + target_species.getId() + "/"
							+ tp.toString() + "/" + mapper + "/" + tp.toString() + "." + deMethod)) {
				if (!eliminateGenesWithoutOrthologue || UtilityManager.getSimilarityHandler()
						.getSimilarities(target_species, query_species).getSimilarities(go.getName()) != null)
					in.put(go.getName(), go);
			}

			deFilesSpecies2.put(tp, in);
		}
	}

	public Species getQuerySpecies() {
		return query_species;
	}

	public Species getTargetSpecies() {
		return target_species;
	}

	public TreeSet<TissuePair> getTissuePairs() {
		return tissuePairs;
	}

	public static HashSet<String> getAllowedGenes() {
		return genesToBeAnalyzed;
	}

}
