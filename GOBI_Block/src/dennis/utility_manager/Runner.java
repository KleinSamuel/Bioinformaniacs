package dennis.utility_manager;

import java.util.Iterator;
import java.util.LinkedList;

import dennis.counter.CounterUtils;
import dennis.tissues.Tissue;

public class Runner {

	// GOgraph fixen

	public static void main(String[] args) {
		UtilityManager utils = new UtilityManager(UtilityManager.DefaultInputMapping, false, false, false);

		// for (NxMmapping m :
		// UtilityManager.getSimilarityHandler().getNxMmappings(UtilityManager.getSpecies(9031),
		// UtilityManager.getSpecies(9544))) {
		// for (ScoringMatrix sm : new BipartitMapping(m,
		// new ScoringFunction(UtilityManager.getSpecies(9031),
		// UtilityManager.getSpecies(9544)))
		// .getScoringMatrix()) {
		// System.out.println(sm.toString());
		// }
		// }

		// Fuzzy fuz = new Fuzzy(3d, 0.1d);
		// for (double d : fuz.getFuzzyArray(1.8d, 0.3d)) {
		// System.out.print(d + " ");
		// }
		// System.out.println();

		// Graph g = GOHandler.getGOgraph();
		// for (TermNode n : g.getTermNodes().values()) {
		// System.out.println(n.getId() + ": " + n.getName() + " " +
		// n.getNamespace());
		// }

		for (Iterator<Species> s = UtilityManager.speciesIterator(); s.hasNext();) {
			Species sp = s.next();
			for (Iterator<Tissue> tissueIt = UtilityManager.tissueIterator(sp); tissueIt.hasNext();) {
				Tissue t = tissueIt.next();
				for (String m : UtilityManager.mapperList()) {
					LinkedList<String> tissueAvg = new LinkedList<>();
					for (Experiment e : t.getExperiments()) {
						if (!UtilityManager.getExperimentNamesWithMissingBams().contains(e.getName())) {
							String fileName = UtilityManager.getConfig("output_directory") + sp.getId() + "/"
									+ t.getName() + "/" + e.getName() + "/" + m + "/gene.counts";
							tissueAvg.add(fileName);
						}
					}
					CounterUtils.createAverageCountFile(tissueAvg, UtilityManager.getConfig("output_directory")
							+ sp.getId() + "/" + t.getName() + "/" + m + "_tissue_average.counts");
				}
			}
		}

		// for (Iterator<Species> speciesIt = UtilityManager.speciesIterator();
		// speciesIt.hasNext();) {
		// Species s = speciesIt.next();
		// for (TissuePair tissuePair : TissueHandler.tissuePairIterator(s)) {
		// for (String mapper : UtilityManager.mapperList()) {
		// LinkedList<String> fileCond1 = new LinkedList<>(), fileCond2 = new
		// LinkedList<>();
		// fileCond1.add(UtilityManager.getConfig("output_directory") +
		// s.getId() + "/"
		// + tissuePair.getKey().getName() + "/" + mapper +
		// "_tissue_average.counts");
		// fileCond2.add(UtilityManager.getConfig("output_directory") +
		// s.getId() + "/"
		// + tissuePair.getValue().getName() + "/" + mapper +
		// "_tissue_average.counts");
		// EBUtils.runEnrichment(fileCond1, fileCond2,
		// UtilityManager.getConfig("enrichment_output") + s.getId() +
		// "/tissue_mix/"
		// + tissuePair.getKey() + "_" + tissuePair.getValue() + "/" + mapper +
		// "/",
		// tissuePair.getKey() + "_" + tissuePair.getValue(), false);
		// }
		// }
		// }
	}

}
