package itfIMP;

import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;

public class PositionIMP implements PositionI {
    private static final long serialVersionUID = 1L;
    private double x;
    private double y;

    public PositionIMP(double x, double y) {
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
        PositionIMP p1 = (PositionIMP) p;
        double x1 = this.x;
        double y1 = this.y;
        double x2 = p1.getX();
        double y2 = p1.getY();
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    @Override
    public Direction directionFrom(PositionI p) {
        // Direction : { NE , NW , SE , SW }
        PositionIMP p1 = (PositionIMP) p;
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
        PositionIMP p1 = (PositionIMP) p;
        double x1 = this.x;
        double y1 = this.y;
        double x2 = p1.getX();
        double y2 = p1.getY();
        return y1 > y2 && (x2 == x1 || x1 > x2);
    }

    @Override
    public boolean southOf(PositionI p) {
        PositionIMP p1 = (PositionIMP) p;
        double x1 = this.x;
        double y1 = this.y;
        double x2 = p1.getX();
        double y2 = p1.getY();
        return y1 < y2 && (x2 == x1 || x1 > x2);
    }

    @Override
    public boolean eastOf(PositionI p) {
        PositionIMP p1 = (PositionIMP) p;
        double x1 = this.x;
        double y1 = this.y;
        double x2 = p1.getX();
        double y2 = p1.getY();
        return x1 > x2 && (y2 == y1 || y1 > y2);
    }

    @Override
    public boolean westOf(PositionI p) {
        PositionIMP p1 = (PositionIMP) p;
        double x1 = this.x;
        double y1 = this.y;
        double x2 = p1.getX();
        double y2 = p1.getY();
        return x1 < x2 && (y2 == y1 || y1 > y2);
    }

}
