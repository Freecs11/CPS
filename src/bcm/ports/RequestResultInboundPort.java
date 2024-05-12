package bcm.ports;

import bcm.components.ClientComponent;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestResultCI;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>RequestResultInboundPort</code> acts as the inbound port that
 * allows the connection between the client component and the node component
 * for the asynchronous request result service
 * </p>
 */
public class RequestResultInboundPort extends AbstractInboundPort implements RequestResultCI {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor of the RequestResultInboundPort
     * 
     * @param uri   the uri of the port
     * @param owner the owner component
     * @throws Exception
     */
    public RequestResultInboundPort(String uri, ComponentI owner)
            throws Exception {
        super(uri, RequestResultCI.class, owner);
    }

    /**
     * Constructor of the RequestResultInboundPort
     * 
     * @param owner the owner component
     * @throws Exception
     */
    public RequestResultInboundPort(ComponentI owner)
            throws Exception {
        super(RequestResultCI.class, owner);
    }

    /**
     * See
     * {@link fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestResultCI#acceptRequestResult(String, QueryResultI)}
     */
    @Override
    public void acceptRequestResult(String requestURI, QueryResultI result) throws Exception {
        this.getOwner().runTask(
                new AbstractComponent.AbstractTask() {
                    @Override
                    public void run() {
                        try {
                            ((ClientComponent) this.getTaskOwner()).acceptRequestResult(requestURI, result);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

}
