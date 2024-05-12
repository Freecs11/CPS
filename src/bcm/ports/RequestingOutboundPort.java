package bcm.ports;

import bcm.components.ClientComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>RequestingOutboundPort</code> acts as the outbound port
 * that allows the connection between the client component and the node
 * component for the query execution on the node component.
 */
public class RequestingOutboundPort extends AbstractOutboundPort
        implements RequestingCI {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor of the RequestingOutboundPort
     * 
     * @param uri   the uri of the port
     * @param owner the owner component
     * @throws Exception
     */
    public RequestingOutboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, RequestingCI.class, owner);
        assert uri != null;
        assert owner instanceof ClientComponent;
    }

    /**
     * Constructor of the RequestingOutboundPort
     * 
     * @param owner the owner component
     * @throws Exception
     */
    public RequestingOutboundPort(ComponentI owner) throws Exception {
        super(RequestingCI.class, owner);
        assert owner instanceof ClientComponent;
    }

    /**
     * See
     * {@link fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI#execute(RequestI)}
     */
    @Override
    public QueryResultI execute(RequestI request) throws Exception {
        return ((RequestingCI) this.getConnector()).execute(request);
    }

    /**
     * See
     * {@link fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI#executeAsync(RequestI)}
     */
    @Override
    public void executeAsync(RequestI request) throws Exception {
        ((RequestingCI) this.getConnector()).executeAsync(request);
    }

}
