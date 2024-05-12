package bcm.connectors;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>RequestingConnector</code> acts as the connector that
 * allows the client component to execute a request on a node component.
 * </p>
 */
public class RequestingConnector extends AbstractConnector
        implements RequestingCI {
    private static final long serialVersionUID = 5097548577299456605L;

    /**
     * @see fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI#execute(fr.sorbonne_u.cps.sensor_network.interfaces.RequestI)
     */
    @Override
    public QueryResultI execute(RequestI request) throws Exception {
        return ((RequestingCI) this.offering).execute(request);
    }

    /**
     * @see fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI#executeAsync(fr.sorbonne_u.cps.sensor_network.interfaces.RequestI)
     */
    @Override
    public void executeAsync(RequestI request) throws Exception {
        ((RequestingCI) this.offering).executeAsync(request);
    }
}
