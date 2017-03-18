package dennis.GO;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;

public class GOHandler {

	private static HashMap<Species, GOmapping> mappings;

	// getGenes!!!! TODO add all genes above
	// question: save all genes to all nodes or calc everytimes

	// GOgraph
	private static Graph goGraph = null;

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

	public static TreeSet<String> getMappedGenes(Species s, String goTerm) {
		return getGOmapping(s).getGenesMappedToGoTerm(goTerm);
	}

	public static TreeSet<String> getMappedGenes(String goTerm) {
		TreeSet<String> ret = new TreeSet<>();
		for (Iterator<Species> speciesIt = UtilityManager.speciesIterator(); speciesIt.hasNext();) {
			ret.addAll(getMappedGenes(speciesIt.next(), goTerm));
		}
		return ret;
	}

	public static TreeSet<String> getMappedGenes(Species s, Collection<String> goTerms) {
		TreeSet<String> ret = new TreeSet<>();
		for (String term : goTerms) {
			ret.addAll(getMappedGenes(s, term));
		}
		return ret;
	}

	public static TreeSet<String> getMappedGenes(Collection<String> goTerms) {
		TreeSet<String> ret = new TreeSet<>();
		for (String term : goTerms) {
			ret.addAll(getMappedGenes(term));
		}
		return ret;
	}

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
