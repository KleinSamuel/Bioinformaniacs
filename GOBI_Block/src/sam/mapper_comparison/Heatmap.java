package sam.mapper_comparison;

import java.io.Serializable;
import java.util.ArrayList;

public class Heatmap implements Serializable {

	private String mapper;
	private String method;
	private ArrayList<String> tissuePairs;
	private Double[][] scores;
	
	public Heatmap(String mapper, String method, ArrayList<String> tissuePairs, Double[][] scores){
		setMapper(mapper);
		setMethod(method);
		setTissuePairs(tissuePairs);
		setScores(scores);
	}

	public String getMapper() {
		return mapper;
	}

	public void setMapper(String mapper) {
		this.mapper = mapper;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public ArrayList<String> getTissuePairs() {
		return tissuePairs;
	}

	public void setTissuePairs(ArrayList<String> tissuePairs) {
		this.tissuePairs = tissuePairs;
	}

	public Double[][] getScores() {
		return scores;
	}

	public void setScores(Double[][] scores) {
		this.scores = scores;
	}
	
}
