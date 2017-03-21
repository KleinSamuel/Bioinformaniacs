package andi.tree;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

public class Car implements Node_Data{
	private int preis;
	private String name;
	private static Vector<String> bauteile;
	
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

	
	public String shared_type() {
		return "Bauteile";
	}

	@Override
	public Vector<?> get_shared(Vector<Boolean> shared) {
		Vector<String> out=new Vector<>();
		if(shared!=null&&shared.size()==bauteile.size()) {
			Iterator<Boolean> i_shared = shared.iterator();
			Iterator<String> i_bauteil = bauteile.iterator();
			while(i_shared.hasNext())
				if(i_shared.next())
					out.add(i_bauteil.next());
		}
		return out;
	}



	@Override
	public Vector<Boolean> get_share_vector() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
