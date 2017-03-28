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
	private static final double DEFAULT_STEIGUNG = 3.0, DEFAULT_MINIMUMVARIANCE = 0.1;
	// minimum value of variance if pval is close to 0
	private double steigung, minimumVariance;

	private NormalDistribution norm;

	/**
	 * @param steigung
	 * @param minimumVariance
	 */
	public Fuzzy(double steigung, double minimumVariance) {
		this.steigung = steigung;
		this.minimumVariance = minimumVariance;
		initCategories();
	}

	/**
	 * creates new fuzzy with standard steigung and minimumVariance
	 */
	public Fuzzy() {
		this.steigung = DEFAULT_STEIGUNG;
		this.minimumVariance = DEFAULT_MINIMUMVARIANCE;
		initCategories();
	}

	/**
	 * 
	 * @param steigung
	 * @param minimumVariance
	 * @param categories
	 */
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
		norm = new NormalDistribution(fc, var);
		double[] fuzz = new double[categories.size()];
		for (int i = 0; i < categories.size(); i++) {
			fuzz[i] = norm.probability(categories.get(i).getKey(), categories.get(i).getValue());
		}
		return fuzz;
	}

	public void initCategories() {
		categories = new ArrayList<>(5);
		categories.add(new Pair<Double, Double>(-Double.MAX_VALUE, -1d));
		categories.add(new Pair<Double, Double>(-1d, -0.5));
		categories.add(new Pair<Double, Double>(-0.5, 0.5));
		categories.add(new Pair<Double, Double>(0.5, 1d));
		categories.add(new Pair<Double, Double>(1d, Double.MAX_VALUE));
	}

	public ArrayList<Pair<Double, Double>> getCategories() {
		return categories;
	}

}
