package bcm.ports;

import bcm.components.NodeComponent;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>RequestingInboundPort</code> acts as the inbound port that
 * allows the connection between the client component and the node component
 */
public class RequestingInboundPort extends AbstractInboundPort
        implements RequestingCI {
    /**
     * Constructor of the RequestingInboundPort
     * 
     * @param uri   the uri of the port
     * @param owner the owner component
     * @throws Exception
     */
    public RequestingInboundPort(
            String uri,
            ComponentI owner) throws Exception {
        super(uri, RequestingCI.class, owner);
        assert uri != null;
        assert owner instanceof NodeComponent;
    }

    /**
     * Constructor of the RequestingInboundPort
     * 
     * @param owner the owner component
     * @throws Exception
     */
    public RequestingInboundPort(ComponentI owner)
            throws Exception {
        super(RequestingCI.class, owner);
        assert owner instanceof NodeComponent;
    }

    /**
     * See
     * {@link fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI#execute(RequestI)}
     */
    @Override
    public QueryResultI execute(RequestI request) throws Exception {
        return this.owner.handleRequest((((NodeComponent) this.getOwner()).getSyncRequestPoolIndex()),
                new AbstractComponent.AbstractService<QueryResultI>() {
                    @Override
                    public QueryResultI call() throws Exception {
                        System.out.println("NodeComponentInboundPort.execute");
                        return ((NodeComponent) this.getServiceOwner()).execute(request);
                    }
                });
    }

    /**
     * See
     * {@link fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI#executeAsync(RequestI)}
     */
    @Override
    public void executeAsync(RequestI request) throws Exception {
        this.getOwner().runTask((((NodeComponent) this.getOwner()).getAsyncRequestPoolIndex()),
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
