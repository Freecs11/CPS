package utils;

public class NodeComponentInfo {
    private Double x, y, range;
    private String name;

    public NodeComponentInfo (String name, Double x, Double y, Double range) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.range = range;
    }


    // redefinition du equals
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof NodeComponentInfo)) {
            return false;
        }
        NodeComponentInfo n = (NodeComponentInfo) o;
        return n.getX() == x && n.getY() == y;
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

    public String toString() {
        return "\nNodeComponentInfo [name=" + name + ", x=" + x + ", y=" + y + ", range=" + range + "]";
    }
}
