package bcm.ports;

import bcm.components.NodeComponent;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;

public class RequestingInboundPort extends AbstractInboundPort
        implements RequestingCI {
    public RequestingInboundPort(
            String uri,
            ComponentI owner) throws Exception {
        super(uri, RequestingCI.class, owner);
        assert uri != null;
        assert owner instanceof NodeComponent;
    }

    public RequestingInboundPort(ComponentI owner)
            throws Exception {
        super(RequestingCI.class, owner);
        assert owner instanceof NodeComponent;
    }

    @Override
    public QueryResultI execute(RequestI request) throws Exception {
        return this.owner.handleRequest((((NodeComponent) this.getOwner()).getSyncPoolIndex()),
                new AbstractComponent.AbstractService<QueryResultI>() {
                    @Override
                    public QueryResultI call() throws Exception {
                        System.out.println("NodeComponentInboundPort.execute");
                        return ((NodeComponent) this.getServiceOwner()).execute(request);
                    }
                });
    }

    @Override
    public void executeAsync(RequestI request) throws Exception {
        this.getOwner().runTask((((NodeComponent) this.getOwner()).getAsyncPoolIndex()),
                new AbstractComponent.AbstractTask() {
                    @Override
                    public void run() {
                        try {
                            ((NodeComponent) this.getTaskOwner()).executeAsync(request);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

}
