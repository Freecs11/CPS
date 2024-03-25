package bcm.connectors;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestResultCI;

public class ClientRequestResult extends AbstractConnector
        implements RequestResultCI {

    @Override
    public void acceptRequestResult(String requestURI, QueryResultI result) throws Exception {
        ((RequestResultCI) this.offering).acceptRequestResult(requestURI, result);
    }
}
