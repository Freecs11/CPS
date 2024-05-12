package utils;

import java.util.Objects;

/**
 * <p>
 * <strong> Description </strong>
 * </p>
 * <p>
 * The class <code>NodeComponentInfo</code> is a props class that contains the
 * information of a node component.
 * </p>
 * 
 */
public class NodeComponentInfo {
    private Double x, y, range;
    private String name;

    /**
     * Constructor of the NodeComponentInfo
     * 
     * @param name  the id of the node
     * @param x     the x coordinate of the node
     * @param y     the y coordinate of the node
     * @param range the range of the node
     */
    public NodeComponentInfo(String name, Double x, Double y, Double range) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.range = range;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        NodeComponentInfo that = (NodeComponentInfo) o;
        return (Objects.equals(x, that.x) && Objects.equals(y, that.y)) || Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, name); // Incorpore les coordonnées et le nom pour la cohérence
    }

    public Double getX() {
        return x;
    }

    public Double getY() {
        return y;
    }

    public Double getRange() {
        return range;
    }

    public String getName() {
        return name;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public void setRange(Double range) {
        this.range = range;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "\nNodeComponentInfo [name=" + name + ", x=" + x + ", y=" + y + ", range=" + range + "]";
    }
}
