package dennis.utility_manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import dennis.GO.GOHandler;
import dennis.counter.CounterUtils;
import dennis.similarities.SimilarityHandler;
import dennis.tissues.Tissue;
import dennis.tissues.TissueHandler;
import dennis.tissues.TissuePair;

public class UtilityManager {

	public static final String DefaultInputMapping = "/home/proj/biocluster/praktikum/genprakt/bioinformaniacs/d‰‰‰hn/config.txt";
	private static HashMap<String, String> configs;
	private static HashMap<String, Species> speciesByName;
	private static HashMap<Integer, Species> speciesById;
	private static SimilarityHandler similarities;
	private GOHandler goUtils;
	private CounterUtils counterUtils;
	private TissueHandler tissueHandler;

	/*
	 * input: inputMappingFile (if null: default is taken) returns an object
	 * containing: - the GO graph (getGO()) - a container holding
	 * SpeciesObjects(getSpecies(String species)):
	 * 
	 * speciesObject contain: - gene-go mappings(getMappedGOterms(String
	 * geneId)) - go-gene mappings(getMappedGenes(String goId)) -
	 * paralogMappings(getParalogs(String geneId)) -
	 * orthologMappings(getOrthologs(String geneId))
	 * 
	 * this class offers utilities like: - returning all genes from all species
	 * mapped to a go term - ...
	 */

	public UtilityManager(String utilityMapping, boolean preloadGOgraph, boolean preloadGOmappings,
			boolean preloadSimilarities) {
		readUtilityMapping();
		readSpeciesMapping();
		similarities = new SimilarityHandler();
		goUtils = new GOHandler();
		counterUtils = new CounterUtils();
		tissueHandler = new TissueHandler();
		if (preloadGOgraph) {
			GOHandler.getGOgraph();
		}
		if (preloadGOmappings) {
			readGOmappings();
		}
		if (preloadSimilarities) {
			for (Species s : speciesById.values()) {
				for (Species sp : speciesById.values()) {
					similarities.addSimilarityFile(s, sp);
				}
			}
		}

	}

	public static SimilarityHandler getSimilarityHandler() {
		return similarities;
	}

	public static Species getSpecies(int id) {
		return speciesById.get(id);
	}

	public static Species getSpecies(String name) {
		return speciesByName.get(name);
	}

	public static void readUtilityMapping() {
		configs = new HashMap<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(DefaultInputMapping)));
			String line = null;
			while ((line = br.readLine()) != null) {
				if (!line.startsWith("#")) {
					String[] split = line.split("\t");
					configs.put(split[0], split[1]);
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static String getConfig(String key) {
		if (configs == null) {
			readUtilityMapping();
		}
		return configs.get(key);
	}

	public void readGOmappings() {
		for (Species sp : speciesById.values()) {
			GOHandler.getGOmapping(sp);
		}
	}

	public void readSpeciesMapping() {
		speciesById = new HashMap<>();
		speciesByName = new HashMap<>();
		try {
			String line = null;
			BufferedReader br = new BufferedReader(new FileReader(new File(configs.get("tax_mapping"))));
			while ((line = br.readLine()) != null) {
				String[] split = line.split("\t");
				int id = Integer.parseInt(split[0]);
				Species species = new Species(id, split[1].toLowerCase(), configs.get("gtfs") + "/" + split[2],
						configs.get("standard_chromosomes") + id + ".standardchrs",
						configs.get("tissue_mappings") + id + ".tissuemapping");
				speciesById.put(id, species);
				speciesByName.put(species.getName(), species);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * return species iterator (id sorted)
	 */
	public static Iterator<Species> speciesIterator() {
		return new TreeSet<Species>(speciesById.values()).iterator();
	}

	/*
	 * mapperIterator
	 */
	public static Iterator<String> mapperIterator() {
		TreeSet<String> mapper = new TreeSet<>();
		mapper.add("contextmap");
		mapper.add("tophat2");
		mapper.add("star");
		mapper.add("hisat");
		return mapper.iterator();
	}

	public static LinkedList<String> mapperList() {
		LinkedList<String> mapper = new LinkedList<>();
		mapper.add("contextmap");
		mapper.add("tophat2");
		mapper.add("star");
		mapper.add("hisat");
		return mapper;
	}

	/*
	 * DEmethod iterator
	 */
	public static Iterator<String> DEmethodIterator() {
		LinkedList<String> methods = new LinkedList<>();
		methods.add("limma");
		methods.add("DESeq");
		methods.add("edgeR");
		return methods.iterator();
	}

	/*
	 * returns tissueName iterator (alphabetical order)
	 */
	public static Iterator<String> tissueNameIterator() {
		return TissueHandler.tissueNameIterator();
	}

	/*
	 * returns tissue iterator for the given species (alphabetical order)
	 */
	public static Iterator<Tissue> tissueIterator(Species s) {
		return TissueHandler.tissueIterator(s);
	}

	/*
	 * returns iterator over all possible tissuePairs for a species
	 */
	public static Iterator<TissuePair> tissuePairIterator(Species s) {
		return TissueHandler.tissuePairIterator(s).iterator();
	}

	public static int getSpeciesIDFromGeneID(String geneId) {
		if (geneId.startsWith("ENSMUSG"))
			return 10090;
		if (geneId.startsWith("ENSGALG"))
			return 9031;
		if (geneId.startsWith("ENSG"))
			return 9606;
		if (geneId.startsWith("ENSRNOG"))
			return 10116;
		if (geneId.startsWith("ENSPTRG"))
			return 9598;
		if (geneId.startsWith("ENSMMUG"))
			return 9544;
		if (geneId.startsWith("ENSGGOG"))
			return 9593;
		if (geneId.startsWith("ENSBTAG"))
			return 9913;
		return -1;
	}

}
