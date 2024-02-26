package bcm.connector;

import java.util.Set;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI;

public class RegistryConnector extends AbstractConnector
        implements RegistrationCI {

    @Override
    public boolean registered(String nodeIdentifier) throws Exception {
        return ((RegistrationCI) this.offering).registered(nodeIdentifier);
    }

    @Override
    public Set<NodeInfoI> register(NodeInfoI nodeInfo) throws Exception {
        return ((RegistrationCI) this.offering).register(nodeInfo);
    }

    @Override
    public NodeInfoI findNewNeighbour(NodeInfoI nodeInfo, Direction d) throws Exception {
        return ((RegistrationCI) this.offering).findNewNeighbour(nodeInfo, d);
    }

    @Override
    public void unregister(String nodeIdentifier) throws Exception {
        ((RegistrationCI) this.offering).unregister(nodeIdentifier);

    }

}
