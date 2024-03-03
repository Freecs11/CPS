package bcm.ports;

import java.util.Set;

import bcm.components.ClientComponent;
import bcm.connectors.LookUpRegistryConnector;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.GeographicalZoneI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI;

public class Client2RegisterOutboundPort extends AbstractOutboundPort
        implements LookupCI {

    /**
     * 
     */
    private static final long serialVersionUID = -3133551134835072507L;

    public Client2RegisterOutboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, LookupCI.class, owner);
        assert uri != null;
        // assert owner instanceof ClientComponent;
    }

    public Client2RegisterOutboundPort(ComponentI owner) throws Exception {
        super(LookupCI.class, owner);
        // assert owner instanceof ClientComponent;
    }

    @Override
    public ConnectionInfoI findByIdentifier(String sensorNodeId) throws Exception {
        return ((LookupCI) this.getConnector()).findByIdentifier(sensorNodeId);
    }

    @Override
    public Set<ConnectionInfoI> findByZone(GeographicalZoneI z) throws Exception {
        return ((LookupCI) this.getConnector()).findByZone(z);
    }

}
