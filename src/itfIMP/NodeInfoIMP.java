package itfIMP;

import fr.sorbonne_u.cps.sensor_network.interfaces.EndPointDescriptorI;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;

public class NodeInfoIMP implements NodeInfoI {

	private String nodeId;
	private EndPointDescriptorI endPointInfo;
	private PositionI nodePosition;
	private double nodeRange;
	private EndPointDescriptorI p2pEndPointInfo;

	public NodeInfoIMP(String nodeId, EndPointDescriptorI endPointInfo, PositionI nodePosition, double nodeRange,
			EndPointDescriptorI p2pEndPointInfo) {
		this.nodeId = nodeId;
		this.endPointInfo = endPointInfo;
		this.nodePosition = nodePosition;
		this.nodeRange = nodeRange;
		this.p2pEndPointInfo = p2pEndPointInfo;
	}

	@Override
	public String nodeIdentifier() {
		return this.nodeId;
	}

	@Override
	public EndPointDescriptorI endPointInfo() {
		return this.endPointInfo;
	}

	@Override
	public PositionI nodePosition() {
		return this.nodePosition;
	}

	@Override
	public double nodeRange() {
		return this.nodeRange;
	}

	@Override
	public EndPointDescriptorI p2pEndPointInfo() {
		return this.p2pEndPointInfo;
	}

}
