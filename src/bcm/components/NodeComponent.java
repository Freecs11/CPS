package bcm.components;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.EndPointDescriptorI;
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
import query.abstraction.AbstractQuery;

@OfferedInterfaces(offered = { RequestingCI.class, SensorNodeP2PCI.class })
@RequiredInterfaces(required = { RegistrationCI.class, SensorNodeP2PCI.class })
public class NodeComponent extends AbstractComponent
        implements RequestingImplI, SensorNodeP2PImplI {
    protected Set<NodeInfoI> neighbours;
    private ExecutionStateIMPL context;
    private ProcessingNodeI processingNode;
    private Map<String, SensorDataI> sensors;
    protected final NodeInfoIMPL nodeInfo;
    protected final NodeComponentInboundPort clientInboundPort;
    protected final NodeP2PInboundPort p2pInboundPort;
    protected final HashMap<NodeInfoI, NodeP2POutboundPort> nodeInfoToP2POutboundPortMap;
    protected final String p2pInboundPortURI;
    protected final NodeComponentOutboundPort node2RegistryOutboundPort;
    protected final String registerInboundPortURI;

    protected NodeComponent(String uri,
            String nodeId,
            Double x, Double y,
            Double range,
            String registryInboundPortURI) throws Exception {
        // only one thread to ensure the serialised execution of services
        // inside the component.
        super(uri, 1, 0);
        this.p2pInboundPortURI = AbstractInboundPort.generatePortURI();
        this.neighbours = new HashSet<>();
        this.clientInboundPort = new NodeComponentInboundPort(AbstractInboundPort.generatePortURI(),
                this);
        this.nodeInfoToP2POutboundPortMap = new HashMap<>();
        this.p2pInboundPort = new NodeP2PInboundPort(AbstractInboundPort.generatePortURI(), this);
        this.p2pInboundPort.publishPort();
        this.node2RegistryOutboundPort = new NodeComponentOutboundPort(AbstractOutboundPort.generatePortURI(), this);
        this.clientInboundPort.publishPort();
        this.node2RegistryOutboundPort.publishPort();
        this.registerInboundPortURI = registryInboundPortURI;
        EndPointDescriptorI endpoint = new EndPointDescIMP(this.clientInboundPort.getPortURI());
        this.nodeInfo = new NodeInfoIMPL(nodeId,
                new PositionIMPL(x, y), endpoint, this.p2pInboundPort, range);
        this.getTracer().setTitle("Node Component: " + nodeId);
        this.getTracer().setRelativePosition(1, 1);
        this.sensors = new HashMap<>();

        // TODO: Need to dynamically add sensors later
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
        this.context = new ExecutionStateIMPL();
        this.context.updateProcessingNode(this.processingNode);

        AbstractComponent.checkImplementationInvariant(this);
        AbstractComponent.checkInvariant(this);
    }

    @Override
    public synchronized void start() throws ComponentStartException {
        super.start();

        try {
            this.doPortConnection(
                    this.node2RegistryOutboundPort.getPortURI(),
                    this.registerInboundPortURI,
                    RegistryConnector.class.getCanonicalName());

            this.logMessage("Starting " + nodeInfo.nodeIdentifier());
            this.neighbours = node2RegistryOutboundPort.register(nodeInfo);
            ((ProcessingNodeIMPL) this.processingNode).setNeighbors(neighbours);

            // ----------------Print neighbours----------------
            StringBuilder sb = new StringBuilder(100);
            sb.append("neighbours : ");
            neighbours.stream().forEach(x -> {
                sb.append(x.nodeIdentifier());
                sb.append(" ");
            });
            this.logMessage(sb.toString());

            this.logMessage("Registration Success: "
                    + node2RegistryOutboundPort.registered(nodeInfo.nodeIdentifier()) + "");
            for (NodeInfoI neighbour : neighbours) {
                NodeP2POutboundPort p2poutboundP = new NodeP2POutboundPort(AbstractOutboundPort.generatePortURI(),
                        this);
                p2poutboundP.publishPort();
                this.doPortConnection(p2poutboundP.getPortURI(),
                        ((BCM4JavaEndPointDescriptorI) neighbour.p2pEndPointInfo()).getInboundPortURI(),
                        NodeConnector.class.getCanonicalName());
                this.nodeInfoToP2POutboundPortMap.put(neighbour, p2poutboundP);
                p2poutboundP.ask4Connection(this.nodeInfo);
            }
        } catch (Exception e) {
            throw new ComponentStartException(e);
        }
        this.logMessage("Node Component successfully started: " + this.nodeInfo.nodeIdentifier());
    }

    public void ask4Disconnection(NodeInfoI neighbour) {
        System.err.println("TRYING in ask4Disconnection ");

        try {
            System.err.println("ask4Disconnection here l160");
            NodeP2POutboundPort nodePort = this.nodeInfoToP2POutboundPortMap.get(neighbour);
            System.err.println("NodePort: " + nodePort);
            // TODO: is this really necessary?
            if (nodePort == null) {
                this.neighbours.remove(neighbour);
                return;
            }
            Direction directionOfNeighbour = this.nodeInfo.nodePosition().directionFrom(neighbour.nodePosition());
            this.neighbours.remove(neighbour);
            this.nodeInfoToP2POutboundPortMap.remove(neighbour);
            // nodePort.ask4Disconnection(this.nodeInfo);
            this.doPortDisconnection(nodePort.getPortURI());
            nodePort.unpublishPort();
            this.logMessage("ask4Disconnection: " + neighbour.nodeIdentifier() + " disconnected");
            this.logMessage(this.nodeInfo.nodeIdentifier() + " looking for new neighbour in direction "
                    + directionOfNeighbour);
            if (this.node2RegistryOutboundPort.connected()) {
                NodeInfoI newNeighbour = this.node2RegistryOutboundPort.findNewNeighbour(this.nodeInfo,
                        directionOfNeighbour);
                if (newNeighbour == null || newNeighbour == neighbour) {
                    return;
                } else if (newNeighbour != null) {
                    this.logMessage("Found new neighbour in direction " + directionOfNeighbour +
                            " : "
                            + newNeighbour.nodeIdentifier());
                    NodeP2POutboundPort newPort = new NodeP2POutboundPort(AbstractOutboundPort.generatePortURI(), this);
                    newPort.publishPort();
                    this.nodeInfoToP2POutboundPortMap.put(newNeighbour, newPort);
                    newPort.ask4Connection(this.nodeInfo);
                }
            }
        } catch (Exception e) {
            System.err.println("Error in ask4Disconnection " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void finalise() throws Exception {
        this.logMessage("stopping node component : " + this.nodeInfo.nodeIdentifier());
        for (NodeInfoI neighbour : neighbours) {
            // this.ask4Disconnection(neighbour);
            // I think this is more correct
            NodeP2POutboundPort nodePort = this.nodeInfoToP2POutboundPortMap.get(neighbour);
            nodePort.ask4Disconnection(this.nodeInfo);
        }
        for (NodeP2POutboundPort p2poutboundPort : this.nodeInfoToP2POutboundPortMap.values()) {
            if (p2poutboundPort.connected()) {
                this.doPortDisconnection(p2poutboundPort.getPortURI());
                p2poutboundPort.unpublishPort();
            }
        }
        if (this.node2RegistryOutboundPort.connected()) {
            this.node2RegistryOutboundPort.unregister(this.nodeInfo.nodeIdentifier());
            this.doPortDisconnection(this.node2RegistryOutboundPort.getPortURI());
            this.node2RegistryOutboundPort.unpublishPort();
        }
        super.finalise();
        // System.out.println("NodeComponent finalise");
    }

    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        // the shutdown is a good place to unpublish inbound ports.
        try {
            // TODO: I think this is redundant because it's already done in finalise
            // if (this.outboundPort.connected()) {
            // this.doPortDisconnection(this.outboundPort.getPortURI());
            // this.outboundPort.unpublishPort();
            // }
            // for (NodeP2POutboundPort p2poutboundPort : this.p2poutboundPorts.values()) {
            // if (p2poutboundPort.connected()) {
            // this.doPortDisconnection(p2poutboundPort.getPortURI());
            // p2poutboundPort.unpublishPort();
            // }
            // }
            if (this.clientInboundPort.isPublished())
                this.clientInboundPort.unpublishPort();
            if (this.p2pInboundPort.isPublished())
                this.p2pInboundPort.unpublishPort();

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
            // if (this.outboundPort.connected()) {
            // this.doPortDisconnection(this.outboundPort.getPortURI());
            // this.outboundPort.unpublishPort();
            // }
            // for (NodeP2POutboundPort p2poutboundPort : this.p2poutboundPorts.values()) {
            // if (p2poutboundPort.connected()) {
            // this.doPortDisconnection(p2poutboundPort.getPortURI());
            // p2poutboundPort.unpublishPort();
            // }
            // }
            if (this.clientInboundPort.isPublished())
                this.clientInboundPort.unpublishPort();
            if (this.p2pInboundPort.isPublished())
                this.p2pInboundPort.unpublishPort();
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
            // check the direction of the new neighbour and add it to the list of neighbours
            // if it's the mininmum distance in that direction
            Direction direction = this.nodeInfo.nodePosition().directionFrom(newNeighbour.nodePosition()); // direction
            // // of the new neighbour
            // Boolean isMin = true;
            // NodeInfoI neighbourMinDist = null;
            // for (NodeInfoI neighbour : this.neighbours) {
            // if (neighbour.nodePosition().directionFrom(this.nodeInfo.nodePosition()) ==
            // direction) {
            // // keep track of the neighbour that is the closest in the same direction as
            // this
            // // one
            // if (neighbour.nodePosition().distance(this.nodeInfo.nodePosition()) <
            // newNeighbour.nodePosition()
            // .distance(this.nodeInfo.nodePosition())) {
            // isMin = false; // if there is a neighbour closer in the same direction
            // neighbourMinDist = neighbour; // we don't check the distances between the
            // neighbours already in
            // // the list because we ensured that
            // // there is only 1 neighbour in each direction and 4 total max (N, S, E, W)
            // }
            // }

            // if (Boolean.TRUE.equals(isMin)) {
            // this.neighbours.add(newNeighbour);
            // this.neighbours.remove(neighbourMinDist); // remove the neighbour that is not
            // the closest in the same
            // // direction
            // this.ask4Disconnection(neighbourMinDist); // disconnect it, so we can connect
            // the new one
            // NodeP2POutboundPort nodePort = this.p2poutboundPorts.get(newNeighbour);
            // // if the port is not created yet
            // // create a new port and connect this node to the neighbor
            // if (nodePort == null) {
            // nodePort = new NodeP2POutboundPort(AbstractOutboundPort.generatePortURI(),
            // this);
            // nodePort.publishPort();
            // this.p2poutboundPorts.put(newNeighbour, nodePort);
            // this.doPortConnection(nodePort.getPortURI(),
            // ((BCM4JavaEndPointDescriptorI)
            // newNeighbour.p2pEndPointInfo()).getInboundPortURI(),
            // NodeConnector.class.getCanonicalName());
            // this.logMessage("askForConnection: " + newNeighbour.nodeIdentifier() + "
            // connected");
            // nodePort.getOwner().logMessage(this.nodeInfo.nodeIdentifier() + " is the one
            // asking");
            // }
            // // if the port is already created we connect
            // // and ask the neighbor to connect back
            // else {
            // this.doPortConnection(nodePort.getPortURI(),
            // ((BCM4JavaEndPointDescriptorI)
            // newNeighbour.p2pEndPointInfo()).getInboundPortURI(),
            // NodeConnector.class.getCanonicalName());
            // this.logMessage("askForConnection: " + newNeighbour.nodeIdentifier() + "
            // connected");
            // nodePort.ask4Connection(this.nodeInfo);
            // nodePort.getOwner().logMessage(this.nodeInfo.nodeIdentifier() + " is the one
            // asking");
            // }
            // }

            NodeInfoI neighbourInTheDirection = null;

            for (NodeInfoI neighbour : this.neighbours) {
                if (this.nodeInfo.nodePosition().directionFrom(neighbour.nodePosition()) == direction) {
                    neighbourInTheDirection = neighbour;

                }
            }

            if (neighbourInTheDirection == null) {
                this.neighbours.add(newNeighbour);
                NodeP2POutboundPort nodePort = new NodeP2POutboundPort(AbstractOutboundPort.generatePortURI(), this);
                nodePort.publishPort();
                this.nodeInfoToP2POutboundPortMap.put(newNeighbour, nodePort);
                this.doPortConnection(
                        nodePort.getPortURI(),
                        ((BCM4JavaEndPointDescriptorI) newNeighbour.p2pEndPointInfo()).getInboundPortURI(),
                        NodeConnector.class.getCanonicalName());
                this.logMessage(newNeighbour.nodeIdentifier() + " connected");
            } else {
                System.err.println("neighbourInTheDirection: " + neighbourInTheDirection);

                this.neighbours.remove(neighbourInTheDirection);
                this.neighbours.add(newNeighbour);

                System.err.println("Ligne 360");
                NodeP2POutboundPort nodePort = this.nodeInfoToP2POutboundPortMap.get(neighbourInTheDirection);
                System.err.println("Ligne 361: ");
                for (NodeInfoI neighbour : this.neighbours) {
                    System.err.print(neighbour.nodeIdentifier());
                    System.err.print(", ");
                }
                System.err.println();
                System.err.println("Current Node: " + newNeighbour.nodeIdentifier());
                nodePort.ask4Disconnection(this.nodeInfo);
                System.err.println("Ligne 362");
                this.doPortDisconnection(nodePort.getPortURI());
                System.err.println("removed : " + neighbourInTheDirection);
                System.err.println("New Neighbor: " + newNeighbour);

                nodePort.unpublishPort();
                this.nodeInfoToP2POutboundPortMap.remove(neighbourInTheDirection);

                NodeP2POutboundPort newPort = new NodeP2POutboundPort(AbstractOutboundPort.generatePortURI(), this);
                newPort.publishPort();
                this.nodeInfoToP2POutboundPortMap.put(newNeighbour, newPort);
                this.doPortConnection(
                        newPort.getPortURI(),
                        ((BCM4JavaEndPointDescriptorI) newNeighbour.p2pEndPointInfo()).getInboundPortURI(),
                        NodeConnector.class.getCanonicalName());
                this.logMessage(newNeighbour.nodeIdentifier() + " connected");
            }

        } catch (Exception e) {
            throw new Exception("Error in ask4Connection" + e.getMessage());
        }
    }

    public QueryResultI execute(RequestContinuationI request) throws Exception {
        if (request == null) {
            throw new Exception("Request is null");
        }
        ExecutionStateI state = ((RequestContinuationIMPL) request).getExecutionState();
        ProcessingNodeI lastNode = state.getProcessingNode();
        PositionI lastPosition = lastNode.getPosition();

        ((ExecutionStateIMPL) state).updateProcessingNode(this.processingNode);
        // Local execution
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
            NodeInfoI neighbour = this.node2RegistryOutboundPort.findNewNeighbour(nodeInfo, direction);

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
                neighbour = this.node2RegistryOutboundPort.findNewNeighbour(nodeInfo, direction);
            }
            RequestContinuationI continuation = new RequestContinuationIMPL(request, state);
            if (neighbour != null
                    && !((ExecutionStateIMPL) state).getNodesVisited().contains(neighbour.nodeIdentifier())) {
                NodeP2POutboundPort nodePort = this.nodeInfoToP2POutboundPortMap.get(neighbour);
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
                    NodeP2POutboundPort nodePort = this.nodeInfoToP2POutboundPortMap.get(neighbour);
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

        // Set<Direction> directions = this.context.getDirections();
        // System.err.println("DIRECTIONS: " + directions.toString());
        // Flooding
        if (context.isFlooding()) {
            RequestContinuationI continuation = new RequestContinuationIMPL(req, this.context);
            for (NodeInfoI neighbour : neighbours) {
                NodeP2POutboundPort nodePort = this.nodeInfoToP2POutboundPortMap.get(neighbour);
                ((QueryResultIMPL) result).update(nodePort.execute(continuation));
            }
        }

        // Directional if not flooding
        else {
            // for (Direction direction : context.getDirections()) {
            Direction direction = ((ExecutionStateIMPL) this.context).getCurrentDirection();
            ((ExecutionStateIMPL) this.context).addNodeVisited(this.nodeInfo.nodeIdentifier());
            RequestContinuationI continuation = new RequestContinuationIMPL(req, this.context);
            NodeInfoI neighbour = this.node2RegistryOutboundPort.findNewNeighbour(nodeInfo, direction);
            if (neighbour != null) {
                System.err.println("NEIGHBOUR: " + neighbour.nodeIdentifier());
                NodeP2POutboundPort nodePort = this.nodeInfoToP2POutboundPortMap.get(neighbour);
                ((QueryResultIMPL) result).update(nodePort.execute(continuation));
            }
            // }
        }

        // ((QueryResultIMPL) result).update(this.execute(continuation));
        return result;
    }

}
