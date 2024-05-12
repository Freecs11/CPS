package bcm.ports;

import bcm.components.NodeComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestResultCI;

/**
 * <p>
 * <strong> Description </strong>
 * </p>
 * <p>
 * The class <code>RequestResultOutboundPort</code> acts as the outbound port
 * that allows the connection between the node component and the client
 * component
 * for the request result sent from the node component to the client component.
 */
public class RequestResultOutboundPort extends AbstractOutboundPort implements RequestResultCI {
    /**
     * Constructor of the RequestResultOutboundPort
     * 
     * @param owner the owner component
     * @throws Exception
     */
    public RequestResultOutboundPort(ComponentI owner)
            throws Exception {
        super(RequestResultCI.class, owner);
    }

    /**
     * Constructor of the RequestResultOutboundPort
     * 
     * @param uri   the uri of the port
     * @param owner the owner component
     * @throws Exception
     */
    public RequestResultOutboundPort(String uri, ComponentI owner)
            throws Exception {
        super(uri, RequestResultCI.class, owner);
        assert owner instanceof NodeComponent;
    }

    /**
     * See
     * {@link RequestResultCI#acceptRequestResult(String, QueryResultI)}
     */
    @Override
    public void acceptRequestResult(String requestURI, QueryResultI result) throws Exception {
        ((RequestResultCI) this.getConnector()).acceptRequestResult(requestURI, result);

    }
}
