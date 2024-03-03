package bcm.ports;

import java.util.Set;

import bcm.components.ClientComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.BCM4JavaEndPointDescriptorI;
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI;

public class NodeP2POutboundPort extends AbstractOutboundPort
        implements SensorNodeP2PCI {

    public NodeP2POutboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, SensorNodeP2PCI.class, owner);
        assert uri != null;
    }

    public NodeP2POutboundPort(ComponentI owner) throws Exception {
        super(SensorNodeP2PCI.class, owner);
        // assert owner instanceof RegistryComponent;
    }

    @Override
    public void ask4Disconnection(NodeInfoI neighbour) throws Exception {
        ((SensorNodeP2PCI) this.getConnector()).ask4Disconnection(neighbour);
    }

    @Override
    public void ask4Connection(NodeInfoI newNeighbour) throws Exception {
        ((SensorNodeP2PCI) this.getConnector()).ask4Connection(newNeighbour);
    }

    @Override
    public QueryResultI execute(RequestContinuationI request) throws Exception {
        return ((SensorNodeP2PCI) this.getConnector()).execute(request);
    }

    @Override
    public void executeAsync(RequestContinuationI requestContinuation) throws Exception {
        ((SensorNodeP2PCI) this.getConnector()).executeAsync(requestContinuation);
    }

}
