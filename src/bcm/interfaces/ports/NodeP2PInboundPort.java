package bcm.interfaces.ports;

import bcm.components.NodeComponent;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.BCM4JavaEndPointDescriptorI;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;

public class NodeP2PInboundPort extends AbstractInboundPort implements RequestingCI,
        BCM4JavaEndPointDescriptorI, SensorNodeP2PCI {
    public NodeP2PInboundPort(
            String uri,
            ComponentI owner) throws Exception {
        super(uri, SensorNodeP2PCI.class, owner);
        assert uri != null;
        // assert owner instanceof NodeComponent;
    }

    public NodeP2PInboundPort(ComponentI owner)
            throws Exception {
        super(SensorNodeP2PCI.class, owner);
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
        this.owner.handleRequest(
                new AbstractComponent.AbstractService<>() {
                    @Override
                    public Object call() throws Exception {
                        System.out.println("NodeComponentInboundPort.execute");
                        ((NodeComponent) this.getServiceOwner()).executeAsync(request);
                        return null;
                    }
                });
    }

    @Override
    public String getInboundPortURI() {
        return this.uri;
    }

    @Override
    public boolean isOfferedInterface(Class<? extends OfferedCI> inter) {
        return inter.equals(RequestingCI.class);
    }

    @Override
    public void ask4Disconnection(NodeInfoI neighbour) throws Exception {
        this.owner.handleRequest(
                new AbstractComponent.AbstractService<>() {
                    @Override
                    public Object call() throws Exception {
                        System.out.println("NodeComponentInboundPort.execute");
                        ((NodeComponent) this.getServiceOwner()).ask4Disconnection(neighbour);
                        return null;
                    }
                });
    }

    @Override
    public void ask4Connection(NodeInfoI newNeighbour) throws Exception {
        this.owner.handleRequest(
                new AbstractComponent.AbstractService<>() {
                    @Override
                    public Object call() throws Exception {
                        System.out.println("NodeComponentInboundPort.execute");
                        ((NodeComponent) this.getServiceOwner()).ask4Connection(newNeighbour);
                        this.getServiceOwner().logMessage(NodeP2PInboundPort.this.uri + " was called to connect to "
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
        this.owner.handleRequest(
                new AbstractComponent.AbstractService<>() {
                    @Override
                    public Object call() throws Exception {
                        System.out.println("NodeComponentInboundPort.execute");
                        ((NodeComponent) this.getServiceOwner()).executeAsync(requestContinuation);
                        return null;
                    }
                });
    }

}
