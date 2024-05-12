package bcm.connectors;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestResultCI;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>ClientRequestResult</code> acts as the connector that
 * allows the client component to accept the request result sent from a node
 * component.
 * </p>
 */
public class ClientRequestResult extends AbstractConnector
        implements RequestResultCI {

    /**
     * @see
     *      fr.sorbonne_u.cps.sensor_network.interfaces.RequestResultCI#acceptRequestResult(java.lang.String)
     */
    @Override
    public void acceptRequestResult(String requestURI, QueryResultI result) throws Exception {
        ((RequestResultCI) this.offering).acceptRequestResult(requestURI, result);

    }
}
