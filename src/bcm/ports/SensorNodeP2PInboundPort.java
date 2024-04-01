package bcm.ports;

import bcm.components.NodeComponent;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI;

public class SensorNodeP2PInboundPort extends AbstractInboundPort
        implements SensorNodeP2PCI {

    public SensorNodeP2PInboundPort(
            String uri,
            ComponentI owner) throws Exception {
        super(uri, SensorNodeP2PCI.class, owner);
        assert uri != null;
    }

    public SensorNodeP2PInboundPort(ComponentI owner)
            throws Exception {
        super(SensorNodeP2PCI.class, owner);
    }

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

    @Override
    public QueryResultI execute(RequestContinuationI request) throws Exception {
        return this.owner.handleRequest(
                new AbstractComponent.AbstractService<QueryResultI>() {
                    @Override
                    public QueryResultI call() throws Exception {
                        System.out.println("NodeComponentInboundPort.execute");
                        return ((NodeComponent) this.getServiceOwner()).execute(request);
                    }
                });
    }

    @Override
    public void executeAsync(RequestContinuationI requestContinuation) throws Exception {
        this.owner.runTask(
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
