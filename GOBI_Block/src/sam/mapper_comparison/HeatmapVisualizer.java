package sam.mapper_comparison;

import java.io.File;
import java.io.IOException;

import debugStuff.DebugMessageFactory;

public class HeatmapVisualizer {

	/**
	 * Create a heatmap with R.
	 * 
	 * The script is located at /home/proj/biocluster/praktikum/genprakt/bioinformaniacs/sam/
	 * 
	 * @param script
	 * @param heatmapCSV
	 * @param outputDir
	 */
	public void createHeatmapWithR(File script, File heatmapCSV, String outputDir){
		RScriptCaller rc = new RScriptCaller(script, heatmapCSV, new File(outputDir));
		rc.run();
	}
	
	public class RScriptCaller implements Runnable {
		
		private File script, csv, output;
		public static final String PATH_TO_Rscript = "/home/proj/biosoft/software/R/R-3.3.0/bin/Rscript";
		
		public RScriptCaller(File script, File csv, File output) {
			this.script = script;
			this.csv = csv;
			this.output = output;
		}
		
		@Override
		public void run() {
			DebugMessageFactory.printInfoDebugMessage(true, "CREATE PLOT WITH R - "+script.getName());
			try {
				Process p = new ProcessBuilder(PATH_TO_Rscript, script.getAbsolutePath(), csv.getAbsolutePath(), output.getAbsolutePath()).inheritIO().start();
				DebugMessageFactory.printInfoDebugMessage(true, "CREATED PLOT WITH R - "+script.getName());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		HeatmapVisualizer hmv = new HeatmapVisualizer();
		
		File script = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"heatmap_example.R");
		File heatmapCSV = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"output/contextmap_limma_fc/CONTEXTMAP_LIMMA_heatmap.csv");
		
		String outputDir = HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"test2.png";
		
		hmv.createHeatmapWithR(script, heatmapCSV, outputDir);
	}
	
}
