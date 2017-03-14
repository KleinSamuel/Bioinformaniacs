package andi.tree;

import java.util.ArrayList;
import java.util.LinkedList;

public class Example {
public static void main(String[] args) {
	ArrayList<Node_Data> cars = new ArrayList<>();
	cars.add(new Car(40000, "VW GOLF R"));
	cars.add(new Car(80000, "Porsche"));
	cars.add(new Car(35000, "VW GOLF GTI"));
	cars.add(new Car(100000, "Corvette C7"));
	cars.add(new Car(1000000, "Bugatti"));
	cars.add(new Car(800000, "Lamborghini"));
	Tree car_t = new Tree(cars);
	System.out.println("Car-Tree\n"+car_t.toString()+"\n");
	
	
	LinkedList<Node_Data> cities = new LinkedList<>();
	cities.add(new City("München", 80, 100));
	cities.add(new City("Garching", 80, 95));
	cities.add(new City("Hamburg", 40, 20));
	cities.add(new City("Hanover", 50, 10));
	cities.add(new City("Berlin", 80, 40));
	cities.add(new City("Garching ad Alz", 90, 100));
	cities.add(new City("Ingolstadt", 80, 85));
	cities.add(new City("Rosenheim", 80, 120));
	Tree city_t = new Tree(cities);
	System.out.println("City_Tree\n"+city_t.toString());
	
}
}


