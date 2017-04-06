package dennis.util;

public class GenePair implements Comparable<GenePair> {

	private String geneKey, geneValue;

	public GenePair(String geneKey, String geneVal) {
		this.geneKey = geneKey;
		this.geneValue = geneVal;
	}

	public String getKey() {
		return geneKey;
	}

	public String getValue() {
		return geneValue;
	}

	/**
	 * like compareTo but ignores if keys and values are switched
	 * 
	 * @param gp
	 * @return
	 */
	public boolean isSame(GenePair gp) {
		return (this.compareTo(gp) == 0 || this.compareTo(new GenePair(gp.getValue(), gp.getKey())) == 0);
	}

	@Override
	public boolean equals(Object obj) {
		return isSame((GenePair) obj);
	}

	@Override
	public String toString() {
		return "gene_pair: " + geneKey + "->" + geneValue;
	}

	@Override
	public int compareTo(GenePair o) {
		int comp = geneKey.compareTo(o.getKey());
		if (comp == 0)
			return geneValue.compareTo(o.getValue());
		return comp;
	}

	@Override
	public int hashCode() {
		return (this.geneKey + "_" + this.geneValue).hashCode();
	}

}
