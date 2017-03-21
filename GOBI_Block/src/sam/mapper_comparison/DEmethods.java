package sam.mapper_comparison;

public enum DEmethods {

	DESEQ, LIMMA, EDGER;
	
	public String getPathName(){
		switch (this) {
		case DESEQ:
			return "DESeq";
		case LIMMA:
			return "limma";
		case EDGER:
			return "edgeR";
		default:
			return null;
		}
	}
	
}
