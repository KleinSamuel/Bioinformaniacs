package sam.mapper_comparison;

import java.io.Serializable;
import java.util.Collection;
import java.util.TreeMap;

public class MasterHeatmapCell implements Serializable{

	private static final long serialVersionUID = 6598042961968445635L;
	
	private Double score;
	private TreeMap<String, Double> mapperPairsWithScore;
	
	public MasterHeatmapCell(TreeMap<String, Double> map){
		this.mapperPairsWithScore = map;
		this.score = computeScore(map.values());		
	}
	
	public Double computeScore(Collection<Double> scores){
		Double out = -1d;
		
		if(scores.size() == 1){
			for(Double d : scores){
				return d;
			}
		}else{
			Double min = Double.MAX_VALUE;
			Double max = Double.MIN_VALUE;
			
			for(Double d : scores){
				min = Math.min(min, d);
				max = Math.max(max, d);
			}
			
			return max-min;
		}
		return out;
	}
	
	public Double getScore() {
		return score;
	}
	public void setScore(Double score) {
		this.score = score;
	}
	public TreeMap<String, Double> getMapperPairsWithScore() {
		return mapperPairsWithScore;
	}
	public void setMapperPairsWithScore(TreeMap<String, Double> mapperPairsWithScore) {
		this.mapperPairsWithScore = mapperPairsWithScore;
	}

}
