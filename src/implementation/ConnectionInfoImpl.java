package implementation;

import java.sql.Connection;

import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.EndPointDescriptorI;

public class ConnectionInfoImpl implements ConnectionInfoI {
    private EndPointDescriptorI endPointDesc;
    private String nodeIdentifier;

    public ConnectionInfoImpl(String nodeIdentifier, EndPointDescriptorI endPointDesc) {
        this.nodeIdentifier = nodeIdentifier;
        this.endPointDesc = endPointDesc;
    }

    @Override
    public String nodeIdentifier() {
        return this.nodeIdentifier;
    }

    @Override
    public EndPointDescriptorI endPointInfo() {
        return this.endPointDesc;
    }

}
