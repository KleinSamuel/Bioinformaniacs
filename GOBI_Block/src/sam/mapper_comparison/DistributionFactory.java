package sam.mapper_comparison;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.math3.util.Pair;

import debugStuff.DebugMessageFactory;
import dennis.enrichment.GeneObject;
import dennis.utility_manager.UtilityManager;
import sam.mapper_comparison.HeatmapFactory.ComparablePairTMP;
import sam.utils.ExternalWriter;

public class DistributionFactory {

	public void getDistributionOfPvalsOfTissuePairs(Mapper mapper, DEmethods method){
		
		String content = HeatmapFactory.PATH_TO_FINAL_HEATMAPS+mapper.getPathName().toUpperCase()+"_"+method.getPathName().toUpperCase()+"_heatmap.content";
		
		TreeMap<Double, Integer> distribMap = new TreeMap<>();
		
		try {
			
			BufferedReader contentReader = new BufferedReader(new FileReader(content));
			
			String line = null;
			
			DecimalFormat df = new DecimalFormat("#.##");
			df.setRoundingMode(RoundingMode.HALF_UP);
			
			while((line = contentReader.readLine()) != null){
				
				String[] lineArray = line.split("\t");
				
				for (int i = 0; i < lineArray.length; i++) {
					Double tmp = Double.parseDouble(df.format(Double.parseDouble(lineArray[i])));
					
					if(distribMap.containsKey(tmp)){
						distribMap.put(tmp, distribMap.get(tmp)+1);
					}else{
						distribMap.put(tmp, 1);
					}
				}
			}
			
			contentReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		File outputX = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"tmpXaxis.csv");
		File outputY = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"tmpYaxis.csv");
		
		ExternalWriter xW = new ExternalWriter(outputX, false);
		ExternalWriter yW = new ExternalWriter(outputY, false);
		
		xW.openWriter();
		yW.openWriter();
		
		for(Entry<Double, Integer> entry : distribMap.entrySet()){
			xW.write(entry.getKey()+"\n");
			yW.write(entry.getValue()+"\n");
		}
		
		xW.closeWriter();
		yW.closeWriter();
		
