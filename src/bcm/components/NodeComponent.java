package bcm.components;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import bcm.connector.NodeConnector;
import bcm.connector.RegistryConnector;
import bcm.interfaces.ports.NodeComponentInboundPort;
import bcm.interfaces.ports.NodeComponentOutboundPort;
import bcm.interfaces.ports.NodeP2PInboundPort;
import bcm.interfaces.ports.NodeP2POutboundPort;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.BCM4JavaEndPointDescriptorI;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI;
import implementation.EndPointDescIMP;
import implementation.NodeInfoIMPL;
import implementation.PositionIMPL;
import implementation.RequestContinuationIMPL;
import implementation.SensorNodeIMPL;

@OfferedInterfaces(offered = { RequestingCI.class, SensorNodeP2PCI.class })
@RequiredInterfaces(required = { RegistrationCI.class })
public class NodeComponent extends AbstractComponent {
    protected Set<NodeInfoI> neighbours;
    protected final SensorNodeIMPL sensorNode;
    protected final NodeInfoIMPL nodeInfo;
    protected final NodeComponentInboundPort inboundPort;
    protected final NodeP2PInboundPort p2pInboundPort;
    protected final HashMap<NodeInfoI, NodeP2POutboundPort> p2poutboundPorts;
    protected final String p2pInboundPortURI;
    protected final NodeComponentOutboundPort outboundPort;
    protected final String registerInboundPortURI;

    protected NodeComponent(String uri,
            String sensorNodeInboundPortURI,
            String node_to_reg_OutboundPortURI,
            String nodeId,
            Double x, Double y,
            Double range,
            String registerInboundPortURI) throws Exception {
        // only one thread to ensure the serialised execution of services
        // inside the component.
        super(uri, 1, 0);
        assert sensorNodeInboundPortURI != null;
        assert node_to_reg_OutboundPortURI != null;
        this.p2pInboundPortURI = AbstractInboundPort.generatePortURI();
        this.neighbours = new HashSet<>();
        this.inboundPort = new NodeComponentInboundPort(sensorNodeInboundPortURI,
                this);
        this.p2poutboundPorts = new HashMap<>();
        this.p2pInboundPort = new NodeP2PInboundPort(AbstractInboundPort.generatePortURI(), this);
        this.p2pInboundPort.publishPort();
        this.outboundPort = new NodeComponentOutboundPort(node_to_reg_OutboundPortURI, this);
        this.inboundPort.publishPort();
        this.outboundPort.publishPort();

        this.registerInboundPortURI = registerInboundPortURI;

        EndPointDescIMP thisP2P = new EndPointDescIMP(this.inboundPort.getPortURI());
        this.nodeInfo = new NodeInfoIMPL(nodeId,
                new PositionIMPL(x, y), thisP2P, this.p2pInboundPort, range);
        this.sensorNode = new SensorNodeIMPL(nodeInfo);

        this.getTracer().setTitle("Node Component: " + nodeId);
        this.getTracer().setRelativePosition(1, 1);

        AbstractComponent.checkImplementationInvariant(this);
        AbstractComponent.checkInvariant(this);
    }

    @Override
    public synchronized void start() throws ComponentStartException {
        super.start();

        try {
            this.doPortConnection(this.outboundPort.getPortURI(), this.registerInboundPortURI,
                    RegistryConnector.class.getCanonicalName());
        } catch (Exception e) {

            throw new ComponentStartException(e);
        }

        try {

            this.logMessage(((NodeInfoIMPL) nodeInfo).nodeIdentifier());
            this.neighbours = outboundPort.register(nodeInfo);
            this.logMessage("neighbours:");
            neighbours.stream().forEach(x -> this.logMessage(((NodeInfoIMPL) x).nodeIdentifier()));
            this.logMessage("registered : " + outboundPort.registered(nodeInfo.nodeIdentifier()) +
                    "");
            for (NodeInfoI neighbour : neighbours) {
                NodeP2POutboundPort p2poutboundP = new NodeP2POutboundPort(AbstractOutboundPort.generatePortURI(),
                        this);
                p2poutboundP.publishPort();
                this.p2poutboundPorts.put(neighbour, p2poutboundP);
                this.ask4Connection(neighbour);
            }
        } catch (Exception e) {
            throw new ComponentStartException(e);
        }
        this.logMessage("starting NodeComponent component : " + this.nodeInfo.nodeIdentifier());
    }

