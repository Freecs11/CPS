package itfIMP.requestsItfIMP;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;

public class ProcessingNodeImp implements ProcessingNodeI {

	private PositionI postion;
	private Set<NodeInfoI> neighbors;
	private String nodeId;
	private Map<String, SensorDataI> sensorDataMap;

	public ProcessingNodeImp(PositionI postion, Set<NodeInfoI> neighbors, String nodeId) {
		this.postion = postion;
		this.neighbors = neighbors;
		this.nodeId = nodeId;
		this.sensorDataMap = new HashMap<>();
	}

	public ProcessingNodeImp() {
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

	@Override
	public QueryResultI propagateRequest(String nodeIdentifier, RequestContinuationI requestContinuation)
			throws Exception {
		return null;
	}

	@Override
	public void propagateRequestAsync(String nodeIdentifier, RequestContinuationI requestContinuation)
			throws Exception {
		// TODO Auto-generated method stub

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
