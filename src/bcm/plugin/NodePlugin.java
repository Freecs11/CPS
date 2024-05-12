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
import fr.sorbonne_u.components.AbstractPort;
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

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>NodePlugin</code> represents the plugin that allows the node
 * component to
 * register itself to the registry component, connect to its neighbours, execute
 * a request, and
 * execute a request asynchronously.
 * </p>
 * 
 */
public class NodePlugin
        extends AbstractPlugin {
    private static final long serialVersionUID = 1L;
    // Data structures of the node component
    // Set of neighbours of the node
    private Set<NodeInfoI> neighbours;
    // Thread safe map of node information to the outbound port of the neighbour
    protected ConcurrentHashMap<NodeInfoI, SensorNodeP2POutboundPort> nodeInfoToP2POutboundPortMap;
    private ProcessingNodeI processingNode;
    // URIs of the requests executed by the node
    private Set<String> requestURIs;
    // Node information of the node
    private NodeInfoI nodeInfo;
    // Sensor data of the node
    private ArrayList<SensorDataI> sensorData;
    // Inbound port of the requesting service
    protected RequestingInboundPort requestingInboundPort;
    // Inbound port for the p2p services
    protected SensorNodeP2PInboundPort sensorNodeP2PInboundPort;
    // URI of the registry inbound port
    protected String registerInboundPortURI;
    // Locks for the thread safe data structures (the sets)
    private final ReadWriteLock neigboursLock = new ReentrantReadWriteLock();

    /**
     * Constructor of the NodePlugin
     * 
     * @param registerInboundPortURI the URI of the registry inbound port
     * @param nodeInfo               the node information
     * @param sensorData             the sensor data
     * @throws Exception
     */
    public NodePlugin(String registerInboundPortURI, NodeInfoI nodeInfo,
            ArrayList<SensorDataI> sensorData)
            throws Exception {
        // port URI for the registry
        this.registerInboundPortURI = registerInboundPortURI;
        // initialisation des listes
        this.requestURIs = new HashSet<>();
        this.neighbours = new HashSet<>();
        this.nodeInfoToP2POutboundPortMap = new ConcurrentHashMap<>();
        // init of the node info and sensor data
        this.nodeInfo = nodeInfo;
        this.sensorData = sensorData;
    }

    /**
     * See {@link fr.sorbonne_u.components.AbstractPlugin#installOn(ComponentI)}
     */
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

    /**
     * See {@link fr.sorbonne_u.components.AbstractPlugin#initialise()}
     */
    @Override
    public void initialise() throws Exception {
        // Init of the inbound ports
        this.requestingInboundPort = new RequestingInboundPort(this.getOwner());
        this.requestingInboundPort.publishPort();
        this.sensorNodeP2PInboundPort = new SensorNodeP2PInboundPort(this.getOwner());
        this.sensorNodeP2PInboundPort.publishPort();
        // init of the node connection information
        EndPointDescriptorI p2pendpoint = new EndPointDescIMPL(this.sensorNodeP2PInboundPort.getPortURI(),
                SensorNodeP2PCI.class);
        EndPointDescriptorI endpoint = new EndPointDescIMPL(this.requestingInboundPort.getPortURI(),
                RequestingCI.class);
        // set the node information
        ((NodeInfoIMPL) this.nodeInfo).setP2pEndPointInfo(p2pendpoint);
        ((NodeInfoIMPL) this.nodeInfo).setEndPointInfo(endpoint);
        this.processingNode = new ProcessingNodeIMPL(nodeInfo.nodePosition(), null,
                nodeInfo.nodeIdentifier());
        ((ProcessingNodeIMPL) (this.processingNode)).addAllSensorData(sensorData);
        this.neighbours = new HashSet<>();
        this.requestURIs = new HashSet<>();
        this.nodeInfoToP2POutboundPortMap = new ConcurrentHashMap<>();
        super.initialise();
    }

    /**
     * See {@link fr.sorbonne_u.components.AbstractPlugin#finalise()}
     */
    @Override
    public void finalise() throws Exception {
        for (SensorNodeP2POutboundPort p2poutboundPort : this.nodeInfoToP2POutboundPortMap.values()) {
            if (p2poutboundPort.connected()) {
                this.getOwner().doPortDisconnection(p2poutboundPort.getPortURI());
            }
            if (p2poutboundPort.isPublished())
                p2poutboundPort.unpublishPort();
        }
        super.finalise();
    }

    /**
     * See {@link fr.sorbonne_u.components.AbstractPlugin#uninstall()}
     */
    @Override
    public void uninstall() throws Exception {
        if (requestingInboundPort.isPublished())
            this.requestingInboundPort.unpublishPort();
        if (sensorNodeP2PInboundPort.isPublished())
            this.sensorNodeP2PInboundPort.unpublishPort();
        super.uninstall();
    }

    // ----------------- PLUGIN METHODS -----------------
    /**
     * Method to ask for connection to a neighbour node
     * in the case where there is already a neighbour in the same direction
     * the method compares the distance between the new neighbour and the neighbour
     * in the same direction
     * if the distance between the new neighbour and the neighbour in the same
     * direction is less than the distance between the new neighbour and the
     * neighbour in the same direction
     * the new neighbour is connected and the neighbour in the same direction is
     * disconnected
     * 
     * @param newNeighbour
     * @throws Exception
     */
    public void ask4Connection(NodeInfoI newNeighbour)
            throws Exception {
        try {
            Direction direction = this.nodeInfo.nodePosition().directionFrom(newNeighbour.nodePosition()); // direction
            NodeInfoI neighbourInTheDirection = null;
            // Copy to avoid ConcurrentModificationException
            Iterator<NodeInfoI> it = new HashSet<>(neighbours).iterator();
            while (it.hasNext()) {
                NodeInfoI neighbour = it.next();
                if (this.nodeInfo.nodePosition().directionFrom(neighbour.nodePosition()) == direction) {
                    neighbourInTheDirection = neighbour;
                    break;
                }
            }

            System.err.println("Neighbour in the direction: " + neighbourInTheDirection);
            // if there is no neighbour in the direction simply connect
            if (neighbourInTheDirection == null) {
                SensorNodeP2POutboundPort newPort = new SensorNodeP2POutboundPort(
                        AbstractOutboundPort.generatePortURI(), this.getOwner());
                newPort.publishPort();
                this.getOwner().doPortConnection(newPort.getPortURI(),
                        ((BCM4JavaEndPointDescriptorI) newNeighbour.p2pEndPointInfo()).getInboundPortURI(),
                        SensorNodeConnector.class.getCanonicalName());
                newPort.ask4Connection(this.nodeInfo);

                this.addNeighbour(newNeighbour, newPort);

                this.logMessage("ask4Connection: " + newNeighbour.nodeIdentifier() + " connected");
            } else {
                // ------- compare the distance between the new neighbour and the neighbour in
                // the same direction
                double distanceNewNeighbour = this.nodeInfo.nodePosition().distance(newNeighbour.nodePosition());
                double distanceNeighbourInTheDirection = this.nodeInfo.nodePosition()
                        .distance(neighbourInTheDirection.nodePosition());
                // if there is a neighbour in the direction that is closer to the new neighbour
                // connect the new neighbour and disconnect the neighbour in the same direction
                if (distanceNewNeighbour < distanceNeighbourInTheDirection) {
                    SensorNodeP2POutboundPort newPort = new SensorNodeP2POutboundPort(
                            AbstractOutboundPort.generatePortURI(), this.getOwner());
                    newPort.publishPort();
                    this.getOwner().doPortConnection(newPort.getPortURI(),
                            ((BCM4JavaEndPointDescriptorI) newNeighbour.p2pEndPointInfo()).getInboundPortURI(),
                            SensorNodeConnector.class.getCanonicalName());
                    newPort.ask4Connection(this.nodeInfo);

                    this.addNeighbour(newNeighbour, newPort);

                    this.logMessage("ask4Connection: " + newNeighbour.nodeIdentifier() + " connected");
                    this.logMessage("Disconnecting neighbour in the same direction: "
                            + neighbourInTheDirection.nodeIdentifier());
                    this.ask4Disconnection(neighbourInTheDirection);
                    this.logMessage(
                            "Neighbours after disconnection: -->;" + neighbourInTheDirection.nodeIdentifier()
                                    + " disconnected");
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

    /**
     * Method to ask for disconnection to a neighbour node
     * after disconnecting the port of the neighbour node
     * the method finds a new neighbour in the same direction if possible
     * by user the service findNewNeighbour from the registry component
     * 
     * @param neighbour
     */
    public void ask4Disconnection(NodeInfoI neighbour) {
        try {

            RegistrationOutboundPort registrationOutboundPort = new RegistrationOutboundPort(this.getOwner());
            registrationOutboundPort.publishPort();
            this.getOwner().doPortConnection(registrationOutboundPort.getPortURI(), this.registerInboundPortURI,
                    RegistryConnector.class.getCanonicalName());

            SensorNodeP2POutboundPort nodePort = this.nodeInfoToP2POutboundPortMap.get(neighbour);
            if (nodePort == null) {
                this.logMessage("ask4Disconnection: " + neighbour.nodeIdentifier() + " not connected");
                return;
            }
            // Find the direction of the neighbour and remove it from the list of neighbours
            Direction directionOfNeighbour = this.nodeInfo.nodePosition().directionFrom(neighbour.nodePosition());

            this.removeNeighbour(neighbour);
            this.getOwner().doPortDisconnection(nodePort.getPortURI());
            nodePort.unpublishPort();
            nodePort.destroyPort();

            // ----- Find new in the same direction if possible -----
            NodeInfoI newNeighbour = registrationOutboundPort.findNewNeighbour(this.nodeInfo,
                    directionOfNeighbour);
            if (newNeighbour != null && newNeighbour.nodeIdentifier() != neighbour.nodeIdentifier()) {
                SensorNodeP2POutboundPort newPort = new SensorNodeP2POutboundPort(
                        AbstractOutboundPort.generatePortURI(), this.getOwner());
                newPort.publishPort();
                this.getOwner().doPortConnection(newPort.getPortURI(),
                        ((BCM4JavaEndPointDescriptorI) newNeighbour.p2pEndPointInfo()).getInboundPortURI(),
                        SensorNodeConnector.class.getCanonicalName());
                newPort.ask4Connection(this.nodeInfo);

                this.addNeighbour(newNeighbour, newPort);

                this.logMessage("ask4Disconnection: " + neighbour.nodeIdentifier() + " disconnected");
                this.logMessage("Found new neighbour in direction " + directionOfNeighbour + " : "
                        + newNeighbour.nodeIdentifier());
            } else {
                this.logMessage("ask4Disconnection: " + neighbour.nodeIdentifier() + " disconnected");
                this.logMessage("No new neighbour found in direction " + directionOfNeighbour);
            }
            this.getOwner().doPortDisconnection(registrationOutboundPort.getPortURI());
            registrationOutboundPort.unpublishPort();
            registrationOutboundPort.destroyPort();

        } catch (Exception e) {
            System.err.println("Error in ask4Disconnection " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Method to add a neighbour to the list of neighbours
     * Thread safe method to add a neighbour to the set of neighbours
     * it also adds the neighbour to the map of nodeInfo to P2POutboundPort
     * 
     * @param neighbour    the neighbour to add
     * @param p2poutboundP the outbound port of the neighbour
     */
    public void addNeighbour(NodeInfoI neighbour, SensorNodeP2POutboundPort p2poutboundP) {
        neigboursLock.writeLock().lock();
        try {
            neighbours.add(neighbour);
            nodeInfoToP2POutboundPortMap.put(neighbour, p2poutboundP);
        } finally {
            neigboursLock.writeLock().unlock();
        }
    }

    /**
     * Method to remove a neighbour from the list of neighbours
     * Thread safe method to remove a neighbour from the set of neighbours
     * it also removes the neighbour from the map of nodeInfo to P2POutboundPort
     * 
     * @param neighbour the neighbour to remove
     */
    public void removeNeighbour(NodeInfoI neighbour) {
        neigboursLock.writeLock().lock();
        try {
            neighbours.remove(neighbour);
            nodeInfoToP2POutboundPortMap.remove(neighbour);
        } finally {
            neigboursLock.writeLock().unlock();
        }
    }

    /**
     * Helper function to do the port connection to the neighbours
     * from the set of neighbours received from the registry
     * 
     * @throws ComponentStartException
     */
    private void connect2Neighbours() throws ComponentStartException {
        try {
            Iterator<NodeInfoI> it = new HashSet<>(neighbours).iterator();
            // Copy to avoid
            // ConcurrentModificationException
            while (it.hasNext()) {
                NodeInfoI neighbour = it.next();
                SensorNodeP2POutboundPort p2poutboundP = new SensorNodeP2POutboundPort(
                        AbstractPort.generatePortURI(),
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

    /**
     * Method to register the node to the registry component
     * by using the service register from the registry component
     * the method also connects to the neighbours received from the registry
     * 
     * @throws Exception
     */
    public void registerNode() throws Exception {
        try {
            RegistrationOutboundPort registrationOutboundPort = new RegistrationOutboundPort(this.getOwner());
            registrationOutboundPort.publishPort();
            this.getOwner().doPortConnection(registrationOutboundPort.getPortURI(), this.registerInboundPortURI,
                    RegistryConnector.class.getCanonicalName());
            // ----------------- REGISTRATION -----------------
            this.neighbours = registrationOutboundPort.register(nodeInfo);
            ((ProcessingNodeIMPL) this.processingNode).setNeighbors(neighbours);
            this.getOwner().logMessage("Registration Success: "
                    + registrationOutboundPort.registered(nodeInfo.nodeIdentifier()) + "");
            this.getOwner().logMessage("Connecting to all the neighbours received from the registry at Time : "
                    + Instant.now() + " .....................");
            this.connect2Neighbours();
            this.getOwner().doPortDisconnection(registrationOutboundPort.getPortURI());
            registrationOutboundPort.unpublishPort();
            registrationOutboundPort.destroyPort();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.getOwner().logMessage("Node Component successfully executed: " +
                this.nodeInfo.nodeIdentifier());
    }

    /**
     * Query execution service offered by the node component
     * The method executes the query and propagates the request to the neighbours
     * if the query is a flooding query or a directional query
     * 
     * @param request the query to execute
     * @return result of the query
     * @throws Exception
     */
    public QueryResultI execute(RequestContinuationI request) throws Exception {
        this.logMessage("Executing RequestContinuationI: " + this.nodeInfo.nodeIdentifier());
        this.logMessage("-----------------------------------------------------------------------------\n");

        if (request == null) {
            throw new Exception("request is null");
        }

        if (request.getQueryCode() == null) {
            throw new Exception("Query is null");
        }
        // Synchronize the request URIs to avoid concurrent modification
        synchronized (this.requestURIs) {
            if (this.requestURIs.contains(request.requestURI())) {
                this.logMessage("Request URI: " + request.requestURI() + " already executed");
                return new QueryResultIMPL();
            }
            this.requestURIs.add(request.requestURI());
        }
        this.requestURIs.add(request.requestURI());
        // Get the state of the request
        ExecutionStateI state = ((RequestContinuationIMPL) request).getExecutionState();
        if (state == null) {
            throw new Exception("State is null");
        }
        System.err.println("executing RequestContinuationI: " + request.toString());
        // Update the processing node
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

        return result;
    }

    /**
     * Query execution service offered by the node component
     * The method executes the query and propagates the request to the neighbours
     * 
     * @param request the query to execute
     * @return result of the query
     * @throws Exception
     */
    public QueryResultI execute(RequestI request) throws Exception {
        this.logMessage("Executing Request: " + this.nodeInfo.nodeIdentifier());
        this.logMessage("--------------------------------------------------------------------------------\n");
        if (request == null) {
            throw new Exception("request is null");
        }

        if (request.getQueryCode() == null) {
            throw new Exception("Query is null");
        }
        // Synchronize the request URIs to avoid concurrent modification
        synchronized (this.requestURIs) {
            if (this.requestURIs.contains(request.requestURI())) {
                this.logMessage("Request URI: " + request.requestURI() + " already executed");
                return new QueryResultIMPL();
            }
            this.requestURIs.add(request.requestURI());
        }
        // Init a new a state and execute the query
        AbstractQuery query = (AbstractQuery) request.getQueryCode();
        ExecutionStateI state = new ExecutionStateIMPL(this.processingNode);
        QueryResultI result = query.eval(state);
        // Propagate the request to neighbours if there is a continuation
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

    /**
     * Helper function to propagate the request to the neighbours
     * in the case of a directional query
     * 
     * @param state   the execution state
     * @param request the query
     * @param result  the result of the query
     * @return the updated result
     * @throws Exception
     */
    private QueryResultI directionalPropagation(ExecutionStateI state, RequestI request, QueryResultI result)
            throws Exception {
        Iterator<NodeInfoI> it = new HashSet<>(neighbours).iterator();
        // Copy to avoid ConcurrentModificationException
        while (it.hasNext()) {
            NodeInfoI neighbour = it.next();
            this.logMessage("propagation " + neighbour.nodeIdentifier());
            // if the neighbour is in the direction of the node
            if (state.getDirections()
                    .contains(processingNode.getPosition().directionFrom(neighbour.nodePosition()))) {
                // Get the outbound port of the neighbour
                SensorNodeP2POutboundPort nodePort = this.nodeInfoToP2POutboundPortMap.get(neighbour);
                this.logMessage("successfully\n");
                if (nodePort != null) {
                    // Create a new state and increment the number of hops
                    ExecutionStateI newState = new ExecutionStateIMPL(state);
                    newState.incrementHops();
                    RequestContinuationI continuation = new RequestContinuationIMPL(
                            request.requestURI(), request.getQueryCode(), request.isAsynchronous(),
                            request.clientConnectionInfo(), newState);
                    // Propagate the request to the neighbour
                    QueryResultI res = nodePort.execute(continuation);
                    ((QueryResultIMPL) result).update(res);
                }
            }
        }
        return result;
    }

    /**
     * Helper function to propagate the request to the neighbours
     * in the case of a flooding query
     * 
     * @param state   the execution state
     * @param request the query
     * @param result  the result of the query
     * @return the updated result
     * @throws Exception
     */
    private QueryResultI floodingPropagation(ExecutionStateI state, RequestI request, QueryResultI result)
            throws Exception {
        Iterator<NodeInfoI> it = new HashSet<>(neighbours).iterator();
        // Copy to avoid ConcurrentModificationException
        while (it.hasNext()) {
            NodeInfoI neighbour = it.next();
            // Get the outbound port of the neighbour if the neighbour is within the maximal
            // distance
            if (state.withinMaximalDistance(neighbour.nodePosition())) {
                SensorNodeP2POutboundPort nodePort = this.nodeInfoToP2POutboundPortMap.get(neighbour);
                if (nodePort != null) {
                    double newMaximalDistance = ((ExecutionStateIMPL) state).getMaxDistance()
                            - processingNode.getPosition().distance(neighbour.nodePosition());
                    // Create a new state and update the maximal distance
                    ExecutionStateIMPL newState = new ExecutionStateIMPL(state);
                    newState.updateMaxDistance(newMaximalDistance);
                    // Create new continuation
                    RequestContinuationI continuation = new RequestContinuationIMPL(
                            request.requestURI(), request.getQueryCode(), request.isAsynchronous(),
                            request.clientConnectionInfo(), newState);
                    // Propagate the request to the neighbour
                    QueryResultI res = nodePort.execute(continuation);
                    // Update the result
                    ((QueryResultIMPL) result).update(res);
                }
            }
        }
        return result;
    }

    /**
     * Query asynchronous execution service offered by the node component
     * The method executes the query and propagates the request to the neighbours
     * if the query is a flooding query or a directional query
     * 
     * @param request the query to execute
     * @throws Exception
     */
    public void executeAsync(RequestI request) throws Exception {
        if (request == null) {
            throw new Exception("request is null");
        }

        if (request.getQueryCode() == null) {
            throw new Exception("Query is null");
        }
        // Synchronize the request URIs to avoid concurrent modification
        synchronized (this.requestURIs) {
            if (this.requestURIs.contains(request.requestURI())) {
                this.logMessage("Request URI: " + request.requestURI() + " already executed");
                return;
            }
            this.requestURIs.add(request.requestURI());
        }
        this.logMessage("received async request, URI: " + request.requestURI());
        // Init a new a state and execute the query
        AbstractQuery query = (AbstractQuery) request.getQueryCode();
        ExecutionStateI state = new ExecutionStateIMPL(this.processingNode);

        QueryResultI result = query.eval(state);
        // Propagate the request to neighbours if there is a continuation
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

    /**
     * Method to send the result to the client when the request is executed
     * asynchronously
     * 
     * @param request the query
     * @param result  the result of the query
     * @throws Exception
     */
    private void returnResultToClient(RequestI request, QueryResultI result) throws Exception {
        RequestResultOutboundPort requestResultOutboundPort = new RequestResultOutboundPort(this.getOwner());
        requestResultOutboundPort.publishPort();
        this.getOwner().doPortConnection(requestResultOutboundPort.getClientPortURI(),
                ((BCM4JavaEndPointDescriptorI) request.clientConnectionInfo().endPointInfo()).getInboundPortURI(),
                ClientRequestResult.class.getCanonicalName());
        requestResultOutboundPort.acceptRequestResult(request.requestURI(), result);
        this.getOwner().doPortDisconnection(requestResultOutboundPort.getClientPortURI());
        requestResultOutboundPort.unpublishPort();
        requestResultOutboundPort.destroyPort();
    }

    /**
     * Helper function to propagate the request to the neighbours
     * in the case of a directional query asynchronously
     * 
     * @param state   the execution state
     * @param request the query
     * @throws Exception
     */
    private void directionalPropagationAsync(ExecutionStateI state, RequestI request)
            throws Exception {
        Iterator<NodeInfoI> it = new HashSet<>(neighbours).iterator();
        // Copy to avoid ConcurrentModificationException
        while (it.hasNext()) {
            NodeInfoI neighbour = it.next();
            // if the neighbour is in the direction of the node
            // get the outbound port of the neighbour
            if (state.getDirections()
                    .contains(processingNode.getPosition().directionFrom(neighbour.nodePosition()))) {
                SensorNodeP2POutboundPort nodePort = this.nodeInfoToP2POutboundPortMap.get(neighbour);
                if (nodePort != null) {
                    // Create a new state and increment the number of hops
                    ExecutionStateI newState = new ExecutionStateIMPL(state);
                    newState.incrementHops();
                    RequestContinuationI continuation = new RequestContinuationIMPL(
                            request.requestURI(), request.getQueryCode(), request.isAsynchronous(),
                            request.clientConnectionInfo(), newState);
                    // Propagate the request to the neighbour
                    nodePort.executeAsync(continuation);
                }
            }
        }
    }

    /**
     * Helper function to propagate the request to the neighbours
     * in the case of a flooding query asynchronously
     * 
     * @param state   the execution state
     * @param request the query
     * @throws Exception
     */
    private void floodingPropagationAsync(ExecutionStateI state, RequestI request)
            throws Exception {
        Iterator<NodeInfoI> it = new HashSet<>(neighbours).iterator();
        // Copy to avoid ConcurrentModificationException
        while (it.hasNext()) {
            NodeInfoI neighbour = it.next();
            this.logMessage("propagation " + neighbour.nodeIdentifier());
            // Get the outbound port of the neighbour if the neighbour is within the maximal
            // distance
            if (state.withinMaximalDistance(neighbour.nodePosition())) {
                SensorNodeP2POutboundPort nodePort = this.nodeInfoToP2POutboundPortMap.get(neighbour);
                if (nodePort != null) {
                    // Create a new state and update the maximal distance
                    double newMaximalDistance = ((ExecutionStateIMPL) state).getMaxDistance()
                            - processingNode.getPosition().distance(neighbour.nodePosition());
                    ExecutionStateIMPL newState = new ExecutionStateIMPL(state);
                    newState.updateMaxDistance(newMaximalDistance);
                    // Create new continuation
                    RequestContinuationI continuation = new RequestContinuationIMPL(
                            request.requestURI(), request.getQueryCode(), request.isAsynchronous(),
                            request.clientConnectionInfo(), newState);
                    // Propagate the request to the neighbour
                    nodePort.executeAsync(continuation);
                }
            }
        }
    }

    /**
     * Query asynchronous execution service offered by the node component
     * The method executes the query and propagates the request to the neighbours
     * if the query is a flooding query or a directional query
     * 
     * @param requestContinuation
     * @throws Exception
     */
    public void executeAsync(RequestContinuationI requestContinuation) throws Exception {
        if (requestContinuation == null) {
            throw new Exception("request is null");
        }

        if (requestContinuation.getQueryCode() == null) {
            throw new Exception("Query is null");
        }
        // Synchronize the request URIs to avoid concurrent modification
        synchronized (this.requestURIs) {
            if (this.requestURIs.contains(requestContinuation.requestURI())) {
                this.logMessage("Request URI: " + requestContinuation.requestURI() + " already executed");
                return;
            }
            this.requestURIs.add(requestContinuation.requestURI());
        }

        this.logMessage("received async request, URI: " + requestContinuation.requestURI() + " Time: " + Instant.now());
        // Init a new a state and execute the query
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
