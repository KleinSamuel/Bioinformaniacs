package dennis.utility_manager;

import java.util.Iterator;

import dennis.bam.BamFileReader;
import dennis.tissues.TissueHandler;

public class Runner {

	// count bam files

	// TODO
	// counts noch mal laufen lassen mit standardChrs
	// rechte 10116/SRR594445/hisat.bam

	public static void main(String[] args) {
		UtilityManager utils = new UtilityManager(UtilityManager.DefaultInputMapping, false, false, false);
		// for (Iterator<Species> speciesIt = UtilityManager.speciesIterator();
		// speciesIt.hasNext();) {
		// EBUtils.runEBForAllTissuePairsAndMappers(speciesIt.next());
		// }

		Experiment e = TissueHandler.getTissue(UtilityManager.getSpecies(10116), "testis").getExperiment("SRR594445");

		BamFileReader bam = new BamFileReader(UtilityManager.getSpecies(10116), "testis", e, "hisat");
		bam.readBAMFile();

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
