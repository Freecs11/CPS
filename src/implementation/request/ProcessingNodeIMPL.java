package implementation.request;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;
import implementation.SensorDataIMPL;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code> ProcessingNodeIMPL</code> acts as an implementation of the
 * <code>ProcessingNodeIMPL</code>  interface  
 * </p>
 * */
public class ProcessingNodeIMPL implements ProcessingNodeI {

	private PositionI postion;
	private Set<NodeInfoI> neighbors;
	private String nodeId;
	private Map<String, SensorDataI> sensorDataMap;

	/**
	 * constructor of ProcessingNodeIMPL
	 * 
	 * @param postion of node 
	 * @param neighbors set of neighbors
	 * @param nodeId the id of the node
	 */
	public ProcessingNodeIMPL(PositionI postion, Set<NodeInfoI> neighbors, String nodeId) {
		this.postion = postion;
		this.neighbors = neighbors;
		this.nodeId = nodeId;
		this.sensorDataMap = new HashMap<>();
	}

	/**
	 *  initialize the sensor data map
	 */
	public ProcessingNodeIMPL() {
		this.sensorDataMap = new HashMap<>();
	}

	/**
	 * See
	 * {@link fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI#getNodeIdentifier()}
	 */
	@Override
	public String getNodeIdentifier() {
		return this.nodeId;
	}

	/**
	 * See {@link fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI#getPosition()}
	 */
	@Override
	public PositionI getPosition() {
		return this.postion;
	}

	/**
	 * See
	 * {@link fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI#getNeighbours()}
	 */
	@Override
	public Set<NodeInfoI> getNeighbours() {
		return this.neighbors;
	}

	/**
	 * See
	 * {@link fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI#getSensorData(String)}
	 */
	@Override
	public SensorDataI getSensorData(String sensorIdentifier) {
		return this.sensorDataMap.get(sensorIdentifier);
	}

	/**
	 * Getter for the position of the node
	 */
	public PositionI getPostion() {
		return postion;
	}


	/**
	 * Setter for the position of the node
	 */
	public void setPostion(PositionI postion) {
		this.postion = postion;
	}

	/**
	 * Getter for the neighbors of the node
	 */
	public Set<NodeInfoI> getNeighbors() {
		return neighbors;
	}

	/**
	 * Setter for the neighbors of the node
	 */
	public void setNeighbors(Set<NodeInfoI> neighbors) {
		this.neighbors = neighbors;
	}

	/**
	 * Getter for the node id
	 */
	public String getNodeId() {
		return nodeId;
	}

	/**
	 * Setter for the node id
	 */
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	/**
	 * Getter for the sensor data map
	 */
	public Map<String, SensorDataI> getSensorDataMap() {
		return sensorDataMap;
	}

	/**
	 * Setter for the sensor data map
	 */
	public void setSensorDataMap(Map<String, SensorDataI> sensorDataMap) {
		this.sensorDataMap = sensorDataMap;
	}

	/**
	 * this Add sensor data to the sensor data map
	 * 
	 * @param  sensorIdentifier the sensor identifier to add to the sensor data map
	 * @param value the value of the sensor data to add
	 */
	public void addSensorData(String sensorIdentifier, Serializable value) {
		SensorDataI sensorData = new SensorDataIMPL(this.nodeId, sensorIdentifier, value);
		this.sensorDataMap.put(sensorIdentifier, sensorData);
	}

	/**
	 * this Add sensor data to the sensor data map with the timestamp
	 * 
	 * @param  sensorIdentifier the sensor identifier to add to the sensor data map
	 * @param value the value of the sensor data to add
	 * @param timestamp the timestamp of the sensor data to add
	 */
	public void addSensorData(String sensorIdentifier, Serializable value, Instant timestamp) {
		SensorDataI sensorData = new SensorDataIMPL(this.nodeId, sensorIdentifier, value, timestamp);
		this.sensorDataMap.put(sensorIdentifier, sensorData);
	}

	/**
	 * this initialize the sensor data map
	 * 
	 * @param  sensorDataMap the sensor data map to add to the sensor data map
	 */
	public void addAllSensorData(ArrayList<SensorDataI> sensorDataMap) {
		for (SensorDataI sensorData : sensorDataMap) {
			this.sensorDataMap.put(sensorData.getSensorIdentifier(), sensorData);
		}
	}
}
