package andi.tree;

public class City implements Node_Data {

	private String name;
	private int pos_x;
	private int pos_y;

	public City(String name, int pos_x, int pos_y) {
		this.name = name;
		this.pos_x = pos_x;
		this.pos_y = pos_y;
	}

	@Override
	public int compareTo(Node_Data o) {
		if (o instanceof City) {
			City other = (City) o;
			if (this.pos_y == other.pos_y)
				return this.pos_x - other.pos_x;
			return this.pos_y - other.pos_y;
		}
		return -1;
	}

	@Override
	public String to_String() {
		return name+"(X:"+pos_x+"|Y:"+pos_y+")";
	}

	@Override
	public double compute_distance(Node_Data nd) {
		if (nd instanceof City) {
			City other = (City) nd;
			return Math.sqrt(Math.abs(this.get_x() - other.get_x()) + Math.abs(this.get_y() - other.get_y()));
		}
		return -1;
	}

	public int get_x() {
		return pos_x;
	}

	public int get_y() {
		return pos_y;
	}

	@Override
	public String shared_info() {
		// TODO Auto-generated method stub
		return null;
	}

}