		File script = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"distribution_plot.R");
		File outputFile = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"/results/plots/"+mapper.getPathName()+"_"+method.getPathName()+"_cor_distrib.png");
		
		RScriptCaller rcaller = new RScriptCaller(script, outputX, outputY, outputFile, "Distribution of correlation for"+mapper.getPathName()+"-"+method.getPathName());
		rcaller.run();
		
	}
	
	public class RScriptCaller implements Runnable {
		
		private File script, csvX, csvY, output;
		private String title;
		public static final String PATH_TO_Rscript = "/home/proj/biosoft/software/R/R-3.3.0/bin/Rscript";
		
		public RScriptCaller(File script, File csvX, File csvY, File output, String title) {
			this.script = script;
			this.csvX = csvX;
			this.csvY = csvY;
			this.output = output;
			this.title = title;
		}
		
		@Override
		public void run() {
			DebugMessageFactory.printInfoDebugMessage(true, "CREATE PLOT WITH R - "+script.getName());
			try {
				Process p = new ProcessBuilder(PATH_TO_Rscript, script.getAbsolutePath(), csvX.getAbsolutePath(), csvY.getAbsolutePath(), output.getAbsolutePath(), title).inheritIO().start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void createPlotsForEveryMapperMethodPair(){
		
		for(Mapper mapper : Mapper.values()){
			for(DEmethods method : DEmethods.values()){
				getDistributionOfPvalsOfTissuePairs(mapper, method);
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
		}
		
	}
	
	public void createAllDistributionsOfPvalsInTissuePair(){
		
		UtilityManager um = new UtilityManager("/home/proj/biocluster/praktikum/genprakt/bioinformaniacs/config.txt", false, false, false);
		
		for(Mapper mapper : Mapper.values()){
			for(DEmethods method : DEmethods.values()){
				
				getDistributionOfPvalsInTissuePair(mapper, method, false);
				
			}
		}
	}
	
	public void getDistributionOfPvalsInTissuePair(Mapper mapper, DEmethods method, boolean createUm){
		
		File resultFile = new File(HeatmapFactory.PATH_TO_RESULT+"significant/"+mapper.getPathName().toLowerCase()+"_"+method.getPathName().toLowerCase()+"/");
		resultFile.mkdirs();
		
		if(createUm){
			UtilityManager um = new UtilityManager("/home/proj/biocluster/praktikum/genprakt/bioinformaniacs/config.txt", false, false, false);
		}
		
		HeatmapFactory hmf = new HeatmapFactory();
		
		Pair<Vector<TissuePairCompare>, Vector<TreeSet<GeneObject>>> pair = hmf.prepareHeatmapForMapperPair(new MapperxMethodPair(mapper, method));
		
		Vector<TissuePairCompare> tissuePairs = pair.getFirst();
		Vector<TreeSet<GeneObject>> geneSets = pair.getSecond();
		
		DecimalFormat df = new DecimalFormat("#.##");
		df.setRoundingMode(RoundingMode.HALF_UP);
		
		for (int i = 0; i < tissuePairs.size(); i++) {
			
			TissuePairCompare currentTissuePair = tissuePairs.get(i);
			TreeSet<GeneObject> currentGeneSet = geneSets.get(i);
			
			String tissue_1_name = currentTissuePair.getKey().getName();
			String tissue_2_name = currentTissuePair.getValue().getName();
			String species_name = currentTissuePair.getSpecies().getName().replaceAll(" ", "-");
			
			String currentString = resultFile.getAbsolutePath()+"/"+tissue_1_name+"_"+tissue_2_name+"_"+species_name;
			
			ExternalWriter extPval = new ExternalWriter(new File(currentString+".top100pval"), false);
			ExternalWriter extFc = new ExternalWriter(new File(currentString+".top100fc"), false);
			extPval.openWriter();
			extFc.openWriter();
			
			String[] geneIDs = new String[currentGeneSet.size()];
			ComparablePairTMP[] pvalArray = new ComparablePairTMP[currentGeneSet.size()];
			ComparablePairTMP[] fcArray = new ComparablePairTMP[currentGeneSet.size()];
			
			TreeMap<Double, Integer> distribMapPval = new TreeMap<>();
			TreeMap<Double, Integer> distribMapFc = new TreeMap<>();
			
			int counter = 0;
			for(GeneObject go : currentGeneSet){
				
				double adj_pval = Double.parseDouble(df.format(go.getAdj_pval()));
				double log_fc = Double.parseDouble(df.format(go.getLog2fc()));
				
				pvalArray[counter] = new ComparablePairTMP(counter, go.getAdj_pval());
				fcArray[counter] = new ComparablePairTMP(counter, go.getLog2fc());
				geneIDs[counter] = go.getName();
				
				if(distribMapPval.containsKey(adj_pval)){
					distribMapPval.put(adj_pval, distribMapPval.get(adj_pval)+1);
				}else{
					distribMapPval.put(adj_pval, 1);
				}
				
				if(distribMapFc.containsKey(log_fc)){
					distribMapFc.put(log_fc, distribMapFc.get(log_fc)+1);
				}else{
					distribMapFc.put(log_fc, 1);
				}
				
				counter++;
			}
			
			File outputX = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"tmpXaxis_2.csv");
			File outputY = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"tmpYaxis_2.csv");
			
			ExternalWriter xW = new ExternalWriter(outputX, false);
			ExternalWriter yW = new ExternalWriter(outputY, false);
			
			xW.openWriter();
			yW.openWriter();
			
			for(Entry<Double, Integer> entry : distribMapPval.entrySet()){
				xW.write(entry.getKey()+"\n");
				yW.write(entry.getValue()+"\n");
			}
			
			xW.closeWriter();
			yW.closeWriter();
			
			File script = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"distribution_plot.R");
			File outputFile = new File(currentString+"_pval_distrib.png");
			
			String title = "Distribution of p-values in "+tissue_1_name+"-"+tissue_2_name+"-"+species_name+" from "+mapper.getPathName()+"-"+method.getPathName();
			
			RScriptCaller rcaller = new RScriptCaller(script, outputX, outputY, outputFile, title);
			rcaller.run();
			
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			Arrays.sort(pvalArray);
			Arrays.sort(fcArray);
			
			for (int j = 0; j < 100; j++) {
				if(j < pvalArray.length){
					extPval.write(geneIDs[pvalArray[j].index]+"\t"+pvalArray[j].value+"\n");
				}
				if(j < fcArray.length){
					extFc.write(geneIDs[fcArray[j].index]+"\t"+fcArray[j].value+"\n");
				}
			}
			
			extPval.closeWriter();
			extFc.closeWriter();
		}
		
	}
	
	public static void main(String[] args) {
		
		DistributionFactory dis = new DistributionFactory();
		
		Mapper mapper = Mapper.STAR.getMapperForString("tophat");
		DEmethods method = DEmethods.DESEQ.getMethodForString("deseq");
		
//		dis.getDistributionOfPvalsOfTissuePairs(mapper, method);
//		dis.createPlotsForEveryMapperMethodPair();
		dis.getDistributionOfPvalsInTissuePair(mapper, method, true);
//		dis.createAllDistributionsOfPvalsInTissuePair();
		
	}
	
}
