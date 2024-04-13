package bcm.ports;

import bcm.components.ClientComponent;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestResultCI;

public class RequestResultInboundPort extends AbstractInboundPort implements RequestResultCI {
    private static final long serialVersionUID = 1L;

    public RequestResultInboundPort(String uri, ComponentI owner)
            throws Exception {
        super(uri, RequestResultCI.class, owner);
    }

    public RequestResultInboundPort(ComponentI owner)
            throws Exception {
        super(RequestResultCI.class, owner);
    }

    @Override
    public void acceptRequestResult(String requestURI, QueryResultI result) throws Exception {
        this.getOwner().handleRequest(owner -> ((ClientComponent) owner)).acceptRequestResult(requestURI, result);
    }

}
