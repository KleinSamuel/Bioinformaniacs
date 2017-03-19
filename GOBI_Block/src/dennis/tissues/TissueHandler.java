package dennis.tissues;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;

import dennis.utility_manager.Experiment;
import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;

/**
 * reads tissue mappings; loads into each species: paths to experiments per
 * tissue looks if data exists for a given species and tissue pair
 */
public class TissueHandler {

	private static HashSet<String> tissueList = null;
	private static HashMap<Species, HashMap<String, Tissue>> tissues = null;

	public static void readTissueMapping(Species sp) {
		HashMap<String, Tissue> ret = new HashMap<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(
					new File(UtilityManager.getConfig("tissue_mappings") + sp.getId() + ".tissuemapping")));
			String line = null, tissueName = null;
			String[] split;
			Tissue t;
			while ((line = br.readLine()) != null) {
				split = line.split("\t");
				tissueName = isTissueOfInterest(split[1].toLowerCase());
				if (tissueName != null) {
					t = ret.get(tissueName);
					if (t == null) {
						t = new Tissue(tissueName);
						ret.put(tissueName, t);
					}
					t.addExperiment(new Experiment(split[0], sp));
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!ret.isEmpty()) {
			tissues.put(sp, ret);
		}
	}

	public static String isTissueOfInterest(String tissueName) {
		if (tissueList == null) {
			readTissueList();
		}
		if (tissueName.contains("testes"))
			return "testis";
		for (String s : tissueList) {
			if (tissueName.contains(s))
				return s;
		}
		return null;
	}

	public static void readTissueList() {
		tissueList = new HashSet<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(UtilityManager.getConfig("tissue_list"))));
			String line = null;
			while ((line = br.readLine()) != null) {
				if (!line.equals("testes")) {
					tissueList.add(line);
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return a iterator over all tissueNames in alphabetical order
	 */
	public static Iterator<String> tissueNameIterator() {
		if (tissueList == null) {
			readTissueList();
		}
		TreeSet<String> ret = new TreeSet<>();
		ret.addAll(tissueList);
		return ret.iterator();
	}

	/**
	 * @return a iterator over all tissues in species s in alphabetical order
	 */
	public static Iterator<Tissue> tissueIterator(Species s) {
		TreeMap<String, Tissue> ret = new TreeMap<>();
		ret.putAll(getTissues(s));
		return ret.values().iterator();
	}

	/**
	 * @return map of tissues from species sp key: tissueName(testes was renamed
	 *         with testis) value: tissue
	 */
	public static HashMap<String, Tissue> getTissues(Species sp) {
		if (tissues == null) {
			tissues = new HashMap<>();
		}
		HashMap<String, Tissue> ret = tissues.get(sp);
		if (ret == null) {
			readTissueMapping(sp);
		}
		return tissues.get(sp);
	}

	/**
	 * @return Tissue of species sp could be null!! e.g.: 9593 lung
	 */
	public static Tissue getTissue(Species sp, String tissue) {
		return getTissues(sp).get(tissue);
	}

	/**
	 * @return all possible tissuePairs for a species
	 */
	public static LinkedList<TissuePair> tissuePairIterator(Species s) {
		LinkedList<TissuePair> tissuePairs = new LinkedList<>();
		TreeSet<Tissue> tissues = new TreeSet<>();
		tissues.addAll(getTissues(s).values());
		for (Iterator<Tissue> t1 = tissues.iterator(); t1.hasNext();) {
			Tissue tiss1 = t1.next();
			for (Iterator<Tissue> t2 = tissues.iterator(); t2.hasNext();) {
				Tissue tiss2 = t2.next();
				if (tiss2.getName().compareTo(tiss1.getName()) <= 0)
					continue;
				tissuePairs.add(new TissuePair(tiss1, tiss2));
			}
		}
		return tissuePairs;
	}

}
