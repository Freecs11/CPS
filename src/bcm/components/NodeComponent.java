package bcm.components;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import bcm.CVM;
import bcm.connectors.ClientRequestResult;
import bcm.connectors.RegistryConnector;
import bcm.connectors.SensorNodeConnector;
import bcm.ports.RequestingInboundPort;
import bcm.ports.RegistrationOutboundPort;
import bcm.ports.RequestResultInboundPort;
import bcm.ports.RequestResultOutboundPort;
import bcm.ports.SensorNodeP2PInboundPort;
import bcm.ports.SensorNodeP2POutboundPort;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.BCM4JavaEndPointDescriptorI;
import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.EndPointDescriptorI;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestResultCI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PImplI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingImplI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.Pair;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import fr.sorbonne_u.utils.aclocks.ClocksServerCI;
import fr.sorbonne_u.utils.aclocks.ClocksServerConnector;
import fr.sorbonne_u.utils.aclocks.ClocksServerOutboundPort;
import implementation.ConnectionInfoImpl;
import implementation.EndPointDescIMPL;
import implementation.NodeInfoIMPL;
import implementation.PositionIMPL;
import implementation.QueryResultIMPL;
import implementation.RequestContinuationIMPL;
import implementation.SensorDataIMPL;
import implementation.request.ExecutionStateIMPL;
import implementation.request.ProcessingNodeIMPL;
import query.abstraction.AbstractQuery;

@OfferedInterfaces(offered = { RequestingCI.class, SensorNodeP2PCI.class, RequestResultCI.class })
@RequiredInterfaces(required = { RegistrationCI.class, SensorNodeP2PCI.class, ClocksServerCI.class,
        RequestResultCI.class })
