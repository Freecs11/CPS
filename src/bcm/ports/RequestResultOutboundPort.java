package bcm.ports;

import bcm.components.NodeComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestResultCI;

public class RequestResultOutboundPort extends AbstractOutboundPort implements RequestResultCI {

    public RequestResultOutboundPort(ComponentI owner)
            throws Exception {
        super(RequestResultCI.class, owner);
    }

    public RequestResultOutboundPort(String uri, ComponentI owner)
            throws Exception {
        super(uri, RequestResultCI.class, owner);
        assert owner instanceof NodeComponent;
    }

    @Override
    public void acceptRequestResult(String requestURI, QueryResultI result) throws Exception {
        ((RequestResultCI) this.getConnector()).acceptRequestResult(requestURI, result);

    }
}
