package sam.mapper_comparison;

import java.io.Serializable;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.math3.util.Pair;

import dennis.enrichment.GeneObject;

public class HeatmapForMapperDePair implements Serializable{

	private static final long serialVersionUID = 2712608991444370317L;
	
	private Vector<Pair<TissuePairCompare, TreeSet<GeneObject>>> axis;
	private Double[][] heatmap;
	
	public HeatmapForMapperDePair(Vector<Pair<TissuePairCompare, TreeSet<GeneObject>>> axis){
		this.axis = axis;
		heatmap = new Double[axis.size()][axis.size()];
	}
	
}
