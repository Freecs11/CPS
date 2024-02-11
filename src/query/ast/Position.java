package query.ast;

public class Position {

	private double pos_x;
	private double pos_y;

	public Position(double x, double y) {
		this.pos_x = x;
		this.pos_y = y;
	}

	public double getPos_x() {
		return pos_x;
	}

	public void setPos_x(double pos_x) {
		this.pos_x = pos_x;
	}

	public double getPos_y() {
		return pos_y;
	}

	public void setPos_y(double pos_y) {
		this.pos_y = pos_y;
	}
}
