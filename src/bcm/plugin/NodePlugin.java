package bcm.plugin;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import bcm.connectors.ClientRequestResult;
import bcm.connectors.RegistryConnector;
import bcm.connectors.SensorNodeConnector;
import bcm.ports.RegistrationOutboundPort;
import bcm.ports.RequestResultOutboundPort;
import bcm.ports.RequestingInboundPort;
import bcm.ports.SensorNodeP2PInboundPort;
import bcm.ports.SensorNodeP2POutboundPort;
import fr.sorbonne_u.components.AbstractPlugin;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.BCM4JavaEndPointDescriptorI;
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.EndPointDescriptorI;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestResultCI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;
import implementation.EndPointDescIMPL;
import implementation.NodeInfoIMPL;
import implementation.QueryResultIMPL;
import implementation.RequestContinuationIMPL;
import implementation.request.ExecutionStateIMPL;
import implementation.request.ProcessingNodeIMPL;
import query.abstraction.AbstractQuery;

public class NodePlugin
        extends AbstractPlugin {
    private static final long serialVersionUID = 1L;

    private Set<NodeInfoI> neighbours;
    private ProcessingNodeI processingNode;
    // Uris des requetes deja executees
    private Set<String> requestURIs;
    private NodeInfoI nodeInfo;
    private ArrayList<SensorDataI> sensorData;

    protected ConcurrentHashMap<NodeInfoI, SensorNodeP2POutboundPort> nodeInfoToP2POutboundPortMap;

    protected RequestingInboundPort requestingInboundPort;
    protected SensorNodeP2PInboundPort sensorNodeP2PInboundPort;
    protected RegistrationOutboundPort RegistrationOutboundPort;
    protected RequestResultOutboundPort requestResultOutboundPort;

    protected String registerInboundPortURI;

    private final ReadWriteLock neigboursLock = new ReentrantReadWriteLock();

    public NodePlugin(String registerInboundPortURI, NodeInfoI nodeInfo,
            ArrayList<SensorDataI> sensorData)
            throws Exception {
        // port URI for the registry
        this.registerInboundPortURI = registerInboundPortURI;
        // initialisation des listes
        this.requestURIs = new HashSet<>();
        this.neighbours = new HashSet<>();
        this.nodeInfoToP2POutboundPortMap = new ConcurrentHashMap<>();

        // initialisation d'endpoint et p2pendpoint
        // EndPointDescriptorI p2pendpoint = new
        // EndPointDescIMPL(this.sensorNodeP2PInboundPort.getPortURI(),
        // SensorNodeP2PCI.class);
        // EndPointDescriptorI endpoint = new
        // EndPointDescIMPL(this.requestingInboundPort.getPortURI(),
        // RequestingCI.class);

        // initialisation de l'objet nodeInfo
        this.nodeInfo = nodeInfo;
        this.sensorData = sensorData;
        // ((NodeInfoIMPL) this.nodeInfo).setP2pEndPointInfo(p2pendpoint);
        // ((NodeInfoIMPL) this.nodeInfo).setEndPointInfo(endpoint);
        // initialisation de l'objet processingNode
        // this.processingNode = new ProcessingNodeIMPL(nodeInfo.nodePosition(), null,
        // nodeInfo.nodeIdentifier());
        // ((ProcessingNodeIMPL) (this.processingNode)).addAllSensorData(sensorData);
    }

    @Override
    public void installOn(ComponentI owner) throws Exception {
        super.installOn(owner);
        System.err.println("NodePlugin installed on " + owner);

        this.addRequiredInterface(RegistrationCI.class);
        this.addRequiredInterface(SensorNodeP2PCI.class);
        this.addRequiredInterface(RequestingCI.class);
        this.addRequiredInterface(RequestResultCI.class);
        this.addOfferedInterface(RequestingCI.class);
        this.addOfferedInterface(SensorNodeP2PCI.class);
    }

    @Override
    public void initialise() throws Exception {
        this.requestingInboundPort = new RequestingInboundPort(this.getOwner());
        this.requestingInboundPort.publishPort();
        this.sensorNodeP2PInboundPort = new SensorNodeP2PInboundPort(this.getOwner());
        this.sensorNodeP2PInboundPort.publishPort();
        this.RegistrationOutboundPort = new RegistrationOutboundPort(this.getOwner());
        this.RegistrationOutboundPort.publishPort();
        this.requestResultOutboundPort = new RequestResultOutboundPort(this.getOwner());
        this.requestResultOutboundPort.publishPort();

        // initialisation d'endpoint et p2pendpoint
        EndPointDescriptorI p2pendpoint = new EndPointDescIMPL(this.sensorNodeP2PInboundPort.getPortURI(),
                SensorNodeP2PCI.class);
        EndPointDescriptorI endpoint = new EndPointDescIMPL(this.requestingInboundPort.getPortURI(),
                RequestingCI.class);

        ((NodeInfoIMPL) this.nodeInfo).setP2pEndPointInfo(p2pendpoint);
        ((NodeInfoIMPL) this.nodeInfo).setEndPointInfo(endpoint);
        this.processingNode = new ProcessingNodeIMPL(nodeInfo.nodePosition(), null,
                nodeInfo.nodeIdentifier());
        ((ProcessingNodeIMPL) (this.processingNode)).addAllSensorData(sensorData);

        this.neighbours = new HashSet<>();
        this.requestURIs = new HashSet<>();
        this.nodeInfoToP2POutboundPortMap = new ConcurrentHashMap<>();
        this.getOwner().doPortConnection(
                this.RegistrationOutboundPort.getPortURI(),
                this.registerInboundPortURI,
                RegistryConnector.class.getCanonicalName());
        super.initialise();
    }

    @Override
    public void finalise() throws Exception {
        // MEC FAIS LE
        if (this.RegistrationOutboundPort.connected()) {
            this.getOwner().doPortDisconnection(this.RegistrationOutboundPort.getPortURI());
        }
        if (this.RegistrationOutboundPort.isPublished())
            this.RegistrationOutboundPort.unpublishPort();
        for (SensorNodeP2POutboundPort p2poutboundPort : this.nodeInfoToP2POutboundPortMap.values()) {
            if (p2poutboundPort.connected()) {
                this.getOwner().doPortDisconnection(p2poutboundPort.getPortURI());
            }
            if (p2poutboundPort.isPublished())
                p2poutboundPort.unpublishPort();
        }

        if (this.requestResultOutboundPort.connected()) {
            this.getOwner().doPortDisconnection(this.requestResultOutboundPort.getPortURI());

        }
        if (this.requestResultOutboundPort.isPublished())
            this.requestResultOutboundPort.unpublishPort();

        super.finalise();
    }

    @Override
    public void uninstall() throws Exception {
        if (requestingInboundPort.isPublished())
            this.requestingInboundPort.unpublishPort();
        if (sensorNodeP2PInboundPort.isPublished())
            this.sensorNodeP2PInboundPort.unpublishPort();
        super.uninstall();
    }

    // ----------------- PLUGIN METHODS -----------------

    public void ask4Connection(NodeInfoI newNeighbour)
            throws Exception {
        try {
            Direction direction = this.nodeInfo.nodePosition().directionFrom(newNeighbour.nodePosition()); // direction
            NodeInfoI neighbourInTheDirection = null;

            Iterator<NodeInfoI> it = new HashSet<>(neighbours).iterator(); // Copie pour éviter
                                                                           // ConcurrentModificationException
            while (it.hasNext()) {
                NodeInfoI neighbour = it.next();
                if (this.nodeInfo.nodePosition().directionFrom(neighbour.nodePosition()) == direction) {
                    neighbourInTheDirection = neighbour;
                    break;
                }
            }

            System.err.println("Neighbour in the direction: " + neighbourInTheDirection);

            if (neighbourInTheDirection == null) {
                SensorNodeP2POutboundPort newPort = new SensorNodeP2POutboundPort(
                        AbstractOutboundPort.generatePortURI(), this.getOwner());
                newPort.publishPort();
                this.nodeInfoToP2POutboundPortMap.put(newNeighbour, newPort);
                this.getOwner().doPortConnection(newPort.getPortURI(),
                        ((BCM4JavaEndPointDescriptorI) newNeighbour.p2pEndPointInfo()).getInboundPortURI(),
                        SensorNodeConnector.class.getCanonicalName());
                newPort.ask4Connection(this.nodeInfo);

                this.addNeighbour(newNeighbour);

                this.logMessage("ask4Connection: " + newNeighbour.nodeIdentifier() + " connected");
                this.printNeighbours();
            } else {
                // ------- compare the distance between the new neighbour and the neighbour in
                // the same direction
                double distanceNewNeighbour = this.nodeInfo.nodePosition().distance(newNeighbour.nodePosition());
                double distanceNeighbourInTheDirection = this.nodeInfo.nodePosition()
                        .distance(neighbourInTheDirection.nodePosition());
                if (distanceNewNeighbour < distanceNeighbourInTheDirection) {
                    SensorNodeP2POutboundPort newPort = new SensorNodeP2POutboundPort(
                            AbstractOutboundPort.generatePortURI(), this.getOwner());
                    newPort.publishPort();
                    this.nodeInfoToP2POutboundPortMap.put(newNeighbour, newPort);
                    this.getOwner().doPortConnection(newPort.getPortURI(),
                            ((BCM4JavaEndPointDescriptorI) newNeighbour.p2pEndPointInfo()).getInboundPortURI(),
                            SensorNodeConnector.class.getCanonicalName());
                    newPort.ask4Connection(this.nodeInfo);

                    this.addNeighbour(newNeighbour);

                    this.logMessage("ask4Connection: " + newNeighbour.nodeIdentifier() + " connected");
                    this.logMessage("Disconnecting neighbour in the same direction: "
                            + neighbourInTheDirection.nodeIdentifier());
                    this.ask4Disconnection(neighbourInTheDirection);
                    this.logMessage(
                            "Neighbours after disconnection: -->;" + neighbourInTheDirection.nodeIdentifier()
                                    + " disconnected");
                    this.printNeighbours();
                } else {
                    this.logMessage("ask4Connection: " + newNeighbour.nodeIdentifier() + " not connected");
                    this.logMessage("Distance between " + newNeighbour.nodeIdentifier() + " and "
                            + neighbourInTheDirection.nodeIdentifier() + " is less than the distance between "
                            + newNeighbour.nodeIdentifier() + " and " + neighbourInTheDirection.nodeIdentifier());
                }
            }
        } catch (Exception e) {
            throw new Exception("Error in ask4Connection" + e.getMessage());
        }
    }

    public void ask4Disconnection(NodeInfoI neighbour) {
        try {
            SensorNodeP2POutboundPort nodePort = this.nodeInfoToP2POutboundPortMap.get(neighbour);
            if (nodePort == null) {
                this.logMessage("ask4Disconnection: " + neighbour.nodeIdentifier() + " not connected");
                return;
            }

            Direction directionOfNeighbour = this.nodeInfo.nodePosition().directionFrom(neighbour.nodePosition());

            this.removeNeighbour(neighbour);

            this.nodeInfoToP2POutboundPortMap.remove(neighbour);
            this.getOwner().doPortDisconnection(nodePort.getPortURI());
            nodePort.unpublishPort();

            // ----- Find new in the same direction if possible -----
            NodeInfoI newNeighbour = this.RegistrationOutboundPort.findNewNeighbour(this.nodeInfo,
                    directionOfNeighbour);
            if (newNeighbour != null && newNeighbour.nodeIdentifier() != neighbour.nodeIdentifier()) {
                SensorNodeP2POutboundPort newPort = new SensorNodeP2POutboundPort(
                        AbstractOutboundPort.generatePortURI(), this.getOwner());
                newPort.publishPort();
                this.nodeInfoToP2POutboundPortMap.put(newNeighbour, newPort);
                this.getOwner().doPortConnection(newPort.getPortURI(),
                        ((BCM4JavaEndPointDescriptorI) newNeighbour.p2pEndPointInfo()).getInboundPortURI(),
                        SensorNodeConnector.class.getCanonicalName());
                newPort.ask4Connection(this.nodeInfo);

                this.addNeighbour(newNeighbour);

                this.logMessage("ask4Disconnection: " + neighbour.nodeIdentifier() + " disconnected");
                this.logMessage("Found new neighbour in direction " + directionOfNeighbour + " : "
                        + newNeighbour.nodeIdentifier());
            } else {
                this.logMessage("ask4Disconnection: " + neighbour.nodeIdentifier() + " disconnected");
                this.logMessage("No new neighbour found in direction " + directionOfNeighbour);
            }

            this.logMessage("Neighbours after disconnection: -->@;" + neighbour.nodeIdentifier() + " disconnected");
            this.logMessage("<Neighbours after disconnection: -->@;" + printNeighbours());

        } catch (Exception e) {
            System.err.println("Error in ask4Disconnection " + e.getMessage());
            e.printStackTrace();
        }
        // }
    }

    public void addNeighbour(NodeInfoI neighbour) {
        neigboursLock.writeLock().lock();
        try {
            neighbours.add(neighbour);
        } finally {
            neigboursLock.writeLock().unlock();
        }
    }

    public void removeNeighbour(NodeInfoI neighbour) {
        neigboursLock.writeLock().lock();
        try {
            neighbours.remove(neighbour);
        } finally {
            neigboursLock.writeLock().unlock();
        }
    }

    public String printNeighbours() {
        StringBuilder sb = new StringBuilder();
        sb.append("Neighbours of " + this.nodeInfo.nodeIdentifier() + " : ");
        for (NodeInfoI neighbour : neighbours) {
            sb.append(neighbour.nodeIdentifier() + ", ");
        }
        return sb.toString();
    }

    public void connect2Neighbours() throws ComponentStartException {
        try {
            Iterator<NodeInfoI> it = new HashSet<>(neighbours).iterator(); // Copie pour éviter
                                                                           // ConcurrentModificationException
            while (it.hasNext()) {
                NodeInfoI neighbour = it.next();
                SensorNodeP2POutboundPort p2poutboundP = new SensorNodeP2POutboundPort(
                        AbstractOutboundPort.generatePortURI(),
                        this.getOwner());
                p2poutboundP.publishPort();
                this.getOwner().doPortConnection(p2poutboundP.getPortURI(),
                        ((BCM4JavaEndPointDescriptorI) neighbour.p2pEndPointInfo()).getInboundPortURI(),
                        SensorNodeConnector.class.getCanonicalName());
                this.nodeInfoToP2POutboundPortMap.put(neighbour, p2poutboundP);
                System.err.println("Connecting to neighbour: " + neighbour.nodeIdentifier());
                logMessage("Connecting to neighbour: " + neighbour.nodeIdentifier());
                p2poutboundP.ask4Connection(this.nodeInfo);
            }
        } catch (

        Exception e) {
            throw new ComponentStartException(e);
        }
    }

    public void registerNode() throws Exception {
        try {
            // ----------------- REGISTRATION -----------------
            this.neighbours = RegistrationOutboundPort.register(nodeInfo);
            ((ProcessingNodeIMPL) this.processingNode).setNeighbors(neighbours);
            this.getOwner().logMessage(this.printNeighbours());
            this.getOwner().logMessage("Registration Success: "
                    + RegistrationOutboundPort.registered(nodeInfo.nodeIdentifier()) + "");
            this.getOwner().logMessage("Connecting to all the neighbours received from the registry at Time : "
                    + Instant.now() + " .....................");
            this.connect2Neighbours();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.getOwner().logMessage("Node Component successfully executed: " +
                this.nodeInfo.nodeIdentifier());
    }

    public QueryResultI execute(RequestContinuationI request) throws Exception {
        this.logMessage("Executing RequestContinuationI: " + this.nodeInfo.nodeIdentifier());
        for (NodeInfoI neighbour : neighbours) {
            this.logMessage("Neighbour : of" + this.nodeInfo.nodeIdentifier() + "->" + neighbour.nodeIdentifier());
        }
        this.logMessage("-----------------------------------------------------------------------------\n");

        if (request == null) {
            throw new Exception("request is null");
        }

        if (request.getQueryCode() == null) {
            throw new Exception("Query is null");
        }

        synchronized (this.requestURIs) {
            if (this.requestURIs.contains(request.requestURI())) {
                this.logMessage("Request URI: " + request.requestURI() + " already executed");
                return new QueryResultIMPL();
            }
            this.requestURIs.add(request.requestURI());
        }

        ExecutionStateI state = ((RequestContinuationIMPL) request).getExecutionState();
        if (state == null) {
            throw new Exception("State is null");
        }
        System.err.println("executing RequestContinuationI: " + request.toString());

        state.updateProcessingNode(this.processingNode);
        System.err.println("Processing Node: " + this.processingNode.getNodeIdentifier());

        // Execute the query
        AbstractQuery query = (AbstractQuery) request.getQueryCode();
        QueryResultI result = query.eval(state);

        if (state.isFlooding()) {
            // Propagate the request to neighbours and update the result
            return this.floodingPropagation(state, request, result);
        } else if (state.isDirectional()) {
            // Can't propagate if no more hops
            // Return local result
            this.logMessage(
                    "Directional Propagation , number of hops: " + ((ExecutionStateIMPL) state).getHops() + "\n");
            if (((ExecutionStateIMPL) state).noMoreHops()) {
                return result;
            }
            // Propagate the request to neighbours and update the result
            this.logMessage("Directional Propagation not ended\n");
            return this.directionalPropagation(state, request, result);
        }

        // Return the final result
        return result;

    }

    public QueryResultI execute(RequestI request) throws Exception {
        this.logMessage("Executing Request: " + this.nodeInfo.nodeIdentifier());
        for (NodeInfoI neighbour : neighbours) {
            this.logMessage(
                    "Neighbour 2 : of" + this.nodeInfo.nodeIdentifier() + "->" + neighbour.nodeIdentifier());
        }
        this.logMessage("--------------------------------------------------------------------------------\n");
        if (request == null) {
            throw new Exception("request is null");
        }

        if (request.getQueryCode() == null) {
            throw new Exception("Query is null");
        }

        synchronized (this.requestURIs) {
            if (this.requestURIs.contains(request.requestURI())) {
                this.logMessage("Request URI: " + request.requestURI() + " already executed");
                return new QueryResultIMPL();
            }
            this.requestURIs.add(request.requestURI());
        }

        AbstractQuery query = (AbstractQuery) request.getQueryCode();
        ExecutionStateI state = new ExecutionStateIMPL(this.processingNode);
        QueryResultI result = query.eval(state);
        if (!state.isContinuationSet()) {
            return result;
        } else if (state.isFlooding()) {
            // Propagate the request to neighbours and update the result
            return this.floodingPropagation(state, request, result);
        } else if (state.isDirectional()) {
            // Can't propagate if no more hops
            // Return local result
            if (((ExecutionStateIMPL) state).noMoreHops()) {
                return result;
            }
            // Propagate the request to neighbours and update the result
            this.logMessage("Directional Propagation not ended\n");
            return this.directionalPropagation(state, request, result);
        }

        // Return the final result
        return result;

    }

    private QueryResultI directionalPropagation(ExecutionStateI state, RequestI request, QueryResultI result)
            throws Exception {
        Iterator<NodeInfoI> it = new HashSet<>(neighbours).iterator();
        while (it.hasNext()) {
            NodeInfoI neighbour = it.next();
            this.logMessage("propagation " + neighbour.nodeIdentifier());
            if (state.getDirections()
                    .contains(processingNode.getPosition().directionFrom(neighbour.nodePosition()))) {
                SensorNodeP2POutboundPort nodePort = this.nodeInfoToP2POutboundPortMap.get(neighbour);
                this.logMessage("successfully\n");
                if (nodePort != null) {
                    ExecutionStateI newState = new ExecutionStateIMPL(state);
                    newState.incrementHops();
                    RequestContinuationI continuation = new RequestContinuationIMPL(
                            request.requestURI(), request.getQueryCode(), request.isAsynchronous(),
                            request.clientConnectionInfo(), newState);
                    QueryResultI res = nodePort.execute(continuation);
                    ((QueryResultIMPL) result).update(res);
                }
            }
        }
        return result;
    }

    private QueryResultI floodingPropagation(ExecutionStateI state, RequestI request, QueryResultI result)
            throws Exception {
        Iterator<NodeInfoI> it = new HashSet<>(neighbours).iterator();
        while (it.hasNext()) {
            NodeInfoI neighbour = it.next();
            if (state.withinMaximalDistance(neighbour.nodePosition())) {
                SensorNodeP2POutboundPort nodePort = this.nodeInfoToP2POutboundPortMap.get(neighbour);
                if (nodePort != null) {
                    double newMaximalDistance = ((ExecutionStateIMPL) state).getMaxDistance()
                            - processingNode.getPosition().distance(neighbour.nodePosition());
                    ExecutionStateIMPL newState = new ExecutionStateIMPL(state);
                    newState.updateMaxDistance(newMaximalDistance);
                    // Create new continuation
                    RequestContinuationI continuation = new RequestContinuationIMPL(
                            request.requestURI(), request.getQueryCode(), request.isAsynchronous(),
                            request.clientConnectionInfo(), newState);
                    // Execute the continuation
                    QueryResultI res = nodePort.execute(continuation);
                    // Update the result
                    ((QueryResultIMPL) result).update(res);
                }
            }
        }
        return result;
    }

    public void executeAsync(RequestI request) throws Exception {
        if (request == null) {
            throw new Exception("request is null");
        }

        if (request.getQueryCode() == null) {
            throw new Exception("Query is null");
        }

        synchronized (this.requestURIs) {
            if (this.requestURIs.contains(request.requestURI())) {
                this.logMessage("Request URI: " + request.requestURI() + " already executed");
                return;
            }
            this.requestURIs.add(request.requestURI());
        }

        this.logMessage("received async request, URI: " + request.requestURI());

        AbstractQuery query = (AbstractQuery) request.getQueryCode();
        ExecutionStateI state = new ExecutionStateIMPL(this.processingNode);

        QueryResultI result = query.eval(state);

        if (!state.isContinuationSet()) {
            this.logMessage("No continuation set");
            returnResultToClient(request, result); // return the result to the client
        } else if (state.isFlooding()) {
            // Propagate the request to neighbours and update the result
            this.floodingPropagationAsync(state, request);
        } else if (state.isDirectional()) {
            // Can't propagate if no more hops
            // Return local result
            if (((ExecutionStateIMPL) state).noMoreHops()) {
                this.logMessage("No more hops");
                returnResultToClient(request, result); // return the result to the client
                return;
            }
            // Propagate the request to neighbours and update the result
            this.directionalPropagationAsync(state, request);
        }

        // every node will return the result to the client after executing the request
        // and propagating it to the neighbours
        returnResultToClient(request, result); // return the result to the client
    }

    private synchronized void returnResultToClient(RequestI request, QueryResultI result) throws Exception {
        this.getOwner().doPortConnection(this.requestResultOutboundPort.getClientPortURI(),
                ((BCM4JavaEndPointDescriptorI) request.clientConnectionInfo().endPointInfo()).getInboundPortURI(),
                ClientRequestResult.class.getCanonicalName());
        synchronized (this.requestURIs) {
            this.logMessage("Sending result to client URI: " + requestURIs);
        }
        this.requestResultOutboundPort.acceptRequestResult(request.requestURI(), result);
        this.getOwner().doPortDisconnection(this.requestResultOutboundPort.getClientPortURI());
    }

    private void directionalPropagationAsync(ExecutionStateI state, RequestI request)
            throws Exception {
        Iterator<NodeInfoI> it = new HashSet<>(neighbours).iterator();
        while (it.hasNext()) {
            NodeInfoI neighbour = it.next();
            if (state.getDirections()
                    .contains(processingNode.getPosition().directionFrom(neighbour.nodePosition()))) {
                SensorNodeP2POutboundPort nodePort = this.nodeInfoToP2POutboundPortMap.get(neighbour);
                if (nodePort != null) {
                    ExecutionStateI newState = new ExecutionStateIMPL(state);
                    newState.incrementHops();
                    RequestContinuationI continuation = new RequestContinuationIMPL(
                            request.requestURI(), request.getQueryCode(), request.isAsynchronous(),
                            request.clientConnectionInfo(), newState);
                    nodePort.executeAsync(continuation);
                }
            }
        }
    }

    private void floodingPropagationAsync(ExecutionStateI state, RequestI request)
            throws Exception {
        Iterator<NodeInfoI> it = new HashSet<>(neighbours).iterator();
        while (it.hasNext()) {
            NodeInfoI neighbour = it.next();
            this.logMessage("propagation " + neighbour.nodeIdentifier());
            if (state.withinMaximalDistance(neighbour.nodePosition())) {
                SensorNodeP2POutboundPort nodePort = this.nodeInfoToP2POutboundPortMap.get(neighbour);
                if (nodePort != null) {
                    double newMaximalDistance = ((ExecutionStateIMPL) state).getMaxDistance()
                            - processingNode.getPosition().distance(neighbour.nodePosition());
                    ExecutionStateIMPL newState = new ExecutionStateIMPL(state);
                    newState.updateMaxDistance(newMaximalDistance);

                    RequestContinuationI continuation = new RequestContinuationIMPL(
                            request.requestURI(), request.getQueryCode(), request.isAsynchronous(),
                            request.clientConnectionInfo(), newState);
                    nodePort.executeAsync(continuation);
                }
            }
        }
    }

    public void executeAsync(RequestContinuationI requestContinuation) throws Exception {
        if (requestContinuation == null) {
            throw new Exception("request is null");
        }

        if (requestContinuation.getQueryCode() == null) {
            throw new Exception("Query is null");
        }

        synchronized (this.requestURIs) {
            if (this.requestURIs.contains(requestContinuation.requestURI())) {
                this.logMessage("Request URI: " + requestContinuation.requestURI() + " already executed");
                return;
            }
            this.requestURIs.add(requestContinuation.requestURI());
        }

        this.logMessage("received async request, URI: " + requestContinuation.requestURI() + " Time: " + Instant.now());

        AbstractQuery query = (AbstractQuery) requestContinuation.getQueryCode();
        ExecutionStateI state = ((RequestContinuationIMPL) requestContinuation).getExecutionState();
        QueryResultI result;
        ((ExecutionStateIMPL) state).updateProcessingNode(this.processingNode);
        result = query.eval(state);
        if (state.isFlooding()) {
            // Propagate the request to neighbours and update the result
            this.floodingPropagationAsync(state, requestContinuation);
        } else if (state.isDirectional()) {
            // Can't propagate if no more hops
            // Return local result
            if (((ExecutionStateIMPL) state).noMoreHops()) {
                this.logMessage("No more hops");
                returnResultToClient(requestContinuation, result); // return the result to the client if no more hops
                                                                   // and return
                return;
            }
            // Propagate the request to neighbours and update the result
            this.directionalPropagationAsync(state, requestContinuation);
        }

        // every node will return the result to the client after executing the request
        // and propagating it to the neighbours
        returnResultToClient(requestContinuation, result); // return the result to the client
    }

}
