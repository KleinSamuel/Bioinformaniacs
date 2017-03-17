package andi.tree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;

import andi.tree.Tree.Cluster_method;

public class Example {
	public static void main(String[] args) {

		 LinkedList<Node_Data> cities = new LinkedList<>();
//		 cities.add(new City("München", 80, 100));
//		 cities.add(new City("Garching", 80, 95));
//		 cities.add(new City("Hamburg", 40, 20));
//		 cities.add(new City("Hanover", 50, 10));
//		 cities.add(new City("Berlin", 80, 40));
//		 cities.add(new City("GarchingAlz", 90, 100));
//		 cities.add(new City("Ingolstadt", 80, 85));
//		 cities.add(new City("Rosenheim", 80, 120));
//		 Tree city_t = new Tree(cities);
//		 System.out.println("City_Tree\n"+city_t.toString());
//		 System.out.println(city_t.to_newick());
		ArrayList<Node_Data> cars = new ArrayList<>();
//		cars.add(new Car(35, "VW GOLF R"));
//		cars.add(new Car(60, "Porsche"));
//		cars.add(new Car(50, "VW GOLF GTI"));
//		cars.add(new Car(70, "Corvette C7"));
//		cars.add(new Car(100, "Lamborghini"));
		int cap = 100;
		int start = 100;
		int step = 5;
		Tree car_t;
		while(start<=cap) {
			cars.clear();
//			long s = System.currentTimeMillis();
			generate_Cars(start, cars);
			car_t  = new Tree(cars);
//			try {
//				Runtime.getRuntime().exec("display "+Plot.get_plot(car_t, car_t.get_root()));
//			} catch (IOException e1) {
//				e1.printStackTrace();
//			}

//			long end = System.currentTimeMillis();
//			long dur = (int) ((end-s));
//			System.out.println("Execution for "+start+" Nodes took "+((int)(dur/60000))+":"+(((int)(dur/1000))%60<10 ? "0"+((int)(dur/1000))%60 : ((int)(dur/1000))%60)+":"+((dur%1000<100) ? (dur%1000<10 ? "00" : "0") : "")+dur%1000+"min");
			start+=step;
//			if(car_t.get_root().get_children().size()>2) {
//			System.out.println(car_t.toString());
//			System.out.println(car_t.to_newick());
//			System.out.println(car_t);
//			break;
//			
			}
//		}
	}

	public static String generate_String(int length) {
		Random r = new Random();
		String abc = "ABCDEFGHIJKLMNOPQRSTUVWKYZ";
		String out = "";
		for(int i = 0; i<length;i++)
			out+=r.nextBoolean() ? abc.charAt(r.nextInt(length-1)) : abc.toLowerCase().charAt(r.nextInt(length-1));
		return out;
	}
	
	public static int generate_Price(int upper) {
		Random r = new Random();
		return r.nextInt(upper);
	}
	
	public static void generate_Cars(int count, Collection<Node_Data> out) {
		for(int i = 0; i<count;i++)
			out.add(new Car(generate_Price(100), generate_String(10)));
	}
	
}
