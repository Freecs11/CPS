package implementation;

import java.io.Serializable;
import java.time.Instant;

import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>SensorDataIMPL</code> acts as an implementation of the
 * <code>SensorDataI</code> interface
 * </p>
 */
public class SensorDataIMPL implements SensorDataI {

    private static final long serialVersionUID = 1L;

    private String nodeIdentifier;
    private String sensorIdentifier;
    private Serializable value;
    private Instant timestamp;
    private Class<? extends Serializable> type;

    /**
     * Constructor of the SensorDataIMPL
     * 
     * @param nodeIdentifier    the node identifier of the sensor
     * @param sensorIdentifier  the sensor identifier
     * @param value             the value of the sensor
     * @param timestamp         the time of value being stored
     * @param type              the type of the value
     */
    public SensorDataIMPL(String nodeIdentifier, String sensorIdentifier, Serializable value, Instant timestamp,
            Class<? extends Serializable> type) {
        super();
        this.nodeIdentifier = nodeIdentifier;
        this.sensorIdentifier = sensorIdentifier;
        this.value = value;
        this.timestamp = timestamp;
        this.type = type;
    }

    /**
     * Constructor of the SensorDataIMPL
     * 
     * @param nodeIdentifier    the node identifier of the sensor
     * @param sensorIdentifier  the sensor identifier
     * @param value             the value of the sensor
     */
    public SensorDataIMPL(String nodeIdentifier, String sensorIdentifier, Serializable value) {
        super();
        this.nodeIdentifier = nodeIdentifier;
        this.sensorIdentifier = sensorIdentifier;
        this.value = value;
        this.timestamp = Instant.now();
        this.type = value.getClass();
    }

    /**
     * Constructor of the SensorDataIMPL
     * 
     * @param sensorIdentifier  the sensor identifier
     * @param value             the value of the sensor
     * @param timestamp         the time of value being stored
     */
    public SensorDataIMPL(String nodeIdentifier, String sensorIdentifier, Serializable value, Instant timestamp) {
        super();
        this.nodeIdentifier = nodeIdentifier;
        this.sensorIdentifier = sensorIdentifier;
        this.value = value;
        this.timestamp = timestamp;
        this.type = value.getClass();
    }
    
    /**
     * Constructor of the SensorDataIMPL
     * 
     * @param sensorIdentifier  the sensor identifier
     * @param value             the value of the sensor
     */
    public SensorDataIMPL(String sensorIdentifier, Serializable value) {
        super();
        this.sensorIdentifier = sensorIdentifier;
        this.value = value;
        this.timestamp = Instant.now();
        this.type = value.getClass();
    }
    
    /**
     * see {@link fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI#getNodeIdentifier()}
     */
    @Override
    public String getNodeIdentifier() {
        return nodeIdentifier;
    }

    /**
     * see {@link fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI#getSensorIdentifier()}
     */
    @Override
    public String getSensorIdentifier() {
        return sensorIdentifier;
    }

    /**
     * see {@link fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI#getType()}
     */
    @Override
    public Class<? extends Serializable> getType() {
        return type;
    }

    /**
     * see {@link fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI#getValue()}
     */
    @Override
    public Serializable getValue() {
        return value;
    }

    /**
     * see {@link fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI#getTimestamp()}
     */
    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

   
    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    /**
     * Setter for the node identifier
     * 
     * @param nodeIdentifier the node identifier to set
     */
    public void setNodeIdentifier(String nodeIdentifier) {
        this.nodeIdentifier = nodeIdentifier;
    }

    /**
     * Setter for the sensor identifier
     * 
     * @param sensorIdentifier the sensor identifier to set
     */
    public void setSensorIdentifier(String sensorIdentifier) {
        this.sensorIdentifier = sensorIdentifier;
    }

    /**
     * Setter for the value
     * 
     * @param value the value to set
     */
    public void setValue(Serializable value) {
        this.value = value;
        this.setTimestamp(Instant.now());
    }

    /**
     * Setter for the timestamp
     * 
     * @param timestamp the timestamp to set 
     */
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Setter for the type of the value
     * 
     * @param type the type to set
     */
    public void setType(Class<? extends Serializable> type) {
        this.type = type;
    }

    /**
     * Description of the SensorDataIMPL
     */
    @Override
    public String toString() {
        return "**************************" + "\nSensorDataIMP [nodeIdentifier=" + nodeIdentifier
                + "\n sensorIdentifier="
                + sensorIdentifier
                + "\n value="
                + value + "]\n";
    }

}
