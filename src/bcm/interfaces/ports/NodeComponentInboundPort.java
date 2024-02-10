package bcm.interfaces.ports;

import bcm.components.NodeComponent;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.BCM4JavaEndPointDescriptorI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;

public class NodeComponentInboundPort extends AbstractInboundPort implements RequestingCI, BCM4JavaEndPointDescriptorI {
    private static final long serialVersionUID = 1L;

    public NodeComponentInboundPort(
            String uri,
            ComponentI owner) throws Exception {
        super(uri, RequestingCI.class, owner);
        assert uri != null;
        // assert owner instanceof NodeComponent;
    }

    public NodeComponentInboundPort(ComponentI owner)
            throws Exception {
        super(RequestingCI.class, owner);
        // assert owner instanceof NodeComponent;
    }

    @Override
    public QueryResultI execute(RequestI request) throws Exception {
        return this.owner.handleRequest(
                new AbstractComponent.AbstractService<QueryResultI>() {
                    @Override
                    public QueryResultI call() throws Exception {
                        System.out.println("NodeComponentInboundPort.execute");
                        return ((NodeComponent) this.getServiceOwner()).returnQueryResult(request);
                    }
                });
    }

    @Override
    public void executeAsync(RequestI request) throws Exception {
        throw new UnsupportedOperationException("Unimplemented method 'executeAsync'");
    }

    @Override
    public String getInboundPortURI() {
        return this.uri;
    }

    @Override
    public boolean isOfferedInterface(Class<? extends OfferedCI> inter) {
        return inter.equals(RequestingCI.class);
    }

}
