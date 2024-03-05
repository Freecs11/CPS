package bcm.ports;

import java.util.Set;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI;

public class NodeComponentOutboundPort extends AbstractOutboundPort
        implements RegistrationCI {

    public NodeComponentOutboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, RegistrationCI.class, owner);
        assert uri != null;

    }

    public NodeComponentOutboundPort(ComponentI owner) throws Exception {
        super(RegistrationCI.class, owner);
        // assert owner instanceof RegistryComponent;
    }

    @Override
    public boolean registered(String nodeIdentifier) throws Exception {
        return ((RegistrationCI) this.getConnector()).registered(nodeIdentifier);
    }

    @Override
    public Set<NodeInfoI> register(NodeInfoI nodeInfo) throws Exception {
        return ((RegistrationCI) this.getConnector()).register(nodeInfo);
    }

    @Override
    public NodeInfoI findNewNeighbour(NodeInfoI nodeInfo, Direction d) throws Exception {
        return ((RegistrationCI) this.getConnector()).findNewNeighbour(nodeInfo, d);
    }

    @Override
    public void unregister(String nodeIdentifier) throws Exception {
        ((RegistrationCI) this.getConnector()).unregister(nodeIdentifier);
    }

}
