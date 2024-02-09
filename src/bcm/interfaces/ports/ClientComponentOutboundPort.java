package bcm.interfaces.ports;

import bcm.components.ClientComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;

public class ClientComponentOutboundPort extends AbstractOutboundPort
        implements RequestingCI {
    private static final long serialVersionUID = 1L;

    public ClientComponentOutboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, RequestingCI.class, owner);
        assert uri != null;
        assert owner instanceof ClientComponent;
    }

    public ClientComponentOutboundPort(ComponentI owner) throws Exception {
        super(RequestingCI.class, owner);
        assert owner instanceof ClientComponent;
    }

    @Override
    public QueryResultI execute(RequestI request) throws Exception {
        return ((RequestingCI) this.getConnector()).execute(request);
    }

    @Override
    public void executeAsync(RequestI request) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'executeAsync'");
    }

}
