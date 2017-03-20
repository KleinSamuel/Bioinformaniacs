package dennis.utility_manager;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;

import dennis.counter.CounterUtils;
import dennis.tissues.Tissue;

public class Runner {

	// count bam files

	// TODO
	// counts noch mal laufen lassen mit standardChrs
	// GO mappings testen / �berlegen ob zur wurzel mitgespeichert wird oder
	// immer neu berechnet
	// den anderen einen Beispiel aufruf schreiben
	// fuzzy stuff

	public static void main(String[] args) {
		UtilityManager utils = new UtilityManager(UtilityManager.DefaultInputMapping, false, false, false);

		for (Iterator<Species> s = UtilityManager.speciesIterator(); s.hasNext();) {
			Species sp = s.next();
			for (Iterator<Tissue> tissueIt = UtilityManager.tissueIterator(sp); tissueIt.hasNext();) {
				Tissue t = tissueIt.next();
				for (String m : UtilityManager.mapperList()) {
					LinkedList<String> tissueAvg = new LinkedList<>();
					for (Experiment e : t.getExperiments()) {
						String fileName = UtilityManager.getConfig("output_directory") + sp.getId() + "/" + t.getName()
								+ "/" + e.getName() + "/" + m + "/gene.counts";
						if (new File(fileName).exists()) {
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
