package implementation.requestsIMPL;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;

public class ProcessingNodeIMPL implements ProcessingNodeI {

	private PositionI postion;
	private Set<NodeInfoI> neighbors;
	private String nodeId;
	private Map<String, SensorDataI> sensorDataMap;

	public ProcessingNodeIMPL(PositionI postion, Set<NodeInfoI> neighbors, String nodeId) {
		this.postion = postion;
		this.neighbors = neighbors;
		this.nodeId = nodeId;
		this.sensorDataMap = new HashMap<>();
	}

	public ProcessingNodeIMPL() {
		this.sensorDataMap = new HashMap<>();
	}

	@Override
	public String getNodeIdentifier() {
		return this.nodeId;
	}

	@Override
	public PositionI getPosition() {
		return this.postion;
	}

	@Override
	public Set<NodeInfoI> getNeighbours() {
		return this.neighbors;
	}

	@Override
	public SensorDataI getSensorData(String sensorIdentifier) {
		return this.sensorDataMap.get(sensorIdentifier);
	}

	public PositionI getPostion() {
		return postion;
	}

	public void setPostion(PositionI postion) {
		this.postion = postion;
	}

	public Set<NodeInfoI> getNeighbors() {
		return neighbors;
	}

	public void setNeighbors(Set<NodeInfoI> neighbors) {
		this.neighbors = neighbors;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public Map<String, SensorDataI> getSensorDataMap() {
		return sensorDataMap;
	}

	public void setSensorDataMap(Map<String, SensorDataI> sensorDataMap) {
		this.sensorDataMap = sensorDataMap;
	}
}