    @Override
    public synchronized void finalise() throws Exception {
        this.logMessage("stopping node component : " + this.nodeInfo.nodeIdentifier());
        for (NodeInfoI neighbour : neighbours) {
            this.ask4Disconnection(neighbour);
            this.p2poutboundPorts.get(neighbour).unpublishPort();
        }

        if (this.outboundPort.connected()) {
            this.doPortDisconnection(this.outboundPort.getPortURI());
            this.outboundPort.unpublishPort();
        } // this.p2poutboundPort.unpublishPort();
        super.finalise();
        // System.out.println("NodeComponent finalise");
    }

    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        // the shutdown is a good place to unpublish inbound ports.
        try {
            if (this.outboundPort.connected()) {
                this.doPortDisconnection(this.outboundPort.getPortURI());
                this.outboundPort.unpublishPort();
            }
            if (this.inboundPort.isPublished())
                this.inboundPort.unpublishPort();
            if (this.p2pInboundPort.isPublished())
                this.p2pInboundPort.unpublishPort();
            for (NodeP2POutboundPort p2poutboundPort : this.p2poutboundPorts.values()) {
                if (p2poutboundPort.connected()) {
                    this.doPortDisconnection(p2poutboundPort.getPortURI());
                    p2poutboundPort.unpublishPort();
                }
            }
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
        // System.out.println("NodeComponent shutdown");
    }

    @Override
    public synchronized void shutdownNow() throws ComponentShutdownException {
        // the shutdown is a good place to unpublish inbound ports.
        try {
            if (this.outboundPort.connected()) {
                this.doPortDisconnection(this.outboundPort.getPortURI());
                this.outboundPort.unpublishPort();
            }
            if (this.inboundPort.isPublished())
                this.inboundPort.unpublishPort();
            if (this.p2pInboundPort.isPublished())
                this.p2pInboundPort.unpublishPort();
            for (NodeP2POutboundPort p2poutboundPort : this.p2poutboundPorts.values()) {
                if (p2poutboundPort.connected()) {
                    this.doPortDisconnection(p2poutboundPort.getPortURI());
                    p2poutboundPort.unpublishPort();
                }
            }
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdownNow();
        // System.out.println("NodeComponent shutdownNow");
    }

    public QueryResultI returnQueryResult(RequestI request) throws Exception {
        return this.sensorNode.execute(request);
    }

    public void executeAsync(RequestI request) throws Exception {
        this.sensorNode.executeAsync(request);
    }

    public void ask4Connection(NodeInfoI newNeighbour)
            throws Exception {
        try {
            NodeP2POutboundPort nodePort = this.p2poutboundPorts.get(newNeighbour);
            if (nodePort != null) {

                this.doPortConnection(nodePort.getPortURI(),
                        ((BCM4JavaEndPointDescriptorI) newNeighbour.p2pEndPointInfo()).getInboundPortURI(),
                        NodeConnector.class.getCanonicalName());
                this.logMessage("askForConnection: " + newNeighbour.nodeIdentifier() + " connected");
                nodePort.ask4Connection(this.nodeInfo);
                nodePort.getOwner().logMessage(this.nodeInfo.nodeIdentifier() + " is the one asking");
            }
        } catch (Exception e) {
            throw new Exception("Error in ask4Connection" + e.getMessage());
        }
    }

    public QueryResultI execute(RequestContinuationI request) throws Exception {
        QueryResultI tr = this.sensorNode.execute(((RequestContinuationIMPL) request).getRequest());

    }

    public void executeAsync(RequestContinuationI request) throws Exception {

    }

    public void ask4Disconnection(NodeInfoI neighbour) {
        try {
            NodeP2POutboundPort nodePort = this.p2poutboundPorts.get(neighbour);
            this.doPortDisconnection(nodePort.getPortURI());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
