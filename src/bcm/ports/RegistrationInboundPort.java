package bcm.ports;

import java.util.Set;

import bcm.components.RegistryComponent;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.BCM4JavaEndPointDescriptorI;
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI;

public class RegistrationInboundPort extends AbstractInboundPort
        implements RegistrationCI {
    /**
     * 
     */
    private static final long serialVersionUID = 2017915843726837790L;

    public RegistrationInboundPort(
            String uri,
            ComponentI owner) throws Exception {
        super(uri, RegistrationCI.class, owner);
        assert uri != null;

    }

    public RegistrationInboundPort(ComponentI owner) throws Exception {
        super(RegistrationCI.class, owner);
    }

    @Override
    public boolean registered(String nodeIdentifier) throws Exception {
        return this.owner.handleRequest(
                new AbstractComponent.AbstractService<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        System.out.println("RegistryInboundPort.findByIdentifier");
                        return ((RegistryComponent) this.getServiceOwner()).registered(nodeIdentifier);
                    }
                });

    }

    @Override
    public Set<NodeInfoI> register(NodeInfoI nodeInfo) throws Exception {
        return this.owner.handleRequest(
                new AbstractComponent.AbstractService<Set<NodeInfoI>>() {
                    @Override
                    public Set<NodeInfoI> call() throws Exception {
                        System.out.println("RegistryInboundPort.findByIdentifier");
                        return ((RegistryComponent) this.getServiceOwner()).register(nodeInfo);
                    }
                });
    }

    @Override
    public NodeInfoI findNewNeighbour(NodeInfoI nodeInfo, Direction d) throws Exception {
        return this.owner.handleRequest(
                new AbstractComponent.AbstractService<NodeInfoI>() {
                    @Override
                    public NodeInfoI call() throws Exception {
                        System.out.println("RegistryInboundPort.findNewNeighbour");
                        return ((RegistryComponent) this.getServiceOwner()).findNewNeighbour(nodeInfo, d);
                    }
                });
    }

    @Override
    public void unregister(String nodeIdentifier) throws Exception {
        this.owner.handleRequest(
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
