package itfIMP;

import java.io.Serializable;
import java.time.Instant;

import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;

public class SensorDataIMP implements SensorDataI {

    private static final long serialVersionUID = 1L;

    private String nodeIdentifier;
    private String sensorIdentifier;
    private Serializable value;
    private Instant timestamp;
    private Class<? extends Serializable> type;

    public SensorDataIMP(String nodeIdentifier, String sensorIdentifier, Serializable value, Instant timestamp) {
        super();
        this.nodeIdentifier = nodeIdentifier;
        this.sensorIdentifier = sensorIdentifier;
        this.value = value;
        this.timestamp = timestamp;
    }

    @Override
    public String getNodeIdentifier() {
        return nodeIdentifier;
    }

    @Override
    public String getSensorIdentifier() {
        return sensorIdentifier;
    }

    @Override
    public Class<? extends Serializable> getType() {
        return type;
    }

    @Override
    public Serializable getValue() {
        return value;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public void setNodeIdentifier(String nodeIdentifier) {
        this.nodeIdentifier = nodeIdentifier;
    }

    public void setSensorIdentifier(String sensorIdentifier) {
        this.sensorIdentifier = sensorIdentifier;
    }

    public void setValue(Serializable value) {
        this.value = value;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public void setType(Class<? extends Serializable> type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "SensorDataIMP [nodeIdentifier=" + nodeIdentifier + ", sensorIdentifier=" + sensorIdentifier + ", value="
                + value + ", timestamp=" + timestamp + ", type=" + type + "]";
    }

}
