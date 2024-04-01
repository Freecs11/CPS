package bcm.connectors;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;

public class RequestingConnector extends AbstractConnector
        implements RequestingCI {
    private static final long serialVersionUID = 5097548577299456605L;

    @Override
    public QueryResultI execute(RequestI request) throws Exception {
        return ((RequestingCI) this.offering).execute(request);
    }

    @Override
    public void executeAsync(RequestI request) throws Exception {
        ((RequestingCI) this.offering).executeAsync(request);
    }
}
