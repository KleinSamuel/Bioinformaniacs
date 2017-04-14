package sam.mapper_comparison;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeMap;

import sam.utils.ExternalWriter;

public class MasterHeatmap implements Serializable{

	private static final long serialVersionUID = 9222654236534522021L;

	private ArrayList<String> tissuePairs;
	private MasterHeatmapCell[][] matrix;
	
	public MasterHeatmap(int size){
		this.setMatrix(new MasterHeatmapCell[size][size]);
		this.setTissuePairs(new ArrayList<String>());
	}
	
	public void addEntryToMatrix(int row, int col, TreeMap<String, Double> mapperWithScore){
		this.matrix[row][col] = new MasterHeatmapCell(mapperWithScore);
	}

	public MasterHeatmapCell[][] getMatrix() {
		return matrix;
	}

	public void setMatrix(MasterHeatmapCell[][] matrix) {
		this.matrix = matrix;
	}

	public ArrayList<String> getTissuePairs() {
		return tissuePairs;
	}

	public void setTissuePairs(ArrayList<String> tissuePairs) {
		this.tissuePairs = tissuePairs;
	}
	
	public void writeToFile(File outputContent){
		
		ExternalWriter extW = new ExternalWriter(outputContent, false);
		extW.openWriter();
		
		for (int i = 0; i < matrix.length; i++) {
			
			for (int j = 0; j < matrix[i].length; j++) {
				
				MasterHeatmapCell tmpCell = matrix[i][j];
				
				extW.write((j==0 ? "" : "\t")+tmpCell.getScore());
				
			}
			extW.write("\n");
		}
		
		extW.closeWriter();
		
	}
	
}
