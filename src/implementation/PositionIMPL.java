package implementation;

import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;

public class PositionIMPL implements PositionI {
    private static final long serialVersionUID = 1L;
    private double x;
    private double y;

    public PositionIMPL(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    @Override
    public double distance(PositionI p) {
        PositionIMPL p1 = (PositionIMPL) p;
        double x1 = this.x;
        double y1 = this.y;
        double x2 = p1.getX();
        double y2 = p1.getY();
        double powerx = (x2 - x1) * (x2 - x1);
        double powery = (y2 - y1) * (y2 - y1);
        return Math.sqrt(powerx + powery);
    }

    @Override
    public Direction directionFrom(PositionI p) {
        // Direction : { NE , NW , SE , SW }
        PositionIMPL p1 = (PositionIMPL) p;
        double x1 = this.x;
        double y1 = this.y;
        double x2 = p1.getX();
        double y2 = p1.getY();
        if (x2 > x1 && y2 > y1) {
            return Direction.NE;
        } else if (x2 < x1 && y2 > y1) {
            return Direction.NW;
        } else if (x2 > x1 && y2 < y1) {
            return Direction.SE;
        } else {
            return Direction.SW;
        }
    }

    @Override
    public boolean northOf(PositionI p) {
        PositionIMPL p1 = (PositionIMPL) p;
        double x1 = this.x;
        double y1 = this.y;
        double x2 = p1.getX();
        double y2 = p1.getY();
        return y1 > y2 && (x1 >= x2);
    }

    @Override
    public boolean southOf(PositionI p) {
        PositionIMPL p1 = (PositionIMPL) p;
        double x1 = this.x;
        double y1 = this.y;
        double x2 = p1.getX();
        double y2 = p1.getY();
        return y1 < y2 && (x1 >= x2);
    }

    @Override
    public boolean eastOf(PositionI p) {
        PositionIMPL p1 = (PositionIMPL) p;
        double x1 = this.x;
        double y1 = this.y;
        double x2 = p1.getX();
        double y2 = p1.getY();
        return x1 > x2 && (y1 >= y2);
    }

    @Override
    public boolean westOf(PositionI p) {
        PositionIMPL p1 = (PositionIMPL) p;
        double x1 = this.x;
        double y1 = this.y;
        double x2 = p1.getX();
        double y2 = p1.getY();
        return x1 < x2 && (y1 >= y2);
    }

}
