package sam.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import sam.mapper_comparison.DEmethods;
import sam.mapper_comparison.Heatmap;
import sam.mapper_comparison.HeatmapFactory;
import sam.mapper_comparison.Mapper;
import sam.mapper_comparison.MapperxMethodPair;

public class HeatmapFromFileReader {

	public void readHeatmapIntoCSV(File heatmap, File heatmapInfo, File csv){
		
		try {
			
			ExternalWriter extW = new ExternalWriter(csv);
			
			ArrayList<String> tissuePairs = new ArrayList<>();
			
			BufferedReader brInfo = new BufferedReader(new FileReader(heatmapInfo));
			
			String line = null;
			
			while((line = brInfo.readLine()) != null){
				tissuePairs.add(line);
				extW.write(",\""+line+"\"");
			}
			
			extW.write("\n");
			
			brInfo.close();
			
			BufferedReader br = new BufferedReader(new FileReader(heatmap));

			line = null;
			int counter = 0;
			
			while((line = br.readLine()) != null){
				
				String[] lineArray = line.split("\t");
				
				extW.write("\""+tissuePairs.get(counter)+"\"");
				
				for (int i = 0; i < lineArray.length; i++) {
					extW.write(","+lineArray[i]);
				}
				
				extW.write("\n");				
				counter++;
			}
			
			br.close();
			extW.closeWriter();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Heatmap readHeatmapIntoObject(File heatmap, File heatmapInfo){
		
		String mapper = heatmap.getName().split("_")[0];
		String method = heatmap.getName().split("_")[1];
		
		ArrayList<String> tissuePairs = new ArrayList<>();
		
		try {
			
			BufferedReader brInfo = new BufferedReader(new FileReader(heatmapInfo));
			String line = null;
			while((line = brInfo.readLine()) != null){
				tissuePairs.add(line);
			}
			brInfo.close();
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		Double[][] scores = new Double[tissuePairs.size()][tissuePairs.size()];
		
		try {
			
			BufferedReader brInfo = new BufferedReader(new FileReader(heatmap));
			String line = null;
			
			int row = 0;
			while((line = brInfo.readLine()) != null){
				
				String[] lineArray = line.split("\t");
				for (int i = 0; i < lineArray.length; i++) {
					scores[row][i] = Double.parseDouble(lineArray[i]);
				}
				row++;
			}
			brInfo.close();
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return new Heatmap(mapper, method, tissuePairs, scores);
	}
	
	
	public static void main(String[] args) {
		
		HeatmapFromFileReader hr = new HeatmapFromFileReader();
		
		MapperxMethodPair pair = new MapperxMethodPair(Mapper.CONTEXTMAP, DEmethods.DESEQ);
		
		File heatmap = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+pair.getMapper()+"_"+pair.getMethod()+"_heatmap.content");
		File heatmapInfo = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+pair.getMapper()+"_"+pair.getMethod()+"_heatmap.info");
		File csv = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+pair.getMapper()+"_"+pair.getMethod()+"_heatmap.csv");
		
		hr.readHeatmapIntoCSV(heatmap, heatmapInfo, csv);
		
	}
	
}