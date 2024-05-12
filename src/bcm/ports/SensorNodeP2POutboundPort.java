package bcm.ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>SensorNodeP2POutboundPort</code> acts as the outbound port
 * that allows the connection between the sensor node component and the client
 * component for the peer-to-peer communication service
 * 
 */
public class SensorNodeP2POutboundPort extends AbstractOutboundPort
        implements SensorNodeP2PCI {
    private static final long serialVersionUID = -8646196640281533190L;

    /**
     * Constructor of the SensorNodeP2POutboundPort
     * 
     * @param uri   the uri of the port
     * @param owner the owner component
     * @throws Exception
     */
    public SensorNodeP2POutboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, SensorNodeP2PCI.class, owner);
        assert uri != null;
    }

    /**
     * Constructor of the SensorNodeP2POutboundPort
     * 
     * @param owner the owner component
     * @throws Exception
     */
    public SensorNodeP2POutboundPort(ComponentI owner) throws Exception {
        super(SensorNodeP2PCI.class, owner);
        // assert owner instanceof RegistryComponent;
    }

    /**
     * See
     * {@link fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI#ask4Disconnection(NodeInfoI)}
     */
    @Override
    public void ask4Disconnection(NodeInfoI neighbour) throws Exception {
        ((SensorNodeP2PCI) this.getConnector()).ask4Disconnection(neighbour);
    }

    /**
     * See
     * {@link fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI#ask4Connection(NodeInfoI)}
     */
    @Override
    public void ask4Connection(NodeInfoI newNeighbour) throws Exception {
        ((SensorNodeP2PCI) this.getConnector()).ask4Connection(newNeighbour);
    }

    /**
     * See
     * {@link fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI#execute(RequestContinuationI)}
     */
    @Override
    public QueryResultI execute(RequestContinuationI request) throws Exception {
        return ((SensorNodeP2PCI) this.getConnector()).execute(request);
    }

    /**
     * See
     * {@link fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI#executeAsync(RequestContinuationI)}
     */
    @Override
    public void executeAsync(RequestContinuationI requestContinuation) throws Exception {
        ((SensorNodeP2PCI) this.getConnector()).executeAsync(requestContinuation);
    }

}
