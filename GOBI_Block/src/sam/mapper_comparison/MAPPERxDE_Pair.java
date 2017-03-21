package sam.mapper_comparison;

public class MAPPERxDE_Pair {
	
	private Mapper selectedMapper;
	private DEmethods selectedMethod;
	
	public MAPPERxDE_Pair(Mapper mapper, DEmethods method){
		this.selectedMapper = mapper;
		this.selectedMethod = method;
	}
	
	public Mapper getMapper(){
		return selectedMapper;
	}

	public DEmethods getMethod(){
		return selectedMethod;
	}
}
