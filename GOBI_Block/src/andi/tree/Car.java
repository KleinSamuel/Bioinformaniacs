package andi.tree;

import java.util.Collection;

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
	public int compareTo(Node_Data o) {
		if(o instanceof Car)
			return name.compareTo(((Car) o).get_name());
		return -1;
	}


	@Override
	public String shared_info(Collection<Node_Data> nds) {
		int max =Integer.MIN_VALUE;
		int min = Integer.MAX_VALUE;
		for(Node_Data nd:nds) {
			Car c = (Car) nd;
			if(c.get_preis()>max)
				max=c.get_preis();
			if(c.get_preis()<min)
				min = c.get_preis();
		}
		return "Alle kosten "+min+"-"+max+"€";
	}


	@Override
	public String get_Name() {
		return name;
	}


	@Override
	public String data_title() {
		return "Price difference of Cars";
	}
	
	
}
