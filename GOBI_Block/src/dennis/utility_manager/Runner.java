package dennis.utility_manager;

import java.util.Iterator;

import dennis.bam.BamFileReader;
import dennis.tissues.Tissue;

public class Runner {

	// count bam files

	// TODO
	// counts noch mal laufen lassen mit standardChrs
	// GO mappings testen / überlegen ob zur wurzel mitgespeichert wird oder
	// immer neu berechnet
	// den anderen einen Beispiel aufruf schreiben
	// fuzzy stuff

	public static void main(String[] args) {
		UtilityManager utils = new UtilityManager(UtilityManager.DefaultInputMapping, false, false, false);

		// Species human = UtilityManager.getSpecies(9606);

		// for (Iterator<Tissue> tissueIt =
		// UtilityManager.tissueIterator(human); tissueIt.hasNext();) {
		// Tissue t = tissueIt.next();
		// for (Experiment e : t.getExperiments()) {
		// LinkedList<String> mapperAvg = new LinkedList<>();
		// for (String m : UtilityManager.mapperList()) {
		// mapperAvg.add(UtilityManager.getConfig("output_directory") +
		// human.getId() + "/" + t.getName() + "/"
		// + e.getName() + "/" + m + "/gene.counts");
		// }
		// CounterUtils.createAverageCountFile(mapperAvg,
		// UtilityManager.getConfig("output_directory")
		// + human.getId() + "/" + t.getName() + "/" + e.getName() +
		// "/mapperAverage.counts");
		// }
		// }

		// for (Iterator<Species> speciesIt = UtilityManager.speciesIterator();
		// speciesIt.hasNext();) {
		// EBUtils.runEBForAllTissuePairsAndMappers(speciesIt.next());
		// }

		int i = 1, toDo = Integer.parseInt(args[0]);

		for (Iterator<Species> speciesIt = UtilityManager.speciesIterator(); speciesIt.hasNext();) {
			Species s = speciesIt.next();
			for (Iterator<Tissue> tissueIt = UtilityManager.tissueIterator(s); tissueIt.hasNext();) {
				Tissue t = tissueIt.next();
				for (Experiment experiment : t.getExperiments()) {
					for (Iterator<String> mapperIt = UtilityManager.mapperIterator(); mapperIt.hasNext();) {
						if (i == toDo) {
							BamFileReader bfr = new BamFileReader(s, t.getName(), experiment, mapperIt.next());
							bfr.readBAMFile();
							return;
						} else {
							mapperIt.next();
							i++;
						}
					}
				}
			}
		}
	}

}
