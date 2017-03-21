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

import dennis.enrichment.GeneObject;
import dennis.tissues.Tissue;
import dennis.tissues.TissueHandler;
import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;

public class HeatmapFactory {

	private ArrayList<MAPPERxDE_Pair> mapperDePairList;
	
	private final String PATH_TO_EB_FILES = "/home/proj/biocluster/praktikum/genprakt/bioinformaniacs/EB/";
	
	public HeatmapFactory(){
		createMapperPairs();
	}
	
	private void createMapperPairs(){
		mapperDePairList = new ArrayList<>();
		for(Mapper mapper : Mapper.values()){
			for(DEmethods method : DEmethods.values()){
				mapperDePairList.add(new MAPPERxDE_Pair(mapper, method));
			}
		}
	}
	
	public void createHeatmapForMapperPair(MAPPERxDE_Pair pair){
		
		Vector<Pair<TissuePairCompare, TreeSet<GeneObject>>> axis = new Vector<>();
		
		ArrayList<String> species_ids = new ArrayList<String>(Arrays.asList(new File(PATH_TO_EB_FILES).list()));
		
		for(String species_id : species_ids){
			Species species = UtilityManager.getSpecies(species_id);
			
			ArrayList<String> tissue_pairs = new ArrayList<String>(Arrays.asList(new File(PATH_TO_EB_FILES+species_id+"/").list()));
			
			for(String tissue_pair_string : tissue_pairs){
				
				String[] tissueStringArray = tissue_pair_string.split("_");
				String tissue_1_string = tissueStringArray[0];
				String tissue_2_string = tissueStringArray[1];
				Tissue tissue_1 = TissueHandler.getTissue(species, tissue_1_string);
				Tissue tissue_2 = TissueHandler.getTissue(species, tissue_2_string);
				
				TissuePairCompare tissuePair = new TissuePairCompare(tissue_1, tissue_2, species);
				
				ArrayList<String> mapper_list = new ArrayList<String>(Arrays.asList(new File(PATH_TO_EB_FILES+species_id+"/"+tissue_pair_string+"/").list()));
				
				if(mapper_list.contains(pair.getMapper().getPathName())){
					
					String fileEnding = pair.getMethod().getPathName();
					String pathToEBFile = PATH_TO_EB_FILES+species_id+"/"+tissue_pair_string+"/"+tissue_pair_string+"."+fileEnding;
					
					
					
				}
				
			}
			
		}
		
	}
	
	public static void storeHeatmapToDisk(String path, HeatmapForMapperDePair hm){
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
			oos.writeObject(hm);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public HeatmapForMapperDePair readHeatmapFromDisk(String path){
		HeatmapForMapperDePair output = null;
		
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
			output = (HeatmapForMapperDePair)ois.readObject();
			ois.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return output;
	}
	
}
