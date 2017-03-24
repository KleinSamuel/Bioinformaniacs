package dennis.utility_manager;

import dennis.analysis.BipartitMapping;
import dennis.analysis.ScoringFunction;
import dennis.analysis.ScoringMatrix;
import dennis.similarities.NxMmapping;

public class Runner {

	// GOgraph fixen

	public static void main(String[] args) {
		UtilityManager utils = new UtilityManager(UtilityManager.DefaultInputMapping, false, false, false);

		for (NxMmapping m : UtilityManager.getSimilarityHandler().getNxMmappings(UtilityManager.getSpecies(9031),
				UtilityManager.getSpecies(9544))) {
			for (ScoringMatrix sm : new BipartitMapping(m,
					new ScoringFunction(UtilityManager.getSpecies(9031), UtilityManager.getSpecies(9544)))
							.getScoringMatrix()) {
				System.out.println(sm.toString());
			}
		}

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

		// for (Iterator<Species> s = UtilityManager.speciesIterator();
		// s.hasNext();) {
		// Species sp = s.next();
		// for (Iterator<Tissue> tissueIt = UtilityManager.tissueIterator(sp);
		// tissueIt.hasNext();) {
		// Tissue t = tissueIt.next();
		// for (String m : UtilityManager.mapperList()) {
		// LinkedList<String> tissueAvg = new LinkedList<>();
		// for (Experiment e : t.getExperiments()) {
		// String fileName = UtilityManager.getConfig("output_directory") +
		// sp.getId() + "/" + t.getName()
		// + "/" + e.getName() + "/" + m + "/gene.counts";
		// if (new File(fileName).exists()) {
		// tissueAvg.add(fileName);
		// }
		// }
		// CounterUtils.createAverageCountFile(tissueAvg,
		// UtilityManager.getConfig("output_directory")
		// + sp.getId() + "/" + t.getName() + "/" + m +
		// "_tissue_average.counts");
		// }
		// }
		// }

		// for (Iterator<Species> speciesIt = UtilityManager.speciesIterator();
		// speciesIt.hasNext();) {
		// EBUtils.runEBForAllTissuePairsAndMappers(speciesIt.next());
		// }

		// int i = 1, toDo = Integer.parseInt(args[0]);
		//
		// for (Iterator<Species> speciesIt = UtilityManager.speciesIterator();
		// speciesIt.hasNext();) {
		// Species s = speciesIt.next();
		// for (Iterator<Tissue> tissueIt = UtilityManager.tissueIterator(s);
		// tissueIt.hasNext();) {
		// Tissue t = tissueIt.next();
		// for (Experiment experiment : t.getExperiments()) {
		// for (Iterator<String> mapperIt = UtilityManager.mapperIterator();
		// mapperIt.hasNext();) {
		// if (i == toDo) {
		// BamFileReader bfr = new BamFileReader(s, t.getName(), experiment,
		// mapperIt.next());
		// bfr.readBAMFile();
		// return;
		// } else {
		// mapperIt.next();
		// i++;
		// }
		// }
		// }
		// }
		// }
	}

}
