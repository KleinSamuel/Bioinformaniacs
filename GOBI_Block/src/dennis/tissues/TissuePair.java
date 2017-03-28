package dennis.tissues;

public class TissuePair implements Comparable<TissuePair> {

	private final Tissue key;
	private final Tissue value;

	public TissuePair(Tissue key, Tissue value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * 
	 * @return tissue1
	 */
	public Tissue getKey() {
		return key;
	}

	/**
	 * 
	 * @return tissue2
	 */
	public Tissue getValue() {
		return value;
	}

	@Override
	public String toString() {
		return key + "_" + value;
	}

	@Override
	public int compareTo(TissuePair o) {
		int i = getKey().compareTo(o.getKey());
		if (i == 0) {
			i = getValue().compareTo(o.getValue());
		}
		return i;
	}

	@Override
	public boolean equals(Object arg0) {
		return this.compareTo((TissuePair) arg0) == 0;
	}

}