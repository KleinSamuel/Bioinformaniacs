package sam.mapper_comparison;

import java.util.ArrayList;

import dennis.enrichment.EBUtils;
import dennis.utility_manager.UtilityManager;

public class TestRunner {

	public static void main(String[] args) {
		
		UtilityManager um = new UtilityManager("/home/proj/biocluster/praktikum/genprakt/bioinformaniacs/config.txt", false, false, false);
		
		ArrayList<String> cond1 = new ArrayList<>();
		cond1.add(UtilityManager.getConfig("output_directory")+"10090/brain/SRR306757/contextmap/gene.counts");
		cond1.add(UtilityManager.getConfig("output_directory")+"10090/brain/SRR594393/contextmap/gene.counts");
		
		ArrayList<String> cond2 = new ArrayList<>();
		cond2.add(UtilityManager.getConfig("output_directory")+"10090/liver/SRR306772/contextmap/gene.counts");
		cond2.add(UtilityManager.getConfig("output_directory")+"10090/liver/SRR306774/contextmap/gene.counts");
		
		EBUtils.runEnrichment(cond1, cond2, "/home/k/kleins/Desktop/GOBI_OUT/", "test", false);
		
	}
	
}
