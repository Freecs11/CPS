package bcm.ports;

import java.util.Set;

import bcm.components.RegistryComponent;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>RegistrationInboundPort</code> acts as the inbound port that
 * allows the connection between the client component and the registry component
 * </p>
 * 
 */
public class RegistrationInboundPort extends AbstractInboundPort
        implements RegistrationCI {
    private static final long serialVersionUID = 2017915843726837790L;

    /**
     * Constructor of the RegistrationInboundPort
     * 
     * @param uri   the uri of the port
     * @param owner the owner component
     * @throws Exception
     */
    public RegistrationInboundPort(
            String uri,
            ComponentI owner) throws Exception {
        super(uri, RegistrationCI.class, owner);
        assert uri != null;

    }

    /**
     * Constructor of the RegistrationInboundPort
     * 
     * @param owner
     * @throws Exception
     */
    public RegistrationInboundPort(ComponentI owner) throws Exception {
        super(RegistrationCI.class, owner);
    }

    /**
     * See
     * {@link fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI#registered(String)}
     */
    @Override
    public boolean registered(String nodeIdentifier) throws Exception {
        return this.owner.handleRequest(
                ((RegistryComponent) this.owner).getRegisteryPoolIndex(),
                new AbstractComponent.AbstractService<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        System.out.println("RegistryInboundPort.findByIdentifier");
                        return ((RegistryComponent) this.getServiceOwner()).registered(nodeIdentifier);
                    }
                });

    }

    /**
     * See
     * {@link fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI#register(NodeInfoI)}
     */
    @Override
    public Set<NodeInfoI> register(NodeInfoI nodeInfo) throws Exception {
        return this.owner.handleRequest(
                ((RegistryComponent) this.owner).getRegisteryPoolIndex(),
                new AbstractComponent.AbstractService<Set<NodeInfoI>>() {
                    @Override
                    public Set<NodeInfoI> call() throws Exception {
                        System.out.println("RegistryInboundPort.findByIdentifier");
                        return ((RegistryComponent) this.getServiceOwner()).register(nodeInfo);
                    }
                });
    }

    /**
     * See
     * {@link fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI#findNewNeighbour(NodeInfoI, Direction)}
     */
    @Override
    public NodeInfoI findNewNeighbour(NodeInfoI nodeInfo, Direction d) throws Exception {
        return this.owner.handleRequest(((RegistryComponent) this.owner).getRegisteryPoolIndex(),
                new AbstractComponent.AbstractService<NodeInfoI>() {
                    @Override
                    public NodeInfoI call() throws Exception {
                        System.out.println("RegistryInboundPort.findNewNeighbour");
                        return ((RegistryComponent) this.getServiceOwner()).findNewNeighbour(nodeInfo, d);
                    }
                });
    }

    /**
     * See
     * {@link fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI#unregister(String)}
     */
    @Override
    public void unregister(String nodeIdentifier) throws Exception {
        this.owner.handleRequest(
                ((RegistryComponent) this.owner).getRegisteryPoolIndex(),
                new AbstractComponent.AbstractService<Void>() {
                    @Override
                    public Void call() throws Exception {
                        System.out.println("RegistryInboundPort.unregister");
                        ((RegistryComponent) this.getServiceOwner()).unregister(nodeIdentifier);
                        return null;
                    }
                });
    }

}
