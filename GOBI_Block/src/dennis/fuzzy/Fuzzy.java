package dennis.fuzzy;

public class Fuzzy {

	// Steigung der Geraden aus der Varianz gezogen wird
	private double steigung = 3.0;
	// minimum value of variance if pval is close to 0
	private double minimumVariance = 0.1;

	public Fuzzy(double steigung, double minimumVariance) {
		this.steigung = steigung;
		this.minimumVariance = minimumVariance;
	}

	public double drawVariance(double pvalue) {
		return steigung * pvalue + minimumVariance;
	}

}
