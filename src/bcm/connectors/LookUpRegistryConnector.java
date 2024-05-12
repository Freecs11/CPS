package bcm.connectors;

import java.util.Set;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.GeographicalZoneI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>LookUpRegistryConnector</code> acts as the connector that
 * allows the client component to lookup the registry component to find the
 * connection information of a sensor node.
 * </p>
 */
public class LookUpRegistryConnector extends AbstractConnector
        implements LookupCI {

    @Override
    public ConnectionInfoI findByIdentifier(String sensorNodeId) throws Exception {
        return ((LookupCI) this.offering).findByIdentifier(sensorNodeId);
    }

    @Override
    public Set<ConnectionInfoI> findByZone(GeographicalZoneI z) throws Exception {
        return ((LookupCI) this.offering).findByZone(z);
    }

}
