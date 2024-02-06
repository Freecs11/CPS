package itfIMP.requestsItfIMP;

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
	private SensorDataI sensorData;
	private String nodeId;
	
	

	public ProcessingNodeImp(PositionI postion, Set<NodeInfoI> neighbors, SensorDataI sensorData, String nodeId) {
		this.postion = postion;
		this.neighbors = neighbors;
		this.sensorData = sensorData;
		this.nodeId = nodeId;
	}

	public ProcessingNodeImp() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String getNodeIdentifier() {
		// TODO Auto-generated method stub
		return this.nodeId;
	}

	@Override
	public PositionI getPosition() {
		// TODO Auto-generated method stub
		return this.postion;
	}
	

	@Override
	public Set<NodeInfoI> getNeighbours() {
		// TODO Auto-generated method stub
		return this.neighbors;
	}

	@Override
	public SensorDataI getSensorData(String sensorIdentifier) {
		// TODO Auto-generated method stub
		return this.sensorData;
	}

	@Override
	public QueryResultI propagateRequest(String nodeIdentifier, RequestContinuationI requestContinuation)
			throws Exception {
		// TODO Auto-generated method stub
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

	public SensorDataI getSensorData() {
		return sensorData;
	}

	public void setSensorData(SensorDataI sensorData) {
		this.sensorData = sensorData;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

}
