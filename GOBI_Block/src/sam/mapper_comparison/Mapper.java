package sam.mapper_comparison;

public enum Mapper {

	STAR, CONTEXTMAP, TOPHAT, HISAT;
	
	public String getPathName(){
		switch (this) {
		case STAR:
			return "star";
		case CONTEXTMAP:
			return "contextmap";
		case TOPHAT:
			return "tophat2";
		case HISAT:
			return "hisat";
		default:
			return null;
		}
	}
	
}
