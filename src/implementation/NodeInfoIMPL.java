package implementation;

import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.EndPointDescriptorI;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;

public class NodeInfoIMPL extends ConnectionInfoImpl
        implements NodeInfoI {
    private final PositionI position;
    private final EndPointDescriptorI p2pEndpoint;
    private final double range;

    public NodeInfoIMPL(String nodeIdentifier, PositionI position, EndPointDescriptorI endpoint,
            EndPointDescriptorI p2pEndpoint, double range) {
        super(nodeIdentifier, endpoint);
        this.position = position;
        this.p2pEndpoint = p2pEndpoint;
        this.range = range;
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
