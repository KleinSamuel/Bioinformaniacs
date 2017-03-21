package dennis.fuzzy;

public class FuzzyArray {

	// type log2fc_range array_index
	// ++ inf -> 1 0
	// + 1 -> 0.5 1
	// O 0.5 -> -0.5 2
	// - -0.5 -> -1 3
	// -- -1 -> -inf 4
	private double[] fuzzyValues = new double[5];

	public FuzzyArray() {

	}

	public FuzzyArray(double[] values) {
		fuzzyValues = values;
	}

	public double[] getArray() {
		return fuzzyValues;
	}
	
}
