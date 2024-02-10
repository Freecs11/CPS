package bcm.connector;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;

public class nodeConnector extends AbstractConnector
        implements RequestingCI {

    @Override
    public QueryResultI execute(RequestI request) throws Exception {
        return ((RequestingCI) this.offering).execute(request);
    }

    @Override
    public void executeAsync(RequestI request) throws Exception {
        ((RequestingCI) this.offering).executeAsync(request);
    }

}
