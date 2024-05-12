package bcm.ports;

import bcm.components.NodeComponent;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>SensorNodeP2PInboundPort</code> acts as the inbound port that
 * allows the connection between the sensor node component and the client
 * component
 * for the peer-to-peer communication service
 */
public class SensorNodeP2PInboundPort extends AbstractInboundPort
        implements SensorNodeP2PCI {
    /**
     * Constructor of the SensorNodeP2PInboundPort
     * 
     * @param uri   the uri of the port
     * @param owner the owner component
     * @throws Exception
     */
    public SensorNodeP2PInboundPort(
            String uri,
            ComponentI owner) throws Exception {
        super(uri, SensorNodeP2PCI.class, owner);
        assert uri != null;
    }

    /**
     * Constructor of the SensorNodeP2PInboundPort
     * 
     * @param owner the owner component
     * @throws Exception
     */
    public SensorNodeP2PInboundPort(ComponentI owner)
            throws Exception {
        super(SensorNodeP2PCI.class, owner);
    }

    /**
     * See
     * {@link fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI#ask4Disconnection(NodeInfoI)}
     */
    @Override
    public void ask4Disconnection(NodeInfoI neighbour) throws Exception {
        this.owner.handleRequest(
                new AbstractComponent.AbstractService<Void>() {
                    @Override
                    public Void call() throws Exception {
                        System.out.println("NodeComponentInboundPort.execute");
                        ((NodeComponent) this.getServiceOwner()).ask4Disconnection(neighbour);
                        return null;
                    }
                });
    }

    /**
     * See
     * {@link fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI#ask4Connection(NodeInfoI)}
     */
    @Override
    public void ask4Connection(NodeInfoI newNeighbour) throws Exception {
        this.owner.handleRequest(
                new AbstractComponent.AbstractService<Void>() {
                    @Override
                    public Void call() throws Exception {
                        System.out.println("NodeComponentInboundPort.execute");
                        ((NodeComponent) this.getServiceOwner()).ask4Connection(newNeighbour);
                        this.getServiceOwner()
                                .logMessage(SensorNodeP2PInboundPort.this.uri + " was called to connect to "
                                        + newNeighbour.nodeIdentifier());
                        return null;
                    }
                });
    }

    /**
     * See
     * {@link fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI#execute(RequestContinuationI)}
     */
    @Override
    public QueryResultI execute(RequestContinuationI request) throws Exception {
        return this.owner.handleRequest((((NodeComponent) this.getOwner()).getSyncContPoolIndex()),
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
     * {@link fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI#executeAsync(RequestContinuationI)}
     */
    @Override
    public void executeAsync(RequestContinuationI requestContinuation) throws Exception {
        this.owner.runTask((((NodeComponent) this.getOwner()).getAsyncContPoolIndex()),
                new AbstractComponent.AbstractTask() {
                    @Override
                    public void run() {
                        try {
                            ((NodeComponent) this.getTaskOwner()).executeAsync(requestContinuation);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

}
