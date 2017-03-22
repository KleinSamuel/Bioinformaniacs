package sam.mapper_comparison;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeMap;

public class MasterHeatmap implements Serializable{

	private static final long serialVersionUID = 9222654236534522021L;

	private ArrayList<String> tissuePairs;
	private MasterHeatmapCell[][] matrix;
	
	public MasterHeatmap(int size){
		this.setMatrix(new MasterHeatmapCell[size][size]);
		this.setTissuePairs(new ArrayList<String>());
	}
	
	public void addEntryToMatrix(int x, int y, TreeMap<String, Double> mapperWithScore){
		this.matrix[x][y] = new MasterHeatmapCell(mapperWithScore);
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
	
}
