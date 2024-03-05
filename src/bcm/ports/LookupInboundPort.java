package bcm.ports;

import java.util.Set;

import bcm.components.RegistryComponent;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.BCM4JavaEndPointDescriptorI;
import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.GeographicalZoneI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI;

public class LookupInboundPort extends AbstractInboundPort
        implements LookupCI, BCM4JavaEndPointDescriptorI {

    public LookupInboundPort(
            String uri,
            ComponentI owner) throws Exception {
        super(uri, LookupCI.class, owner);
        assert uri != null;
    }

    public LookupInboundPort(ComponentI owner) throws Exception {
        super(LookupCI.class, owner);
    }

    @Override
    public String getInboundPortURI() {
        return this.uri;
    }

    @Override
    public boolean isOfferedInterface(Class<? extends OfferedCI> inter) {
        return inter.equals(LookupCI.class);
    }

    @Override
    public ConnectionInfoI findByIdentifier(String sensorNodeId) throws Exception {
        return this.owner.handleRequest(
                new AbstractComponent.AbstractService<ConnectionInfoI>() {
                    @Override
                    public ConnectionInfoI call() throws Exception {
                        System.out.println("LookupInboundPort.findByIdentifier");
                        return ((RegistryComponent) this.getServiceOwner()).findByIdentifier(sensorNodeId);
                    }
                });
    }

    @Override
    public Set<ConnectionInfoI> findByZone(GeographicalZoneI z) throws Exception {
        return this.owner.handleRequest(
                new AbstractComponent.AbstractService<Set<ConnectionInfoI>>() {
                    @Override
                    public Set<ConnectionInfoI> call() throws Exception {
                        System.out.println("LookupInboundPort.findByZone");
                        return ((RegistryComponent) this.getServiceOwner()).findByZone(z);
                    }
                });
    }

}
