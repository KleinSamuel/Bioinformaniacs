package sam.mapper_comparison;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.math3.util.Pair;

import dennis.enrichment.EnrichmentAnalysisUtils;
import dennis.enrichment.GeneObject;
import dennis.tissues.Tissue;
import dennis.tissues.TissueHandler;
import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;

public class HeatmapFactory {

	private ArrayList<MapperxMethodPair> mapperDePairList;
	
	private final String PATH_TO_EB_FILES = "/home/proj/biocluster/praktikum/genprakt/bioinformaniacs/EB/";
	private final String PATH_TO_HEATMAP_OUTPUT = "/home/proj/biocluster/oraktikum/genprakt/bioinformaniacs/sam/";
	
	public HeatmapFactory(){
		createMapperPairs();
	}
	
	private void createMapperPairs(){
		mapperDePairList = new ArrayList<>();
		for(Mapper mapper : Mapper.values()){
			for(DEmethods method : DEmethods.values()){
				mapperDePairList.add(new MapperxMethodPair(mapper, method));
			}
		}
	}
	
	public void createHeatmapForEachMapperPair(){
		for(MapperxMethodPair pair : this.mapperDePairList){
			Pair<Vector<TissuePairCompare>, Vector<TreeSet<GeneObject>>> output = createHeatmapForMapperPair(pair);
			
			Heatmap heatmap = computeHeatmap(output, pair.getMapper().getPathName(), pair.getMethod().getPathName());
			storeHeatmapToDisk(PATH_TO_HEATMAP_OUTPUT, heatmap);
		}
	}
	
	public Pair<Vector<TissuePairCompare>,Vector<TreeSet<GeneObject>>> createHeatmapForMapperPair(MapperxMethodPair pair){
		
		Vector<TissuePairCompare> tissuePairs = new Vector<>();
		Vector<TreeSet<GeneObject>> geneObjects = new Vector<>();
		
		String mapperName = pair.getMapper().getPathName();
		String methodName = pair.getMethod().getPathName();
		
		ArrayList<String> species_ids = new ArrayList<String>(Arrays.asList(new File(PATH_TO_EB_FILES).list()));
		
		for(String species_id : species_ids){
			Species species = UtilityManager.getSpecies(Integer.parseInt(species_id));
			
			ArrayList<String> tissue_pairs = new ArrayList<String>(Arrays.asList(new File(PATH_TO_EB_FILES+species_id+"/").list()));
			
			for(String tissue_pair_string : tissue_pairs){
				
				String[] tissueStringArray = tissue_pair_string.split("_");
				String tissue_1_string = tissueStringArray[0];
				String tissue_2_string = tissueStringArray[1];
				Tissue tissue_1 = TissueHandler.getTissue(species, tissue_1_string);
				Tissue tissue_2 = TissueHandler.getTissue(species, tissue_2_string);
				
				TissuePairCompare tissuePair = new TissuePairCompare(tissue_1, tissue_2, species);
				
				ArrayList<String> mapper_list = new ArrayList<String>(Arrays.asList(new File(PATH_TO_EB_FILES+species_id+"/"+tissue_pair_string+"/").list()));
				
				/* check if mapper exists */
				if(mapper_list.contains(mapperName)){
					
					/* check if DEmethod exists */
					ArrayList<String> file_list = new ArrayList<String>(Arrays.asList(new File(PATH_TO_EB_FILES+species_id+"/"+tissue_pair_string+"/"+mapperName+"/").list()));
					
					boolean methodExists = false;
					for(String s : file_list){
						if(s.contains(methodName)){
							methodExists = true;
							break;
						}
					}
					if(!methodExists){
						break;
					}
					
					String fileEnding = pair.getMethod().getPathName();
					String pathToDEFile = PATH_TO_EB_FILES+species_id+"/"+tissue_pair_string+"/"+pair.getMapper().getPathName()+"/"+tissue_pair_string+"."+fileEnding;
					
					TreeSet<GeneObject> set = EnrichmentAnalysisUtils.readDEfile(pathToDEFile);
					
					tissuePairs.add(tissuePair);
					geneObjects.add(set);
					
				}
			}
		}
		
		return new Pair<>(tissuePairs, geneObjects);
	}
	
	public Heatmap computeHeatmap(Pair<Vector<TissuePairCompare>, Vector<TreeSet<GeneObject>>> pair, String mapper, String method){
		
		ArrayList<String> tissuePairs = new ArrayList<>();
		Double[][] scores = new Double[pair.getFirst().size()][pair.getFirst().size()];
		
		return new Heatmap(mapper, method, tissuePairs, scores);
	}
	
	public void storeHeatmapToDisk(String path, Object hm){
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
			oos.writeObject(hm);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public Heatmap readHeatmapFromDisk(String path){
		Heatmap output = null;
		
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
			output = (Heatmap)ois.readObject();
			ois.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return output;
	}
	
	public static void main(String[] args) {
		
		UtilityManager um = new UtilityManager("/home/proj/biocluster/praktikum/genprakt/bioinformaniacs/config.txt", false, false, false);
		
		HeatmapFactory hmf = new HeatmapFactory();
		Pair<Vector<TissuePairCompare>, Vector<TreeSet<GeneObject>>> pair = hmf.createHeatmapForMapperPair(new MapperxMethodPair(Mapper.CONTEXTMAP, DEmethods.LIMMA));
		
		
		
	}
	
}
