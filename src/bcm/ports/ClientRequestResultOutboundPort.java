package bcm.ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestResultCI;

public class ClientRequestResultOutboundPort extends AbstractOutboundPort implements RequestResultCI
         {
    

    public ClientRequestResultOutboundPort(ComponentI owner)
            throws Exception {
        super(RequestResultCI.class, owner);
    }

    
    @Override
    public void acceptRequestResult(String requestURI, QueryResultI result) throws Exception {
        ((RequestResultCI) this.getConnector()).acceptRequestResult(requestURI, result);
    }
}

