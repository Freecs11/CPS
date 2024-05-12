package bcm.ports;

import java.util.Set;

import bcm.components.RegistryComponent;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.GeographicalZoneI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI;

/**
 * <p>
 * <strong> Description </strong>
 * </p>
 * <p>
 * The class <code>LookupInboundPort</code> acts as the inbound port that
 * allows the connection between the client component and the registry component
 * </p>
 */
public class LookupInboundPort extends AbstractInboundPort
        implements LookupCI {
    /**
     * Constructor of the LookupInboundPort
     * 
     * @param uri   the uri of the port
     * @param owner the owner component
     * @throws Exception
     */
    public LookupInboundPort(
            String uri,
            ComponentI owner) throws Exception {
        super(uri, LookupCI.class, owner);
        assert uri != null;
    }

    /**
     * Constructor of the LookupInboundPort
     * 
     * @param owner
     * @throws Exception
     */
    public LookupInboundPort(ComponentI owner) throws Exception {
        super(LookupCI.class, owner);
    }

    /**
     * See
     * {@link fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI#findByIdentifier(String)}
     */
    @Override
    public ConnectionInfoI findByIdentifier(String sensorNodeId) throws Exception {
        return this.getOwner().handleRequest(((RegistryComponent) this.owner).getRegisteryPoolIndex(),
                new AbstractComponent.AbstractService<ConnectionInfoI>() {
                    @Override
                    public ConnectionInfoI call() throws Exception {
                        System.out.println("LookupInboundPort.findByIdentifier");
                        return ((RegistryComponent) this.getServiceOwner()).findByIdentifier(sensorNodeId);
                    }
                });
    }

    /**
     * See
     * {@link fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI#findByZone(GeographicalZoneI)}
     */
    @Override
    public Set<ConnectionInfoI> findByZone(GeographicalZoneI z) throws Exception {
        return this.owner.handleRequest(((RegistryComponent) this.owner).getRegisteryPoolIndex(),
                new AbstractComponent.AbstractService<Set<ConnectionInfoI>>() {
                    @Override
                    public Set<ConnectionInfoI> call() throws Exception {
                        System.out.println("LookupInboundPort.findByZone");
                        return ((RegistryComponent) this.getServiceOwner()).findByZone(z);
                    }
                });
    }

}
