package bcm.ports;

import java.util.Set;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.GeographicalZoneI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI;

/**
 * <p>
 * <strong> Description </strong>
 * </p>
 * <p>
 * The class <code>LookupOutboundPort</code> acts as the outbound port that
 * allows the connection between the client component and the registry component
 * </p>
 * 
 */
public class LookupOutboundPort extends AbstractOutboundPort
        implements LookupCI {
    private static final long serialVersionUID = -3133551134835072507L;

    /**
     * Constructor of the LookupOutboundPort
     * 
     * @param uri   the uri of the port
     * @param owner the owner component
     * @throws Exception
     */
    public LookupOutboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, LookupCI.class, owner);
        assert uri != null;
    }

    /**
     * Constructor of the LookupOutboundPort
     * 
     * @param owner the owner component
     * @throws Exception
     */
    public LookupOutboundPort(ComponentI owner) throws Exception {
        super(LookupCI.class, owner);
    }

    /**
     * See
     * {@link fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI#findByIdentifier(String)}
     */
    @Override
    public ConnectionInfoI findByIdentifier(String sensorNodeId) throws Exception {
        return ((LookupCI) this.getConnector()).findByIdentifier(sensorNodeId);
    }

    /**
     * See
     * {@link fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI#findByZone(GeographicalZoneI)}
     */
    @Override
    public Set<ConnectionInfoI> findByZone(GeographicalZoneI z) throws Exception {
        return ((LookupCI) this.getConnector()).findByZone(z);
    }

}
