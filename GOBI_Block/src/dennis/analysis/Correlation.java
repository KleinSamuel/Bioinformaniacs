package dennis.analysis;

import dennis.enrichment.EnrichmentAnalysisUtils;
import dennis.utility_manager.Species;

public class Correlation {

	private String geneId;
	private Species species1, species2;

	public Correlation(Species s1, Species s2, String gene) {
		species1 = s1;
		species2 = s2;
		geneId = gene;
	}

	public static double correlation() {
		EnrichmentAnalysisUtils.readDEfile(filePath)
	}

}
