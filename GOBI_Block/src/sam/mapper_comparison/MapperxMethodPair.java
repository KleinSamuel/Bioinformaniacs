package sam.mapper_comparison;

public class MapperxMethodPair {
	
	private Mapper selectedMapper;
	private DEmethods selectedMethod;
	
	public MapperxMethodPair(Mapper mapper, DEmethods method){
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
