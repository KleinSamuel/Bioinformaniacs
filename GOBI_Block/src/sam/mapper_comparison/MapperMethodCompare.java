package sam.mapper_comparison;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import sam.utils.ExternalWriter;

public class MapperMethodCompare {

	public void readCorFile(File f, File output){
		
		ArrayList<String> axis = new ArrayList<String>();
		ArrayList<String> values = new ArrayList<String>();
		
		try {
			
			BufferedReader br = new BufferedReader(new FileReader(f));
			
			String line = null;
			String tmp = "";
			String tmp2 = "";
			
			while((line = br.readLine()) != null){
				
				String firstPair = line.split("\t")[0].split("-")[0];
				
				if(!tmp.equals(firstPair)){
					
					if(tmp.equals("")){
						tmp2 = "";
					}
					values.add(tmp2);
					tmp = firstPair;
					axis.add(firstPair);
					tmp2 = "\""+firstPair+"\"";
				}
				
				tmp2 += ","+line.split("\t")[1];
				
			}
			
			values.add(tmp2);
			
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ExternalWriter extW = new ExternalWriter(output, false);
		extW.openWriter();
		
		for (int i = 0; i < axis.size(); i++) {
			extW.write(","+"\""+axis.get(i)+"\"");
		}
		for(String s : values){
			extW.write(s+"\n");
		}
		
		extW.closeWriter();
		
	}
	
	public static void main(String[] args) {
		
		MapperMethodCompare  pr = new MapperMethodCompare();
		
		File input = new File("/mnt/raidbiocluster/praktikum/genprakt-ws16/bioinformaniacs/sam/correlationsMM/correlation.content");
		File output = new File("/mnt/raidbiocluster/praktikum/genprakt-ws16/bioinformaniacs/sam/correlationsMM/correlation.csv");
		
		pr.readCorFile(input, output);
		
		HeatmapVisualizer hmv = new HeatmapVisualizer();
		
		File script = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"heatmap_example_cor.R");
		
		String outputDir = input.getAbsolutePath().substring(0, input.getAbsolutePath().lastIndexOf("/"))+"/correlation_method_2.png";
		
		hmv.createHeatmapWithR(script, output, outputDir);
		
	}
	
}