public class NodeComponent extends AbstractComponent
        implements RequestingImplI, SensorNodeP2PImplI {

    protected Set<NodeInfoI> neighbours;
    private ProcessingNodeI processingNode;
    private ArrayList<SensorDataI> sensorData;
    // Uris des requetes deja executees
    protected final Set<String> requestURIs;

    protected final NodeInfoI nodeInfo;

    // -------------------- Les PORTS -------------------------
    protected final RequestingInboundPort requestingInboundPort;
    protected final RequestResultInboundPort requestResultInboundPort;
    protected final SensorNodeP2PInboundPort sensorNodeP2PInboundPort;
    protected final RegistrationOutboundPort RegistrationOutboundPort;
    protected final RequestResultOutboundPort requestResultOutboundPort;

    // -------------------- HashMap pour garder les ports des voisins
    // -------------------------
    protected final HashMap<NodeInfoI, SensorNodeP2POutboundPort> nodeInfoToP2POutboundPortMap;

    // -------------------- L'HORLOGE -------------------------
    protected AcceleratedClock clock;
    protected Instant startInstant;

    // -------------------- URI du registre -------------------------
    protected String registerInboundPortURI;
    protected String nodeURI;

    protected NodeComponent(String uri,
            String nodeId,
            Double x, Double y,
            Double range,
            String registryInboundPortURI,
            ArrayList<SensorDataI> sensorData,
            Instant startInstant) throws Exception {
        // only one thread to ensure the serialised execution of services
        // inside the component.
        super(uri, 1, 1);

        assert uri != null : new PreconditionException("uri can't be null!");
        assert registryInboundPortURI != null : new PreconditionException("registryInboundPortURI can't be null!");
        assert nodeId != null : new PreconditionException("nodeId can't be null!");
        assert x != null : new PreconditionException("Position@x can't be null!");
        assert y != null : new PreconditionException("Position@y can't be null!");
        assert range != null : new PreconditionException("range can't be null!");
        assert sensorData != null : new PreconditionException("ArrayList of sensorData can't be null!");
        assert startInstant != null : new PreconditionException("startInstant can't be null!");

        // port URI for the registry
        this.registerInboundPortURI = registryInboundPortURI;
        this.nodeURI = uri;
        // initialisation des listes
        this.requestURIs = new HashSet<>();
        this.neighbours = new HashSet<>();
        this.sensorData = sensorData;
        this.nodeInfoToP2POutboundPortMap = new HashMap<>();

        // initialisation des ports
        this.requestingInboundPort = new RequestingInboundPort(AbstractInboundPort.generatePortURI(), this);
        this.sensorNodeP2PInboundPort = new SensorNodeP2PInboundPort(AbstractInboundPort.generatePortURI(), this);
        this.requestResultInboundPort = new RequestResultInboundPort(AbstractInboundPort.generatePortURI(), this);
        this.RegistrationOutboundPort = new RegistrationOutboundPort(AbstractOutboundPort.generatePortURI(), this);
        this.requestResultOutboundPort = new RequestResultOutboundPort(
                AbstractOutboundPort.generatePortURI(), this);

        // publication des ports
        this.requestingInboundPort.publishPort();
        this.sensorNodeP2PInboundPort.publishPort();
        this.requestResultInboundPort.publishPort();
        this.RegistrationOutboundPort.publishPort();
        this.requestResultOutboundPort.publishPort();

        // initialisation d'endpoint et p2pendpoint
        EndPointDescriptorI p2pendpoint = new EndPointDescIMPL(this.sensorNodeP2PInboundPort.getPortURI(),
                SensorNodeP2PCI.class);
        EndPointDescriptorI endpoint = new EndPointDescIMPL(this.requestingInboundPort.getPortURI(),
                RequestingCI.class);

        // initialisation de l'objet nodeInfo
        this.nodeInfo = new NodeInfoIMPL(nodeId, new PositionIMPL(x, y), endpoint, p2pendpoint, range);

        // initialisation de l'objet processingNode
        this.processingNode = new ProcessingNodeIMPL(this.nodeInfo.nodePosition(), null,
                nodeId);

        // ajout des sensorData
        ((ProcessingNodeIMPL) this.processingNode).addAllSensorData(this.sensorData);

        // initialisation de l'horloge
        this.startInstant = startInstant;

        AbstractComponent.checkImplementationInvariant(this);
        AbstractComponent.checkInvariant(this);
    }

    // Constructeur avec auto-génération de l'URI
    protected NodeComponent(
            String nodeId,
            Double x, Double y,
            Double range,
            String registryInboundPortURI,
            ArrayList<SensorDataI> sensorData,
            Instant startInstant) throws Exception {
        // only one thread to ensure the serialised execution of services
        // inside the component.
        super(AbstractInboundPort.generatePortURI(), 1, 1);
        assert this.reflectionInboundPortURI != null : new PreconditionException("generated uri can't be null!");
        assert registryInboundPortURI != null : new PreconditionException("registryInboundPortURI can't be null!");
        assert nodeId != null : new PreconditionException("nodeId can't be null!");
        assert x != null : new PreconditionException("Position@x can't be null!");
        assert y != null : new PreconditionException("Position@y can't be null!");
        assert range != null : new PreconditionException("range can't be null!");
        assert sensorData != null : new PreconditionException("ArrayList of sensorData can't be null!");
        assert startInstant != null : new PreconditionException("startInstant can't be null!");
        // port URI for the registry
        this.registerInboundPortURI = registryInboundPortURI;

        // initialisation des listes
        this.requestURIs = new HashSet<>();
        this.neighbours = new HashSet<>();
        this.sensorData = sensorData;
        this.nodeInfoToP2POutboundPortMap = new HashMap<>();

        // initialisation des ports
        this.requestingInboundPort = new RequestingInboundPort(AbstractInboundPort.generatePortURI(), this);
        this.sensorNodeP2PInboundPort = new SensorNodeP2PInboundPort(AbstractInboundPort.generatePortURI(), this);
        this.requestResultInboundPort = new RequestResultInboundPort(AbstractInboundPort.generatePortURI(), this);
        this.RegistrationOutboundPort = new RegistrationOutboundPort(AbstractOutboundPort.generatePortURI(), this);
        this.requestResultOutboundPort = new RequestResultOutboundPort(
                AbstractOutboundPort.generatePortURI(), this);

        // publication des ports
        this.requestingInboundPort.publishPort();
        this.sensorNodeP2PInboundPort.publishPort();
        this.requestResultInboundPort.publishPort();
        this.RegistrationOutboundPort.publishPort();
        this.requestResultOutboundPort.publishPort();

        // initialisation d'endpoint et p2pendpoint
        EndPointDescriptorI p2pendpoint = new EndPointDescIMPL(this.sensorNodeP2PInboundPort.getPortURI(),
                SensorNodeP2PCI.class);
        EndPointDescriptorI endpoint = new EndPointDescIMPL(this.requestingInboundPort.getPortURI(),
                RequestingCI.class);

        // initialisation de l'objet nodeInfo
        this.nodeInfo = new NodeInfoIMPL(nodeId, new PositionIMPL(x, y), endpoint, p2pendpoint, range);

        // initialisation de l'objet processingNode
        this.processingNode = new ProcessingNodeIMPL(this.nodeInfo.nodePosition(), null,
                this.nodeInfo.nodeIdentifier());

        // ajout des sensorData
        ((ProcessingNodeIMPL) this.processingNode).addAllSensorData(sensorData);

        // initialisation de l'horloge
        this.startInstant = startInstant;

        AbstractComponent.checkImplementationInvariant(this);
        AbstractComponent.checkInvariant(this);
    }

    @Override
    public synchronized void start() throws ComponentStartException {

        try {
            this.doPortConnection(
                    this.RegistrationOutboundPort.getPortURI(),
                    this.registerInboundPortURI,
                    RegistryConnector.class.getCanonicalName());

            this.logMessage("Starting " + nodeInfo.nodeIdentifier());

        } catch (Exception e) {
            throw new ComponentStartException(e);
        }
        this.logMessage("Node Component successfully started: " + this.nodeInfo.nodeIdentifier());

        System.err
                .println(" Sensor Nodes : " + ((ProcessingNodeIMPL) this.processingNode).getSensorDataMap().toString());

        super.start();
    }

    @Override
    public synchronized void execute() throws Exception {
        // ------CONNECTION TO THE CLOCK SERVER------

        ClocksServerOutboundPort clockPort = new ClocksServerOutboundPort(
                AbstractOutboundPort.generatePortURI(), this);
        clockPort.publishPort();
        this.doPortConnection(
                clockPort.getPortURI(),
                ClocksServer.STANDARD_INBOUNDPORT_URI,
                ClocksServerConnector.class.getCanonicalName());
        this.clock = clockPort.getClock(CVM.CLOCK_URI);
        this.doPortDisconnection(clockPort.getPortURI());
        clockPort.unpublishPort();
        clockPort.destroyPort();

        // ----------------- DELAYED STARTUP -----------------
        this.logMessage("Node component waiting.......");
        long delayTilStart = this.clock.nanoDelayUntilInstant(this.startInstant);
        this.logMessage("Waiting " + delayTilStart + " ns before executing the node component.");
        this.scheduleTask(
                nil -> {
                    try {
                        // ----------------- REGISTRATION -----------------
                        this.neighbours = RegistrationOutboundPort.register(nodeInfo);
                        ((ProcessingNodeIMPL) this.processingNode).setNeighbors(neighbours);
                        this.logMessage(this.printNeighbours());
                        this.logMessage("Registration Success: "
                                + RegistrationOutboundPort.registered(nodeInfo.nodeIdentifier()) + "");
                        this.logMessage("Connecting to all the neighbours received from the registry........");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    this.logMessage("Node Component successfully executed: " + this.nodeInfo.nodeIdentifier());

                }, delayTilStart, TimeUnit.NANOSECONDS);

        // ----------------- CONNECTING TO NEIGHBOURS -----------------
        long delayTilConnect2Neighbours = this.clock.nanoDelayUntilInstant(this.startInstant.plusSeconds(5));
        this.scheduleTask(
                nil -> {
                    try {
                        this.connect2Neighbours();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, delayTilConnect2Neighbours, TimeUnit.NANOSECONDS);
    }

    private void connect2Neighbours() throws ComponentStartException {
        try {
            Iterator<NodeInfoI> it = this.neighbours.iterator();
            while (it.hasNext()) {
                NodeInfoI neighbour = it.next();
                SensorNodeP2POutboundPort p2poutboundP = new SensorNodeP2POutboundPort(
                        AbstractOutboundPort.generatePortURI(),
                        this);
                p2poutboundP.publishPort();
                this.doPortConnection(p2poutboundP.getPortURI(),
                        ((BCM4JavaEndPointDescriptorI) neighbour.p2pEndPointInfo()).getInboundPortURI(),
                        SensorNodeConnector.class.getCanonicalName());
                this.nodeInfoToP2POutboundPortMap.put(neighbour, p2poutboundP);
                System.err.println("Connecting to neighbour: " + neighbour.nodeIdentifier());
                p2poutboundP.ask4Connection(this.nodeInfo);
            }
        } catch (

        Exception e) {
            throw new ComponentStartException(e);
        }
    }

    private String printNeighbours() {
        StringBuilder sb = new StringBuilder();
        sb.append("Neighbours of " + this.nodeInfo.nodeIdentifier() + " : ");
        for (NodeInfoI neighbour : neighbours) {
            sb.append(neighbour.nodeIdentifier() + ", ");
        }
        return sb.toString();
    }

    public void ask4Connection(NodeInfoI newNeighbour)
            throws Exception {
        try {
            Direction direction = this.nodeInfo.nodePosition().directionFrom(newNeighbour.nodePosition()); // direction
            NodeInfoI neighbourInTheDirection = null;
            for (NodeInfoI neighbour : this.neighbours) {
                if (this.nodeInfo.nodePosition().directionFrom(neighbour.nodePosition()) == direction) {
                    neighbourInTheDirection = neighbour;
                }
            }

            System.err.println("Neighbour in the direction: " + neighbourInTheDirection);

            if (neighbourInTheDirection == null) {
                SensorNodeP2POutboundPort newPort = new SensorNodeP2POutboundPort(
                        AbstractOutboundPort.generatePortURI(), this);
                newPort.publishPort();
                this.nodeInfoToP2POutboundPortMap.put(newNeighbour, newPort);
                this.doPortConnection(newPort.getPortURI(),
                        ((BCM4JavaEndPointDescriptorI) newNeighbour.p2pEndPointInfo()).getInboundPortURI(),
                        SensorNodeConnector.class.getCanonicalName());
                newPort.ask4Connection(this.nodeInfo);
                this.neighbours.add(newNeighbour);
                this.logMessage("ask4Connection: " + newNeighbour.nodeIdentifier() + " connected");
                this.printNeighbours();
            } else {
                this.logMessage("ask4Connection: " + newNeighbour.nodeIdentifier() + " already connected");
            }
        } catch (Exception e) {
            throw new Exception("Error in ask4Connection" + e.getMessage());
        }
    }

    public void ask4Disconnection(NodeInfoI neighbour) {
        try {
            SensorNodeP2POutboundPort nodePort = this.nodeInfoToP2POutboundPortMap.get(neighbour);
            Direction directionOfNeighbour = this.nodeInfo.nodePosition().directionFrom(neighbour.nodePosition());
            this.neighbours.remove(neighbour);
            this.nodeInfoToP2POutboundPortMap.remove(neighbour);
            this.doPortDisconnection(nodePort.getPortURI());
            nodePort.unpublishPort();

            // ----- Find new in the same direction if possible -----
            NodeInfoI newNeighbour = this.RegistrationOutboundPort.findNewNeighbour(this.nodeInfo,
                    directionOfNeighbour);
            if (newNeighbour != null && newNeighbour != neighbour) {
                SensorNodeP2POutboundPort newPort = new SensorNodeP2POutboundPort(
                        AbstractOutboundPort.generatePortURI(), this);
                newPort.publishPort();
                this.nodeInfoToP2POutboundPortMap.put(newNeighbour, newPort);
                this.doPortConnection(newPort.getPortURI(),
                        ((BCM4JavaEndPointDescriptorI) newNeighbour.p2pEndPointInfo()).getInboundPortURI(),
                        SensorNodeConnector.class.getCanonicalName());
                newPort.ask4Connection(this.nodeInfo);
                this.neighbours.add(newNeighbour);
                this.logMessage("ask4Disconnection: " + neighbour.nodeIdentifier() + " disconnected");
                this.logMessage("Found new neighbour in direction " + directionOfNeighbour + " : "
                        + newNeighbour.nodeIdentifier());
            } else {
                this.logMessage("ask4Disconnection: " + neighbour.nodeIdentifier() + " disconnected");
                this.logMessage("No new neighbour found in direction " + directionOfNeighbour);
            }

        } catch (Exception e) {
            System.err.println("Error in ask4Disconnection " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void finalise() throws Exception {
        this.logMessage("stopping node component : " + this.nodeInfo.nodeIdentifier());
        // ----- NO CALL TO SERVICES IN FINALISE -----

        if (this.RegistrationOutboundPort.connected()) {
            this.doPortDisconnection(this.RegistrationOutboundPort.getPortURI());
            this.RegistrationOutboundPort.unpublishPort();
        }

        for (SensorNodeP2POutboundPort p2poutboundPort : this.nodeInfoToP2POutboundPortMap.values()) {
            if (p2poutboundPort.connected()) {
                this.doPortDisconnection(p2poutboundPort.getPortURI());
                p2poutboundPort.unpublishPort();
            }
        }
        super.finalise();
        // System.out.println("NodeComponent finalise");
    }

    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        // the shutdown is a good place to unpublish inbound ports.
        try {
            if (this.requestingInboundPort.isPublished())
                this.requestingInboundPort.unpublishPort();
            if (this.sensorNodeP2PInboundPort.isPublished())
                this.sensorNodeP2PInboundPort.unpublishPort();
            if (this.requestResultInboundPort.isPublished())
                this.requestResultInboundPort.unpublishPort();

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
            if (this.requestingInboundPort.isPublished())
                this.requestingInboundPort.unpublishPort();
            if (this.sensorNodeP2PInboundPort.isPublished())
                this.sensorNodeP2PInboundPort.unpublishPort();
            if (this.requestResultInboundPort.isPublished())
                this.requestResultInboundPort.unpublishPort();
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdownNow();
        // System.out.println("NodeComponent shutdownNow");
    }

    @Override
    public QueryResultI execute(RequestContinuationI request) throws Exception {
        if (request == null) {
            throw new Exception("request is null");
        }
        if (this.requestURIs.contains(request.requestURI())) {
            this.logMessage("Request URI: " + request.requestURI() + " already executed");
            return new QueryResultIMPL();
        }
        if (request.getQueryCode() == null) {
            throw new Exception("Query is null");
        }
        this.requestURIs.add(request.requestURI());
        ExecutionStateI state = ((RequestContinuationIMPL) request).getExecutionState();
        if (state == null) {
            throw new Exception("State is null");
        }

        state.updateProcessingNode(this.processingNode);
        AbstractQuery query = (AbstractQuery) request.getQueryCode();
        QueryResultI result = query.eval(state);
        if (state.isFlooding()) {
            // Propagate the request to neighbours and update the result
            this.floodingPropagation(state, request, result);
        } else if (state.isDirectional()) {
            // Can't propagate if no more hops
            // Return local result
            if (((ExecutionStateIMPL) state).noMoreHops()) {
                return result;
            }
            // Propagate the request to neighbours and update the result
            this.directionalPropagation(state, request, result);
        }

        // Return the final result
        return result;
    }

    @Override
    public void executeAsync(RequestContinuationI requestContinuation) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'executeAsync'");
    }

    @Override
    public QueryResultI execute(RequestI request) throws Exception {
        if (request == null) {
            throw new Exception("request is null");
        }
        if (this.requestURIs.contains(request.requestURI())) {
            this.logMessage("Request URI: " + request.requestURI() + " already executed");
            return new QueryResultIMPL();
        }
        if (request.getQueryCode() == null) {
            throw new Exception("Query is null");
        }
        this.requestURIs.add(request.requestURI());

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
            this.directionalPropagation(state, request, result);
        }

        // Return the final result
        return result;
    }

    private void directionalPropagation(ExecutionStateI state, RequestI request, QueryResultI result)
            throws Exception {
        for (NodeInfoI neighbour : neighbours) {
            if (state.getDirections()
                    .contains(processingNode.getPosition().directionFrom(neighbour.nodePosition()))) {
                SensorNodeP2POutboundPort nodePort = this.nodeInfoToP2POutboundPortMap.get(neighbour);
                if (nodePort != null) {
                    ExecutionStateI newState = new ExecutionStateIMPL();
                    ((ExecutionStateIMPL) newState).incrementHops();
                    RequestContinuationI continuation = new RequestContinuationIMPL(
                            request.requestURI(), request.getQueryCode(), request.isAsynchronous(),
                            request.clientConnectionInfo(), newState);
                    QueryResultI res = nodePort.execute(continuation);
                    ((QueryResultIMPL) result).update(res);
                }
            }
        }
    }

    private QueryResultI floodingPropagation(ExecutionStateI state, RequestI request, QueryResultI result)
            throws Exception {
        for (NodeInfoI neighbour : neighbours) {
            if (state.withinMaximalDistance(neighbour.nodePosition())) {
                SensorNodeP2POutboundPort nodePort = this.nodeInfoToP2POutboundPortMap.get(neighbour);
                if (nodePort != null) {
                    // Init new state with updated maximal distance
                    // because we are propagating the request
                    ExecutionStateI newState = new ExecutionStateIMPL();
                    double newMaximalDistance = ((ExecutionStateIMPL) state).getMaxDistance()
                            - processingNode.getPosition().distance(neighbour.nodePosition());
                    ((ExecutionStateIMPL) newState).updateMaxDistance(newMaximalDistance);

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

    @Override
    public void executeAsync(RequestI request) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'executeAsync'");
    }

    // public QueryResultI execute(RequestContinuationI request) throws Exception {
    // if (request == null) {
    // // ((ExecutionStateIMPL) this.context).flush();
    // this.context = new ExecutionStateIMPL();
    // this.context.updateProcessingNode(this.processingNode);
    // throw new Exception("Request is null");
    // }
    // System.err.println("REQUEST URI: " + request.requestURI());
    // System.err.println("REQUEST MAP URIS: " + this.requestURIs.toString());

    // if (this.requestURIs.contains(request.requestURI())) {
    // // ((ExecutionStateIMPL) this.context).flush();
    // this.context = new ExecutionStateIMPL();
    // this.context.updateProcessingNode(this.processingNode);
    // QueryResultI result = new QueryResultIMPL();
    // System.err.println("REQUEST URI: " + request.requestURI() + " already
    // executed");
    // return result;
    // }
    // ExecutionStateI state = ((RequestContinuationIMPL)
    // request).getExecutionState();
    // ProcessingNodeI lastNode = state.getProcessingNode();
    // PositionI lastPosition = lastNode.getPosition();

    // ((ExecutionStateIMPL) state).updateProcessingNode(this.processingNode);
    // // Local execution
    // if (request.getQueryCode() == null) {
    // // ((ExecutionStateIMPL) this.context).flush();
    // this.context = new ExecutionStateIMPL();
    // this.context.updateProcessingNode(this.processingNode);
    // throw new Exception("Query is null");
    // }
    // AbstractQuery query = (AbstractQuery) request.getQueryCode();
    // QueryResultI result = (QueryResultIMPL) query.eval(this.context);
    // // we add the node to the visited nodes in the state to avoid loops
    // // Directional
    // System.err.println("STATE: " + this.context.toString());

    // this.requestURIs.add(request.requestURI());

    // if (state.isDirectional()) {
    // state.incrementHops();
    // if (((ExecutionStateIMPL) state).noMoreHops()) {
    // // ((ExecutionStateIMPL) this.context).flush();
    // this.context = new ExecutionStateIMPL();
    // this.context.updateProcessingNode(this.processingNode);
    // return result;
    // }
    // // New continuation
    // Set<Direction> directions = this.context.getDirections();
    // RequestContinuationI continuation = new RequestContinuationIMPL(request,
    // state);
    // for (Direction direction : directions) {
    // NodeInfoI neighbour =
    // this.RegistrationOutboundPort.findNewNeighbour(nodeInfo, direction);
    // if (neighbour != null) {
    // NodeP2POutboundPort nodePort =
    // this.nodeInfoToP2POutboundPortMap.get(neighbour);
    // if (nodePort != null)
    // ((QueryResultIMPL) result).update(nodePort.execute(continuation));
    // }
    // }
    // }
    // // Flooding
    // else {
    // System.err.println("FLOODING in continuation");
    // if (!this.context.withinMaximalDistance(this.processingNode.getPosition())) {
    // this.context = new ExecutionStateIMPL();
    // this.context.updateProcessingNode(this.processingNode);
    // // ((ExecutionStateIMPL) this.context).flush();
    // return result;
    // }
    // Double currentMaxDist = ((ExecutionStateIMPL) state).getMaxDistance();
    // Double distanceTraveled =
    // this.processingNode.getPosition().distance(lastPosition);
    // ((ExecutionStateIMPL) state).updateMaxDistance(currentMaxDist -
    // distanceTraveled);
    // // New continuation
    // RequestContinuationI continuation = new RequestContinuationIMPL(request,
    // state);
    // for (NodeInfoI neighbour : neighbours) {
    // NodeP2POutboundPort nodePort =
    // this.nodeInfoToP2POutboundPortMap.get(neighbour);
    // if (nodePort != null) {
    // System.err.println("FLOODING in continuation: " +
    // neighbour.nodeIdentifier());

    // QueryResultI execInNeighhbour = nodePort.execute(continuation);

    // ((QueryResultIMPL) result).update(execInNeighhbour);
    // }
    // }
    // }
    // // ((ExecutionStateIMPL) this.context).flush();
    // // Return final result
    // this.context = new ExecutionStateIMPL();
    // this.context.updateProcessingNode(this.processingNode);
    // return result;
    // }

    // public QueryResultI execute(RequestI request) throws Exception {
    // if (requestURIs.contains(request.requestURI())) {
    // // ((ExecutionStateIMPL) this.context).flush();
    // this.context = new ExecutionStateIMPL();
    // this.context.updateProcessingNode(this.processingNode);
    // return new QueryResultIMPL();
    // }
    // if (request == null || request.getQueryCode() == null) {
    // // ((ExecutionStateIMPL) this.context).flush();
    // this.context = new ExecutionStateIMPL();
    // this.context.updateProcessingNode(this.processingNode);
    // throw new Exception("Query is null or request is null");
    // }
    // this.requestURIs.add(request.requestURI());

    // AbstractQuery query = (AbstractQuery) request.getQueryCode();
    // QueryResultI result = (QueryResultIMPL) query.eval(this.context);
    // System.err.println("STATE: " + this.context.toString());

    // // Check if not continuation
    // if (!this.context.isContinuationSet()) {
    // // nouvelle facon de faire le flush
    // this.context = new ExecutionStateIMPL();
    // this.context.updateProcessingNode(this.processingNode);
    // return result;
    // }

    // // Set<Direction> directions = this.context.getDirections();
    // // System.err.println("DIRECTIONS: " + directions.toString());
    // // Flooding
    // if (context.isFlooding()) {
    // System.err.println("FLOODING");
    // RequestContinuationI continuation = new RequestContinuationIMPL(request,
    // this.context);
    // for (NodeInfoI neighbour : neighbours) {
    // NodeP2POutboundPort nodePort =
    // this.nodeInfoToP2POutboundPortMap.get(neighbour);
    // System.err.println("was here ");
    // QueryResultI res = nodePort.execute(continuation);
    // ((QueryResultIMPL) result).update(res);
    // }
    // }
    // // Directional if not flooding
    // else {
    // Set<Direction> directions = this.context.getDirections();
    // System.err.println("DIRECTIONS: " + directions.toString());
    // RequestContinuationI continuation = new RequestContinuationIMPL(request,
    // this.context);
    // for (Direction direction : directions) {
    // NodeInfoI neighbour =
    // this.RegistrationOutboundPort.findNewNeighbour(nodeInfo, direction);
    // System.err.println("NEIGHBOUR: " + neighbour);
    // if (neighbour != null) {
    // NodeP2POutboundPort nodePort =
    // this.nodeInfoToP2POutboundPortMap.get(neighbour);
    // if (nodePort != null) {
    // QueryResultI ress = nodePort.execute(continuation);
    // System.err.println("RESS: " + ress.toString());
    // ((QueryResultIMPL) result).update(ress);
    // }
    // }
    // }
    // }
    // this.context = new ExecutionStateIMPL();
    // this.context.updateProcessingNode(this.processingNode);
    // return result;
    // }

    // public void executeAsync(RequestI request) throws Exception {
    // // local
    // ConnectionInfoImpl clientInfo = (ConnectionInfoImpl)
    // request.clientConnectionInfo();
    // if (clientInfo != null) {
    // System.err.println("CLIENT INFO: " + clientInfo.toString());
    // }

    // if (requestURIs.contains(request.requestURI())) {
    // // ((ExecutionStateIMPL) this.context).flush();
    // return;
    // }

    // if (request == null || request.getQueryCode() == null) {
    // // ((ExecutionStateIMPL) this.context).flush();
    // throw new Exception("Query is null or request is null");
    // }
    // AbstractQuery query = (AbstractQuery) request.getQueryCode();
    // QueryResultI result = (QueryResultIMPL) query.eval(this.context);
    // System.err.println("STATE: " + this.context.toString());

    // // Check if not continuation
    // if (!this.context.isContinuationSet()) {
    // System.err.println("NOT CONTINUATION");
    // // ((ExecutionStateIMPL) this.context).flush();
    // this.requestURIs.add(request.requestURI());
    // this.doPortConnection(this.requestResultOutboundPort.getPortURI(),
    // ((EndPointDescIMPL) clientInfo.endPointInfo()).getURI(),
    // ClientRequestResult.class.getCanonicalName());
    // this.requestResultOutboundPort.acceptRequestResult(request.requestURI(),
    // result);
    // // this.doPortDisconnection(this.requestResultOutboundPort.getPortURI());
    // // ((ExecutionStateIMPL) this.context).flush();
    // return;
    // }
    // // this.context.addToCurrentResult(result);
    // if (context.isFlooding()) {
    // System.err.println("FLOODING");
    // RequestContinuationI continuation = new RequestContinuationIMPL(request,
    // this.context);
    // for (NodeInfoI neighbour : neighbours) {
    // NodeP2POutboundPort nodePort =
    // this.nodeInfoToP2POutboundPortMap.get(neighbour);
    // nodePort.executeAsync(continuation);
    // }

    // // ((ExecutionStateIMPL) this.context).flush();
    // return;
    // }
    // // Directional if not flooding
    // else {
    // Set<Direction> directions = this.context.getDirections();
    // System.err.println("DIRECTIONS: " + directions.toString());
    // RequestContinuationI continuation = new RequestContinuationIMPL(request,
    // this.context);
    // for (Direction direction : directions) {*

    // NodeInfoI neighbour =
    // this.RegistrationOutboundPort.findNewNeighbour(nodeInfo, direction);
    // if (neighbour != null) {
    // NodeP2POutboundPort nodePort =
    // this.nodeInfoToP2POutboundPortMap.get(neighbour);
    // if (nodePort != null) {
    // System.err.println("EXECUTING ASYNC");
    // nodePort.executeAsync(continuation);
    // }
    // }
    // }

    // return;
    // }

    // }

    // public void executeAsync(RequestContinuationI request) throws Exception {
    // if (request == null) {
    // throw new Exception("Request is null");
    // }
    // if (this.requestURIs.contains(request.requestURI())) {
    // // this.context = new
    // return;
    // }
    // ExecutionStateI state = ((RequestContinuationIMPL)
    // request).getExecutionState();
    // ProcessingNodeI lastNode = state.getProcessingNode();
    // PositionI lastPosition = lastNode.getPosition();

    // ((ExecutionStateIMPL) state).updateProcessingNode(this.processingNode);
    // // Local execution
    // if (request.getQueryCode() == null) {
    // // ((ExecutionStateIMPL) this.context).flush();
    // throw new Exception("Query is null");
    // }
    // AbstractQuery query = (AbstractQuery) request.getQueryCode();
    // QueryResultI result = (QueryResultIMPL) query.eval(this.context);
    // // we add the node to the visited nodes in the state to avoid loops
    // // Directional
    // if (state.isDirectional()) {
    // state.incrementHops();
    // if (((ExecutionStateIMPL) state).noMoreHops()) {
    // System.err.println("NO MORE HOPS");
    // this.context.addToCurrentResult(result);
    // this.doPortConnection(this.requestResultOutboundPort.getPortURI(),
    // ((EndPointDescIMPL) request.clientConnectionInfo().endPointInfo()).getURI(),
    // ClientRequestResult.class.getCanonicalName());
    // this.requestResultOutboundPort.acceptRequestResult(request.requestURI(),
    // result);
    // this.doPortDisconnection(this.requestResultOutboundPort.getPortURI());
    // // ((ExecutionStateIMPL) this.context).flush();
    // return;
    // }
    // // New continuation
    // Set<Direction> directions = this.context.getDirections();
    // RequestContinuationI continuation = new RequestContinuationIMPL(request,
    // state);
    // for (Direction direction : directions) {
    // NodeInfoI neighbour =
    // this.RegistrationOutboundPort.findNewNeighbour(nodeInfo, direction);
    // if (neighbour != null) {
    // NodeP2POutboundPort nodePort =
    // this.nodeInfoToP2POutboundPortMap.get(neighbour);
    // if (nodePort != null) {
    // nodePort.executeAsync(continuation);
    // }
    // }
    // }

    // // ((ExecutionStateIMPL) this.context).flush();
    // return;
    // }
    // // Flooding
    // else {
    // if (!this.context.withinMaximalDistance(this.processingNode.getPosition())) {
    // this.context.addToCurrentResult(result);
    // this.doPortConnection(this.requestResultOutboundPort.getPortURI(),
    // ((EndPointDescIMPL) request.clientConnectionInfo().endPointInfo()).getURI(),
    // ClientRequestResult.class.getCanonicalName());
    // this.requestResultOutboundPort.acceptRequestResult(request.requestURI(),
    // result);
    // this.doPortDisconnection(this.requestResultOutboundPort.getPortURI());
    // // ((ExecutionStateIMPL) this.context).flush();
    // return;
    // }
    // Double currentMaxDist = ((ExecutionStateIMPL) state).getMaxDistance();
    // Double distanceTraveled =
    // this.processingNode.getPosition().distance(lastPosition);
    // ((ExecutionStateIMPL) state).updateMaxDistance(currentMaxDist -
    // distanceTraveled);
    // // New continuation
    // RequestContinuationI continuation = new RequestContinuationIMPL(request,
    // state);
    // for (NodeInfoI neighbour : neighbours) {
    // // if (!((ExecutionStateIMPL)
    // // state).getNodesVisited().contains(neighbour.nodeIdentifier())) {
    // NodeP2POutboundPort nodePort =
    // this.nodeInfoToP2POutboundPortMap.get(neighbour);
    // if (nodePort != null)
    // nodePort.executeAsync(continuation);
    // }

    // // ((ExecutionStateIMPL) this.context).flush();
    // return;
    // }

    // }

}
