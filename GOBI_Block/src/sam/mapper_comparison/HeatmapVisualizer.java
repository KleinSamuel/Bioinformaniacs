package sam.mapper_comparison;

import java.io.File;
import java.io.IOException;

import debugStuff.DebugMessageFactory;
import sam.utils.HeatmapFromFileReader;

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
		
		String ma = "contextmap";
		String me = "edger";
		
		Mapper mapper = Mapper.STAR.getMapperForString(ma);
		DEmethods method = DEmethods.DESEQ.getMethodForString(me);
		
		File script = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"heatmap_example.R");
		
		File heatmap = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"output_new/"+ma+"_"+me+"_fc/"+ma.toUpperCase()+"_"+me.toUpperCase()+"_heatmap.content");
		File heatmapInfo = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"output_new/"+ma+"_"+me+"_fc/"+ma.toUpperCase()+"_"+me.toUpperCase()+"_heatmap.info");
		File heatmapCSV = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"output_new/"+ma+"_"+me+"_fc/"+ma.toUpperCase()+"_"+me.toUpperCase()+"_heatmap.csv");
		
		HeatmapFromFileReader hmf = new HeatmapFromFileReader();
		hmf.readHeatmapIntoCSV(heatmap, heatmapInfo, heatmapCSV);
		
		String outputDir = HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+ma+"_"+me+".png";
		
		hmv.createHeatmapWithR(script, heatmapCSV, outputDir);
	}
	
}
