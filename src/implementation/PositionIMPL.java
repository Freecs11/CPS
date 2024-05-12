package implementation;

import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>PositionIMPL</code> acts as the implementation of the
 * <code>PositionI</code> interface and provides the position of the node in the
 * network.
 * </p>
 */
public class PositionIMPL implements PositionI {
    private static final long serialVersionUID = 1L;
    private double x;
    private double y;

    /**
     * Constructor of the PositionIMPL
     * 
     * @param x the x coordinate of the position
     * @param y the y coordinate of the position
     */
    public PositionIMPL(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Getter of the x coordinate of the position
     * 
     * @return the x coordinate of the position
     */
    public double getX() {
        return this.x;
    }

    /**
     * Getter of the y coordinate of the position
     * 
     * @return the y coordinate of the position
     */
    public double getY() {
        return this.y;
    }

    /**
     * see {@link fr.sorbonne_u.cps.sensor_network.interfaces.PositionI#distance(PositionI)}
     */
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

    /**
     * see {@link fr.sorbonne_u.cps.sensor_network.interfaces.PositionI#directionFrom(PositionI)}
     */
    @Override
    public Direction directionFrom(PositionI p) {
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
        } else if (x2 < x1 && y2 < y1) {
            return Direction.SW;
        } else {
            return null;
        }
    }

    /**
     * see {@link fr.sorbonne_u.cps.sensor_network.interfaces.PositionI#northOf(PositionI)}
     */
    @Override
    public boolean northOf(PositionI p) {
        PositionIMPL p1 = (PositionIMPL) p;
        double x1 = this.x;
        double y1 = this.y;
        double x2 = p1.getX();
        double y2 = p1.getY();
        return y1 > y2 && (x1 >= x2);
    }

    /**
     * see {@link fr.sorbonne_u.cps.sensor_network.interfaces.PositionI#southOf(PositionI)}
     */
    @Override
    public boolean southOf(PositionI p) {
        PositionIMPL p1 = (PositionIMPL) p;
        double x1 = this.x;
        double y1 = this.y;
        double x2 = p1.getX();
        double y2 = p1.getY();
        return y1 < y2 && (x1 >= x2);
    }

    /**
     * see {@link fr.sorbonne_u.cps.sensor_network.interfaces.PositionI#eastOf(PositionI)}
     */
    @Override
    public boolean eastOf(PositionI p) {
        PositionIMPL p1 = (PositionIMPL) p;
        double x1 = this.x;
        double y1 = this.y;
        double x2 = p1.getX();
        double y2 = p1.getY();
        return x1 > x2 && (y1 >= y2);
    }

    /**
     * see {@link fr.sorbonne_u.cps.sensor_network.interfaces.PositionI#westOf(PositionI)}
     */
    @Override
    public boolean westOf(PositionI p) {
        PositionIMPL p1 = (PositionIMPL) p;
        double x1 = this.x;
        double y1 = this.y;
        double x2 = p1.getX();
        double y2 = p1.getY();
        return x1 < x2 && (y1 >= y2);
    }

    @Override
    public String toString() {
        return "Coord {" + "x=" + x + ", y=" + y + '}';
    }
}
