package bcm.connectors;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI;

public class SensorNodeConnector extends AbstractConnector
        implements SensorNodeP2PCI {

    private static final long serialVersionUID = 5097548577299456605L;

    @Override
    public void ask4Disconnection(NodeInfoI neighbour) throws Exception {
        ((SensorNodeP2PCI) this.offering).ask4Disconnection(neighbour);
    }

    @Override
    public void ask4Connection(NodeInfoI newNeighbour) throws Exception {
        ((SensorNodeP2PCI) this.offering).ask4Connection(newNeighbour);
    }

    @Override
    public QueryResultI execute(RequestContinuationI request) throws Exception {
        return ((SensorNodeP2PCI) this.offering).execute(request);
    }

    @Override
    public void executeAsync(RequestContinuationI requestContinuation) throws Exception {
        ((SensorNodeP2PCI) this.offering).executeAsync(requestContinuation);
    }

}