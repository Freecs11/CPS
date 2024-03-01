package bcm.components;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import abstractQuery.AbstractQuery;
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
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PImplI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingImplI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;
import implementation.EndPointDescIMP;
import implementation.NodeInfoIMPL;
import implementation.PositionIMPL;
import implementation.QueryResultIMPL;
import implementation.RequestContinuationIMPL;
import implementation.RequestIMPL;
import implementation.SensorDataIMPL;
import implementation.requestsIMPL.ExecutionStateIMPL;
import implementation.requestsIMPL.ProcessingNodeIMPL;

@OfferedInterfaces(offered = { RequestingCI.class, SensorNodeP2PCI.class })
@RequiredInterfaces(required = { RegistrationCI.class, SensorNodeP2PCI.class })
public class NodeComponent extends AbstractComponent
        implements RequestingImplI, SensorNodeP2PImplI {
    protected Set<NodeInfoI> neighbours;
    private ExecutionStateIMPL context;
    private ProcessingNodeI processingNode;
    private Map<String, SensorDataI> sensors;
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
        // TODO: Need to put this into NodeInfo (p2pEndpointInfo)
        this.p2pInboundPort = new NodeP2PInboundPort(AbstractInboundPort.generatePortURI(), this);
        this.p2pInboundPort.publishPort();
        this.outboundPort = new NodeComponentOutboundPort(node_to_reg_OutboundPortURI, this);
        // TODO: Need to put this into NodeInfo (endpointInfo)
        this.inboundPort.publishPort();
        this.outboundPort.publishPort();

        this.registerInboundPortURI = registerInboundPortURI;

        EndPointDescIMP thisP2P = new EndPointDescIMP(this.inboundPort.getPortURI());
        this.nodeInfo = new NodeInfoIMPL(nodeId,
                new PositionIMPL(x, y), thisP2P, this.p2pInboundPort, range);
        this.getTracer().setTitle("Node Component: " + nodeId);
        this.getTracer().setRelativePosition(1, 1);

        this.sensors = new HashMap<>();
        SensorDataIMPL sensor = new SensorDataIMPL(nodeInfo.nodeIdentifier(), "temperature",
                20.0, Instant.now(), Double.class);
        SensorDataIMPL sensor2 = new SensorDataIMPL(nodeInfo.nodeIdentifier(), "humidity", 50.0,
                Instant.now(), Double.class);
        SensorDataIMPL sensor3 = new SensorDataIMPL(nodeInfo.nodeIdentifier(), "light", 100.0,
                Instant.now(), Double.class);
        this.sensors.put("light", sensor3);
        this.sensors.put("humidity", sensor2);
        this.sensors.put("temperature", sensor);
        this.processingNode = new ProcessingNodeIMPL(this.nodeInfo.nodePosition(), null,
                this.nodeInfo.nodeIdentifier());
        ((ProcessingNodeIMPL) this.processingNode).setSensorDataMap(this.sensors);
        // TODO: Need to change this to init a new State and then use the setter
        // (updatePN)
        this.context = new ExecutionStateIMPL(this.processingNode);
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
            ((ProcessingNodeIMPL) this.processingNode).setNeighbors(neighbours);
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

    public void executeAsync(RequestI request) throws Exception {
        // TODO Auto-generated method stub
    }

    public void ask4Connection(NodeInfoI newNeighbour)
            throws Exception {
        try {
            NodeP2POutboundPort nodePort = this.p2poutboundPorts.get(newNeighbour);
            // if the port is not created yet
            // create a new port and connect this node to the neighbor
            if (nodePort == null) {
                nodePort = new NodeP2POutboundPort(AbstractOutboundPort.generatePortURI(), this);
                nodePort.publishPort();
                this.p2poutboundPorts.put(newNeighbour, nodePort);
                this.doPortConnection(nodePort.getPortURI(),
                        ((BCM4JavaEndPointDescriptorI) newNeighbour.p2pEndPointInfo()).getInboundPortURI(),
                        NodeConnector.class.getCanonicalName());
                this.logMessage("askForConnection: " + newNeighbour.nodeIdentifier() + " connected");
                nodePort.getOwner().logMessage(this.nodeInfo.nodeIdentifier() + " is the one asking");
            }
            // if the port is already created we connect
            // and ask the neighbor to connect back
            else {
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
        ExecutionStateI state = ((RequestContinuationIMPL) request).getExecutionState();
        ProcessingNodeI lastNode = state.getProcessingNode();
        PositionI lastPosition = lastNode.getPosition();

        ((ExecutionStateIMPL) state).updateProcessingNode(this.processingNode);

        // Local execution
        if (request == null) {
            throw new Exception("Request is null");
        }
        if (request.getQueryCode() == null) {
            throw new Exception("Query is null");
        }
        AbstractQuery query = (AbstractQuery) request.getQueryCode();
        QueryResultI result = (QueryResultIMPL) query.eval(this.context);

        // we add the node to the visited nodes in the state to avoid loops
        ((ExecutionStateIMPL) state).addNodeVisited(this.nodeInfo.nodeIdentifier());
        // Directional
        if (state.isDirectional()) {
            state.incrementHops();
            ((ExecutionStateIMPL) state).addNodeVisited(this.nodeInfo.nodeIdentifier());
            if (((ExecutionStateIMPL) state).noMoreHops()) {
                return result;
            }
            // New continuation
            Direction direction = ((ExecutionStateIMPL) state).getCurrentDirection();
            NodeInfoI neighbour = this.outboundPort.findNewNeighbour(nodeInfo, direction);

            // explanation to thinh :
            // if the neighbour is null, we remove the direction from the state and we get a
            // new one if there is one
            // if the neighbour is not null, we execute the continuation on the neighbour (
            // same direction as the one we are coming from)
            // direction is updated in the state only if ( no more neighbours in the current
            // direction )
            // we also keep track of the visited nodes to avoid loops
            // code is ugly but it works

            if (neighbour == null) {
                ((ExecutionStateIMPL) state).removeDirection(direction);
                if (((ExecutionStateIMPL) state).getDirections().isEmpty()) {
                    return result;
                }
                direction = ((ExecutionStateIMPL) state).getDirections().iterator().next();
                ((ExecutionStateIMPL) state).setCurrentDirection(direction);
                neighbour = this.outboundPort.findNewNeighbour(nodeInfo, direction);
            }
            RequestContinuationI continuation = new RequestContinuationIMPL(request, state);
            if (neighbour != null
                    && !((ExecutionStateIMPL) state).getNodesVisited().contains(neighbour.nodeIdentifier())) {
                NodeP2POutboundPort nodePort = this.p2poutboundPorts.get(neighbour);
                if (nodePort != null)
                    ((QueryResultIMPL) result).update(nodePort.execute(continuation));
            }
        }

        // Flooding
        else

        {
            if (!this.context.withinMaximalDistance(this.processingNode.getPosition())) {
                return result;
            }
            Double currentMaxDist = ((ExecutionStateIMPL) state).getMaxDistance();
            Double distanceTraveled = this.processingNode.getPosition().distance(lastPosition);
            ((ExecutionStateIMPL) state).updateMaxDistance(currentMaxDist - distanceTraveled);
            // New continuation
            RequestContinuationI continuation = new RequestContinuationIMPL(request, state);
            for (NodeInfoI neighbour : neighbours) {
                if (!((ExecutionStateIMPL) state).getNodesVisited().contains(neighbour.nodeIdentifier())) {
                    NodeP2POutboundPort nodePort = this.p2poutboundPorts.get(neighbour);
                    if (nodePort != null)
                        ((QueryResultIMPL) result).update(nodePort.execute(continuation));
                }
            }
        }

        // Return final result
        return result;
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

    @Override
    public QueryResultI execute(RequestI request) throws Exception {
        if (request == null) {
            throw new Exception("Request is null");
        }
        RequestI req = (RequestIMPL) request;
        if (req.getQueryCode() == null) {
            throw new Exception("Query is null");
        }
        AbstractQuery query = (AbstractQuery) req.getQueryCode();
        QueryResultI result = (QueryResultIMPL) query.eval(this.context);
        System.err.println("STATE: " + ((ExecutionStateIMPL) this.context));

        // Check if not continuation
        if (!this.context.isContinuationSet()) {
            return result;
        }

        Set<Direction> directions = this.context.getDirections();
        System.err.println("DIRECTIONS: " + directions.toString());
        // Flooding
        if (context.isFlooding()) {
            RequestContinuationI continuation = new RequestContinuationIMPL(req, this.context);
            for (NodeInfoI neighbour : neighbours) {
                NodeP2POutboundPort nodePort = this.p2poutboundPorts.get(neighbour);
                ((QueryResultIMPL) result).update(nodePort.execute(continuation));
            }
        }

        // Directional if not flooding
        else {
            // for (Direction direction : context.getDirections()) {
            Direction direction = directions.iterator().next();
            ((ExecutionStateIMPL) this.context).setCurrentDirection(direction);
            ((ExecutionStateIMPL) this.context).addNodeVisited(this.nodeInfo.nodeIdentifier());
            RequestContinuationI continuation = new RequestContinuationIMPL(req, this.context);
            NodeInfoI neighbour = this.outboundPort.findNewNeighbour(nodeInfo, direction);
            if (neighbour != null) {
                System.err.println("NEIGHBOUR: " + neighbour.nodeIdentifier());
                NodeP2POutboundPort nodePort = this.p2poutboundPorts.get(neighbour);
                ((QueryResultIMPL) result).update(nodePort.execute(continuation));
            }
            // }
        }

        // ((QueryResultIMPL) result).update(this.execute(continuation));
        return result;
    }

}
