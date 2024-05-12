package bcm.connectors;

import java.util.Set;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>RegistryConnector</code> acts as the connector that
 * allows the registry component to register, unregister, and find new
 * neighbours
 * of a sensor node.
 * </p>
 */
public class RegistryConnector extends AbstractConnector
        implements RegistrationCI {
    /**
     * @see fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI#registered(java.lang.String)
     */
    @Override
    public boolean registered(String nodeIdentifier) throws Exception {
        return ((RegistrationCI) this.offering).registered(nodeIdentifier);
    }

    /**
     * @see fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI#register(fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI)
     */
    @Override
    public Set<NodeInfoI> register(NodeInfoI nodeInfo) throws Exception {
        return ((RegistrationCI) this.offering).register(nodeInfo);
    }

    /**
     * @see fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI#findNewNeighbour(fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI,
     *      fr.sorbonne_u.cps.sensor_network.interfaces.Direction)
     */
    @Override
    public NodeInfoI findNewNeighbour(NodeInfoI nodeInfo, Direction d) throws Exception {
        return ((RegistrationCI) this.offering).findNewNeighbour(nodeInfo, d);
    }

    /**
     * @see fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI#unregister(java.lang.String)
     */
    @Override
    public void unregister(String nodeIdentifier) throws Exception {
        ((RegistrationCI) this.offering).unregister(nodeIdentifier);

    }

}
