package andi.tree;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

public class Car implements Node_Data{
	private int preis;
	private String name;
	private static Vector<String> alle_bauteile = new Vector<>(Arrays.asList(new String[] {"Lenkrad","Dach","Sitze","Reifen","Licht","ABS", "ESP"}));
	private Vector<Boolean> bauteile;
	
	public Car(int preis, String name, Vector<String> baut) {
		this.preis = preis;
		this.name = name;
		this.set_Bauteile(baut);
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
	public String get_Name() {
		return name;
	}


	@Override
	public String data_title() {
		return "Price difference of Cars";
	}

	
	public String shared_type() {
		return "Bauteile";
	}

	@Override
	public Vector<?> get_shared(Vector<Boolean> shared) {
		Vector<String> out=new Vector<>();
		if(shared!=null&&shared.size()==alle_bauteile.size()) {
			Iterator<Boolean> i_shared = shared.iterator();
			Iterator<String> i_bauteil = alle_bauteile.iterator();
			while(i_shared.hasNext())
				if(i_shared.next())
					out.add(i_bauteil.next());
				else
					i_bauteil.next();
		}
		return out;
	}



	@Override
	public Vector<Boolean> get_share_vector() {
		return bauteile;
	}
	
	public void set_Bauteile(Vector<String> bauteile) {
		this.bauteile = new Vector<>();
		Iterator<String> i = alle_bauteile.iterator();
		while(i.hasNext())
				this.bauteile.add(bauteile.contains(i.next()));
	}


	@Override
	public String get_distance_measurement() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
