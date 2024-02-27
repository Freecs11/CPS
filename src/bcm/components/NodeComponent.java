package bcm.components;

import java.util.HashSet;
import java.util.Set;

import bcm.connector.NodeConnector;
import bcm.connector.RegistryConnector;
import bcm.interfaces.ports.NodeComponentInboundPort;
import bcm.interfaces.ports.NodeComponentOutboundPort;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI;
import implementation.EndPointDescIMP;
import implementation.NodeInfoIMPL;
import implementation.PositionIMPL;
import implementation.SensorNodeIMPL;

@OfferedInterfaces(offered = { RequestingCI.class, SensorNodeP2PCI.class })
@RequiredInterfaces(required = { RegistrationCI.class })
public class NodeComponent extends AbstractComponent {
    protected Set<NodeInfoI> neighbours;
    protected final SensorNodeIMPL sensorNode;
    protected final NodeInfoIMPL nodeInfo;
    protected final NodeComponentInboundPort inboundPort;
    protected final NodeComponentOutboundPort outboundPort;

    protected NodeComponent(String uri,
            String sensorNodeInboundPortURI,
            String node_to_reg_OutboundPortURI,
            String nodeId,
            Double x, Double y,
            Double range) throws Exception {
        // only one thread to ensure the serialised execution of services
        // inside the component.
        super(uri, 1, 0);
        assert sensorNodeInboundPortURI != null;
        assert node_to_reg_OutboundPortURI != null;
        this.neighbours = new HashSet<>();
        this.inboundPort = new NodeComponentInboundPort(sensorNodeInboundPortURI,
                this);
        this.outboundPort = new NodeComponentOutboundPort(node_to_reg_OutboundPortURI, this);
        this.inboundPort.publishPort();
        this.outboundPort.publishPort();
        EndPointDescIMP thisP2P = new EndPointDescIMP(this.inboundPort.getPortURI());
        this.nodeInfo = new NodeInfoIMPL(nodeId,
                new PositionIMPL(x, y), thisP2P, this.inboundPort, range);
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
            this.doPortConnection(this.outboundPort.getPortURI(), "register-inbound-port",
                    RegistryConnector.class.getCanonicalName());
        } catch (Exception e) {

            throw new ComponentStartException(e);
        }

        try {
            this.logMessage(((NodeInfoIMPL) nodeInfo).nodeIdentifier());
            this.neighbours = outboundPort.register(nodeInfo);
            this.logMessage("neighbours:");

            neighbours.stream().forEach(x -> this.logMessage(((NodeInfoIMPL) x).nodeIdentifier()));
            this.logMessage("registered : " + outboundPort.registered(nodeInfo.nodeIdentifier()) + "");
            for (NodeInfoI neighbour : neighbours) {
                this.ask4Connection(neighbour);
            }
        } catch (Exception e) {
            throw new ComponentStartException(e);
        }
        this.logMessage("starting NodeComponent component.");
    }

    @Override
    public synchronized void finalise() throws Exception {
        this.logMessage("stopping provider component.");
        this.printExecutionLogOnFile("provider");

        super.finalise();
        // System.out.println("NodeComponent finalise");
    }

    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        // the shutdown is a good place to unpublish inbound ports.
        try {
            this.inboundPort.unpublishPort();
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
            this.inboundPort.unpublishPort();
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
            this.doPortConnection(this.outboundPort.getPortURI(),
                    ((EndPointDescIMP) newNeighbour.endPointInfo()).getURI(),
                    NodeConnector.class.getCanonicalName());
            this.logMessage("askForConnection: " + newNeighbour.nodeIdentifier() + " connected");
        } catch (Exception e) {
            throw new Exception("Error in ask4Connection");
        }
    }

    public QueryResultI execute(RequestContinuationI request) throws Exception {
        return null;
    }

    public void executeAsync(RequestContinuationI request) throws Exception {

    }

    public void ask4Disconnection(NodeInfoI neighbour) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'ask4Disconnection'");
    }

}
