package dennis.fuzzy;

import java.util.ArrayList;

import org.apache.commons.math3.distribution.NormalDistribution;

import javafx.util.Pair;

public class Fuzzy {

	// type log2fc_range array_index
	// ++ inf -> 1 0
	// + 1 -> 0.5 1
	// O 0.5 -> -0.5 2
	// - -0.5 -> -1 3
	// -- -1 -> -inf 4
	private ArrayList<Pair<Double, Double>> categories;

	// Steigung der Geraden aus der Varianz gezogen wird
	private double steigung = 3.0;
	// minimum value of variance if pval is close to 0
	private double minimumVariance = 0.1;

	public Fuzzy(double steigung, double minimumVariance) {
		this.steigung = steigung;
		this.minimumVariance = minimumVariance;
		categories = new ArrayList<>(5);
		categories.add(new Pair<Double, Double>(-Double.MAX_VALUE, -1d));
		categories.add(new Pair<Double, Double>(-1d, -0.5));
		categories.add(new Pair<Double, Double>(-0.5, 0.5));
		categories.add(new Pair<Double, Double>(0.5, 1d));
		categories.add(new Pair<Double, Double>(1d, Double.MAX_VALUE));
	}

	public Fuzzy(double steigung, double minimumVariance, ArrayList<Pair<Double, Double>> categories) {
		this.steigung = steigung;
		this.minimumVariance = minimumVariance;
		this.categories = categories;
	}

	public double drawVariance(double pvalue) {
		return steigung * pvalue + minimumVariance;
	}

	public double[] getFuzzyArray(double fc, double pval) {
		double var = drawVariance(pval);
		NormalDistribution norm = new NormalDistribution(fc, var);
		double[] fuzz = new double[categories.size()];
		for (int i = 0; i < categories.size(); i++) {
			fuzz[i] = norm.probability(categories.get(i).getKey(), categories.get(i).getValue());
		}
		return fuzz;
	}

}
