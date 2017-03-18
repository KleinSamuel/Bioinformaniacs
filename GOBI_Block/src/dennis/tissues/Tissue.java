package dennis.tissues;

import java.util.LinkedList;

import dennis.utility_manager.Experiment;

public class Tissue implements Comparable<Tissue> {

	private String name;
	private int number;

	private LinkedList<Experiment> experiments;

	public Tissue(String name) {
		this.name = name;
		this.number = 0;
		this.experiments = new LinkedList<>();
	}

	public String getName() {
		return name;
	}

	public LinkedList<Experiment> getExperiments() {
		return experiments;
	}

	public int getNumber() {
		return number;
	}

	public void addExperiment(Experiment dir) {
		experiments.add(dir);
		number = experiments.size();
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public int compareTo(Tissue o) {
		return name.compareTo(o.getName());
	}

}
