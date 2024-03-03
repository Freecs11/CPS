package implementation;

import fr.sorbonne_u.cps.sensor_network.interfaces.EndPointDescriptorI;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;

public class NodeInfoIMPL implements NodeInfoI {

    private final String nodeIdentifier;
    private final PositionI position;
    private final EndPointDescriptorI endpoint;
    private final EndPointDescriptorI p2pEndpoint;
    private final double range;

    public NodeInfoIMPL(String nodeIdentifier, PositionI position, EndPointDescriptorI endpoint,
            EndPointDescriptorI p2pEndpoint, double range) {
        this.nodeIdentifier = nodeIdentifier;
        this.position = position;
        this.endpoint = endpoint;
        this.p2pEndpoint = p2pEndpoint;
        this.range = range;
    }

    @Override
    public String nodeIdentifier() {
        return this.nodeIdentifier;
    }

    @Override
    public EndPointDescriptorI endPointInfo() {
        return this.endpoint;
    }

    @Override
    public PositionI nodePosition() {
        return this.position;
    }

    @Override
    public double nodeRange() {
        return this.range;
    }

    @Override
    public EndPointDescriptorI p2pEndPointInfo() {
        return this.p2pEndpoint;
    }

}
