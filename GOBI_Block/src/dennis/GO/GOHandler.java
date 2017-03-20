package dennis.GO;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;

public class GOHandler {

	private static HashMap<Species, GOmapping> mappings;

	// getGenes!!!! TODO add all genes above
	// question: save all genes to all nodes or calc everytimes

	// GOgraph
	private static Graph goGraph = null;

	/**
	 * 
	 * @param sp
	 * @return GOmapping containing all mappings for the given species
	 */
	public static GOmapping getGOmapping(Species sp) {
		if (mappings == null) {
			mappings = new HashMap<>();
		}
		GOmapping map = mappings.get(sp);
		if (map == null) {
			map = new GOmapping(UtilityManager.getConfig("go_mappings") + sp.getId() + ".gomappings");
			mappings.put(sp, map);
		}
		return map;
	}

	/**
	 * 
	 * @param s
	 * @param goTerm
	 * @return all genes directly mapped to goTerm(directly = most specific)
	 */
	public static TreeSet<String> getMappedGenes(Species s, String goTerm) {
		return getGOmapping(s).getGenesMappedToGoTerm(goTerm);
	}

	/**
	 * 
	 * @param s;
	 *            can be null -> calculated
	 * @param geneId
	 * @return all GOs directly mapped to gene(directly = most specific)
	 */
	public static TreeSet<String> getMappedGOterms(Species s, String geneId) {
		if (s == null) {
			s = UtilityManager.getSpecies(UtilityManager.getSpeciesIDFromGeneID(geneId));
		}
		return getGOmapping(s).getGOsMappedToGene(geneId);
	}

	/**
	 * 
	 * @param goTerm
	 * @return all genes mapped to the given goTerm(from all species)
	 */
	public static TreeSet<String> getMappedGenes(String goTerm) {
		TreeSet<String> ret = new TreeSet<>();
		for (Iterator<Species> speciesIt = UtilityManager.speciesIterator(); speciesIt.hasNext();) {
			ret.addAll(getMappedGenes(speciesIt.next(), goTerm));
		}
		return ret;
	}

	/**
	 * 
	 * @param s
	 * @param goTerms
	 * @return all genes directly mapped to the given goTerms
	 */
	public static TreeSet<String> getMappedGenes(Species s, Collection<String> goTerms) {
		TreeSet<String> ret = new TreeSet<>();
		for (String term : goTerms) {
			ret.addAll(getMappedGenes(s, term));
		}
		return ret;
	}

	/**
	 * @param s
	 * @param geneIds
	 * @return all terms directly mapped to the given genes
	 */
	public static TreeSet<String> getMappedGOterms(Species s, Collection<String> geneIds) {
		TreeSet<String> ret = new TreeSet<>();
		for (String gene : geneIds) {
			ret.addAll(getMappedGOterms(s, gene));
		}
		return ret;
	}

	/**
	 * 
	 * @param goTerms
	 * @return all genes directly mapped to the goTerms from all species
	 */
	public static TreeSet<String> getMappedGenes(Collection<String> goTerms) {
		TreeSet<String> ret = new TreeSet<>();
		for (String term : goTerms) {
			ret.addAll(getMappedGenes(term));
		}
		return ret;
	}

	/**
	 * 
	 * @param s:
	 *            can be null
	 * @param geneId
	 * @return
	 */
	public static HashMap<String, LinkedList<String>> getAllMappedGOs(Species s, String geneId) {
		HashMap<String, LinkedList<String>> ret = new HashMap<>();
		if (s == null) {
			s = UtilityManager.getSpecies(UtilityManager.getSpeciesIDFromGeneID(geneId));
		}
		for (String mappedTerm : getMappedGOterms(s, geneId)) {
			TermNode node = getGOgraph().getNode(mappedTerm);
			ret.put(mappedTerm, getTermsToRoot(node));
		}
		return ret;
	}

	/**
	 * @param direct:
	 *            the node to start from
	 * @return list of nodes from direct(incl) to root(incl)
	 */
	public static LinkedList<String> getTermsToRoot(TermNode direct) {
		LinkedList<String> ret = new LinkedList<>();
		ret.add(direct.getId());
		while (!direct.getEdges().isEmpty()) {
			direct = direct.getEdges().keySet().iterator().next();
			ret.add(direct.getId());
		}
		return ret;
	}

	/**
	 * will read the graph if not there yet
	 * 
	 * @return GOgraph
	 */
	public static Graph getGOgraph() {
		if (goGraph == null) {
			readGOgraph();
		}
		return goGraph;
	}

	public static void readGOgraph() {
		goGraph = OBOreader.readOBOFile(UtilityManager.getConfig("go_graph"));
	}

}
