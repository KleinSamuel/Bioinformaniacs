package dennis.analysis;

import java.util.HashMap;
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
	public static double MIN_PERCENTAGE_OF_GENE_OCCURENCES_IN_TPS = 0.5d;
	public static int MIN_OCCURENCES_IN_TPS;

	private Species query_species, target_species;
	private TreeMap<TissuePair, TreeMap<String, GeneObject>> deFilesSpecies1 = null, deFilesSpecies2 = null;
	private TreeSet<TissuePair> tissuePairs = null;
	private String mapper, deMethod;

	private HashSet<String> genesToBeAnalyzed = null;

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
	 *            if==null: 50%
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

		MIN_OCCURENCES_IN_TPS = (int) (tissuePairs.size() * MIN_PERCENTAGE_OF_GENE_OCCURENCES_IN_TPS) + 1;

		deFilesSpecies1 = new TreeMap<>();
		deFilesSpecies2 = new TreeMap<>();
		readDEfiles();

		if (deFilesSpecies1.isEmpty() || deFilesSpecies2.isEmpty()) {
			System.out.println("ERRRRROOOOR");
			System.exit(1);
		}

		genesToBeAnalyzed = new HashSet<>();
		calculateAllGenesInEB(deFilesSpecies1);
		calculateAllGenesInEB(deFilesSpecies2);
		System.out.println(
				"genes to be analyzed: " + genesToBeAnalyzed.size() + " in " + tissuePairs.size() + " tissuePairs");

		cluster = new Clustering(query_species, target_species, genesToBeAnalyzed);

		scoring = new Scoring(this);

		comp = new SeqIdVsFuzzyComparison(UtilityManager.getConfig("clustering_output") + query_species.getId() + "_"
				+ target_species.getId() + "/" + mapper + "/" + deMethod + "/").open();

		compareScoringMethods();

		comp.close();
		// differenceStats.close();
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
			System.out.println("[" + analyzedClusters + " | " + clusterCount + "] clusters analyzed");

			// HashMap<String, BipartiteMatching> matchingsForScoringFunctions =
			// score
			// .getBipartiteMatchingsForAllScoringFunctions();
			BipartiteMatching seqId = scoring.getBestBipartiteMatching("seqId", map);
			BipartiteMatching fuzzy = scoring.getBestBipartiteMatching("fuzzy", map);

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
			analyzedClusters++;
		}
	}

	public TreeSet<String> calculateAllGenesInEB() {
		HashMap<String, Integer> number_tps_gene_occures_in = new HashMap<>();
		for (TissuePair tp : tissuePairs) {
			for (String gene : deFilesSpecies1.get(tp).keySet()) {
				Integer number_tps = number_tps_gene_occures_in.get(gene);
				if (number_tps == null) {
					number_tps_gene_occures_in.put(gene, 1);
				}
			}
			for (String gene : deFilesSpecies2.get(tp).keySet()) {

			}
		}
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

	public void readDEfiles() {
		for (TissuePair tp : tissuePairs) {
			TreeMap<String, GeneObject> in = new TreeMap<>();
			for (GeneObject go : EnrichmentAnalysisUtils
					.readDEfile(UtilityManager.getConfig("enrichment_output") + query_species.getId() + "/"
							+ tp.toString() + "/" + mapper + "/" + tp.toString() + "." + deMethod)) {
				in.put(go.getName(), go);
			}

			deFilesSpecies1.put(tp, in);

			in = new TreeMap<>();

			for (GeneObject go : EnrichmentAnalysisUtils
					.readDEfile(UtilityManager.getConfig("enrichment_output") + target_species.getId() + "/"
							+ tp.toString() + "/" + mapper + "/" + tp.toString() + "." + deMethod)) {
				in.put(go.getName(), go);
			}

			deFilesSpecies2.put(tp, in);
		}
	}

	public static BipartiteMatchingDifferenceObject getDifferenceBetweenBipartits(BipartiteMatching bi1,
			BipartiteMatching bi2) {
		return new BipartiteMatchingDifferenceObject(bi1, bi2);
	}

	public Species getQuerySpecies() {
		return query_species;
	}

	public Species getTargetSpecies() {
		return target_species;
	}

}
