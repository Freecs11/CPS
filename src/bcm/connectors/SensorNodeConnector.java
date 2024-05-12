package bcm.connectors;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>SensorNodeConnector</code> acts as the connector that
 * allows the sensor node component to ask for connection and disconnection
 * of a neighbour node, execute a request, and execute a request asynchronously.
 * </p>
 */
public class SensorNodeConnector extends AbstractConnector
        implements SensorNodeP2PCI {

    /**
     * @see fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI#ask4Disconnection(fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI)
     */
    @Override
    public void ask4Disconnection(NodeInfoI neighbour) throws Exception {
        ((SensorNodeP2PCI) this.offering).ask4Disconnection(neighbour);
    }

    /**
     * @see fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI#ask4Connection(fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI)
     */
    @Override
    public void ask4Connection(NodeInfoI newNeighbour) throws Exception {
        ((SensorNodeP2PCI) this.offering).ask4Connection(newNeighbour);
    }

    /**
     * @see fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI#execute(fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI)
     */
    @Override
    public QueryResultI execute(RequestContinuationI request) throws Exception {
        return ((SensorNodeP2PCI) this.offering).execute(request);
    }

    /**
     * @see fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI#executeAsync(fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI)
     */
    @Override
    public void executeAsync(RequestContinuationI requestContinuation) throws Exception {
        ((SensorNodeP2PCI) this.offering).executeAsync(requestContinuation);
    }

}