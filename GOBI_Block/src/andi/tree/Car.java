package andi.tree;

public class Car implements Node_Data{
	private int preis;
	private String name;
	
	public Car(int preis, String name) {
		this.preis = preis;
		this.name = name;
	}
	

	@Override
	public String toString() {
		return name+" <"+preis+"€>";
	}

	@Override
	public double compute_distance(Node_Data nd) {
		
		return Math.abs(this.get_preis()-((Car)nd).get_preis());
	}
	
	public int get_preis() {
		return preis;
	}
	public String get_name() {
		return name;
	}
	@Override
	public String shared_info() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public int compareTo(Node_Data o) {
		if(o instanceof Car)
			return name.compareTo(((Car) o).get_name());
		return -1;
	}
	
	
}
