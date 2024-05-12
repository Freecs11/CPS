package implementation;

import fr.sorbonne_u.cps.sensor_network.interfaces.EndPointDescriptorI;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>NodeInfoIMPL</code> acts as the implementation of the
 * <code>NodeInfoI</code> interface and extends the <code>ConnectionInfoImpl</code> class
 * to provide the information about the node.
 * </p>
 */
public class NodeInfoIMPL extends ConnectionInfoImpl
        implements NodeInfoI {
    private final PositionI position;
    private EndPointDescriptorI p2pEndpoint;
    private final double range;

    /**
     * Constructor of the NodeInfoIMPL
     * 
     * @param nodeIdentifier the node identifier in the network
     * @param position       the position of the node in the network
     * @param endpoint       the protocols to connect sensor node
     * @param p2pEndpoint    the  protocols to connect sensor node
     * @param range          the limite access node neighbours nodes in the network
     */
    public NodeInfoIMPL(String nodeIdentifier, PositionI position, EndPointDescriptorI endpoint,
            EndPointDescriptorI p2pEndpoint, double range) {
        super(nodeIdentifier, endpoint);
        this.position = position;
        this.p2pEndpoint = p2pEndpoint;
        this.range = range;
    }

    /**
     * see {@link fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI#nodePosition()}
     */
    @Override
    public PositionI nodePosition() {
        return this.position;
    }

    /**
     * see {@link fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI#nodeRange()}
     */
    @Override
    public double nodeRange() {
        return this.range;
    }

    /**
     * see {@link fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI#p2pEndPointInfo()}
     */
    @Override
    public EndPointDescriptorI p2pEndPointInfo() {
        return this.p2pEndpoint;
    }

    /**
     * Set the p2pEndPointInfo
     * 
     * @param p2pEndpoint the p2pEndPointInfo to set
     */
    public void setP2pEndPointInfo(EndPointDescriptorI p2pEndpoint) {
        this.p2pEndpoint = p2pEndpoint;
    }
}
