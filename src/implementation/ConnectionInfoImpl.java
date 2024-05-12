package implementation;

import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.EndPointDescriptorI;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>ConnectionInfoImpl</code> acts as the implementation of the
 * <code>ConnectionInfo</code> interface. It is used to store the information about the node connection.
 * </p>
 */
public class ConnectionInfoImpl implements ConnectionInfoI {
    private EndPointDescriptorI endPointDesc;
    private String nodeIdentifier;

    /**
     * Constructor of the ConnectionInfoImpl
     * 
     * @param nodeIdentifier the node identifier
     * @param endPointDesc   the end point descriptor
     */
    public ConnectionInfoImpl(String nodeIdentifier, EndPointDescriptorI endPointDesc) {
        this.nodeIdentifier = nodeIdentifier;
        this.endPointDesc = endPointDesc;
    }

    /**
     * see {@link fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI#nodeIdentifier()}
     */
    @Override
    public String nodeIdentifier() {
        return this.nodeIdentifier;
    }

    /**
     * see {@link fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI#endPointInfo()}
     */
    @Override
    public EndPointDescriptorI endPointInfo() {
        return this.endPointDesc;
    }

    /**
     * Set the node identifier
     * 
     * @param endPointDesc the connection info
     */
    public void setEndPointInfo(EndPointDescriptorI endPointDesc) {
        this.endPointDesc = endPointDesc;
    }

}
