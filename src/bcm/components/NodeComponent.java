package bcm.components;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import bcm.CVM;
import bcm.connectors.ClientRequestResult;
import bcm.connectors.NodeConnector;
import bcm.connectors.RegistryConnector;
import bcm.connectors.SensorNodeConnector;
import bcm.ports.RegistrationOutboundPort;
import bcm.ports.RequestResultInboundPort;
import bcm.ports.RequestResultOutboundPort;
import bcm.ports.RequestingInboundPort;
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
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.EndPointDescriptorI;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
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
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import fr.sorbonne_u.utils.aclocks.ClocksServerCI;
import fr.sorbonne_u.utils.aclocks.ClocksServerConnector;
import fr.sorbonne_u.utils.aclocks.ClocksServerOutboundPort;
import implementation.EndPointDescIMPL;
import implementation.NodeInfoIMPL;
import implementation.PositionIMPL;
import implementation.QueryResultIMPL;
import implementation.RequestContinuationIMPL;
import implementation.request.ExecutionStateIMPL;
import implementation.request.ProcessingNodeIMPL;
import query.abstraction.AbstractQuery;

@OfferedInterfaces(offered = { RequestingCI.class, SensorNodeP2PCI.class })
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
    // protected final RequestResultInboundPort requestResultInboundPort;
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
        // we put in 15 threads to ensure that the component does not block when doing a
        // task that requires calling other components that then need to call back this
        // component.
        // TODO : do this programmatically, for example when we send a continuation to a
        // neighbour we add a thread to the component ...
        super(uri, 15, 15);

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
        // this.requestResultInboundPort = new
        // RequestResultInboundPort(AbstractInboundPort.generatePortURI(), this);
        this.RegistrationOutboundPort = new RegistrationOutboundPort(AbstractOutboundPort.generatePortURI(), this);
        this.requestResultOutboundPort = new RequestResultOutboundPort(
                AbstractOutboundPort.generatePortURI(), this);

        // publication des ports
        this.requestingInboundPort.publishPort();
        this.sensorNodeP2PInboundPort.publishPort();
        // this.requestResultInboundPort.publishPort();
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
        // we put in 15 threads to ensure that the component does not block when doing a
        // task that requires calling other components that then need to call back this
        // component.
        // TODO : do this programmatically, for example when we send a continuation to a
        // neighbour we add a thread to the component ...
        super(AbstractInboundPort.generatePortURI(), 15, 15);
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
        // this.requestResultInboundPort = new
        // RequestResultInboundPort(AbstractInboundPort.generatePortURI(), this);
        this.RegistrationOutboundPort = new RegistrationOutboundPort(AbstractOutboundPort.generatePortURI(), this);
        this.requestResultOutboundPort = new RequestResultOutboundPort(
                AbstractOutboundPort.generatePortURI(), this);

        // publication des ports
        this.requestingInboundPort.publishPort();
        this.sensorNodeP2PInboundPort.publishPort();
        // this.requestResultInboundPort.publishPort();
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

        // System.err.println(" Sensor Nodes : " + ((ProcessingNodeIMPL)
        // this.processingNode).getSensorDataMap().toString());

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
        // print the current time and the start time
        this.clock.waitUntilStart(); // wait until the start time
        this.logMessage("Node component starting.......");

        long delayRegister = this.clock.nanoDelayUntilInstant(this.startInstant);
        this.scheduleTask(
                o -> {
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

                }, delayRegister, TimeUnit.NANOSECONDS);

        // ----------------- CONNECTING TO NEIGHBOURS -----------------
        long delayTilConnect2Neighbours = this.clock.nanoDelayUntilInstant(this.startInstant.plusSeconds(2L));
        this.scheduleTask(
                o -> {
                    try {
                        this.connect2Neighbours();
                        this.logMessage("Connected to all the neighbours : " + this.printNeighbours());
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
                    break;
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
                // ------- compare the distance between the new neighbour and the neighbour in
                // the same direction
                double distanceNewNeighbour = this.nodeInfo.nodePosition().distance(newNeighbour.nodePosition());
                double distanceNeighbourInTheDirection = this.nodeInfo.nodePosition()
                        .distance(neighbourInTheDirection.nodePosition());
                if (distanceNewNeighbour < distanceNeighbourInTheDirection) {
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
                    this.logMessage("Disconnecting neighbour in the same direction: "
                            + neighbourInTheDirection.nodeIdentifier());
                    this.ask4Disconnection(neighbourInTheDirection);
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
            // if (this.requestResultInboundPort.isPublished())
            // this.requestResultInboundPort.unpublishPort();

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
            // if (this.requestResultInboundPort.isPublished())
            // this.requestResultInboundPort.unpublishPort();
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdownNow();
        // System.out.println("NodeComponent shutdownNow");
    }

    @Override
    public QueryResultI execute(RequestContinuationI request) throws Exception {
        this.logMessage("Neighbours of " + this.nodeInfo.nodeIdentifier());
        for (NodeInfoI neighbour : neighbours) {
            this.logMessage("Neighour : " + neighbour.nodeIdentifier());
        }
        if (request == null) {
            throw new Exception("request is null");
        }
        this.logMessage(request.toString());
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
        System.err.println("executinh RequestContinuationI: " + request.toString());
        state.updateProcessingNode(this.processingNode);
        System.err.println("Processing Node: " + this.processingNode.getNodeIdentifier());

        AbstractQuery query = (AbstractQuery) request.getQueryCode();
        QueryResultI result = query.eval(state);
        if (state.isFlooding()) {
            // Propagate the request to neighbours and update the result
            return this.floodingPropagation(state, request, result);
        } else if (state.isDirectional()) {
            // Can't propagate if no more hops
            // Return local result
            if (((ExecutionStateIMPL) state).noMoreHops()) {
                return result;
            }
            // Propagate the request to neighbours and update the result
            return this.directionalPropagation(state, request, result);
        }

        // Return the final result
        return result;
    }

    @Override
    public QueryResultI execute(RequestI request) throws Exception {
        this.logMessage("Neighbours of " + this.nodeInfo.nodeIdentifier());
        for (NodeInfoI neighbour : neighbours) {
            this.logMessage("Neighour : " + neighbour.nodeIdentifier());
        }

        if (request == null) {
            throw new Exception("request is null");
        }
        if (this.requestURIs.contains(request.requestURI())) {
            this.logMessage("Request URI: " + request.requestURI() + " already executed");
            return new QueryResultIMPL();
        }

        // System.err.println("Request URI: " + request.requestURI() + " not executed
        // yet");
        // System.err.println("RequestURIS : " + this.requestURIs.toString());

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
            return this.directionalPropagation(state, request, result);
        }

        // Return the final result
        return result;
    }

    private QueryResultI directionalPropagation(ExecutionStateI state, RequestI request, QueryResultI result)
            throws Exception {
        for (NodeInfoI neighbour : neighbours) {
            if (state.getDirections()
                    .contains(processingNode.getPosition().directionFrom(neighbour.nodePosition()))) {
                SensorNodeP2POutboundPort nodePort = this.nodeInfoToP2POutboundPortMap.get(neighbour);
                if (nodePort != null) {
                    // ExecutionStateI newState = new ExecutionStateIMPL();
                    // ((ExecutionStateIMPL) newState).setDirectional(true);
                    ((ExecutionStateIMPL) state).incrementHops();

                    RequestContinuationI continuation = new RequestContinuationIMPL(
                            request.requestURI(), request.getQueryCode(), request.isAsynchronous(),
                            request.clientConnectionInfo(), state);
                    QueryResultI res = nodePort.execute(continuation);
                    ((QueryResultIMPL) result).update(res);
                }
            }
        }
        return result;
    }

    private QueryResultI floodingPropagation(ExecutionStateI state, RequestI request, QueryResultI result)
            throws Exception {

        System.err.println("Neighbours : ");
        neighbours.forEach(n -> System.err.println(n.nodeIdentifier()));
        for (NodeInfoI neighbour : neighbours) {
            if (state.withinMaximalDistance(neighbour.nodePosition())) {
                SensorNodeP2POutboundPort nodePort = this.nodeInfoToP2POutboundPortMap.get(neighbour);
                if (nodePort != null) {
                    double newMaximalDistance = ((ExecutionStateIMPL) state).getMaxDistance()
                            - processingNode.getPosition().distance(neighbour.nodePosition());
                    ((ExecutionStateIMPL) state).updateMaxDistance(newMaximalDistance);
                    // Create new continuation
                    RequestContinuationI continuation = new RequestContinuationIMPL(
                            request.requestURI(), request.getQueryCode(), request.isAsynchronous(),
                            request.clientConnectionInfo(), state);
                    // Execute the continuation

                    QueryResultI res = nodePort.execute(continuation);
                    // print the result of the query in a readable format
                    System.err.println("result we got " + res.toString() + " from " + neighbour.nodeIdentifier());
                    // Update the result
                    ((QueryResultIMPL) result).update(res);
                }
            }
        }
        return result;
    }

    @Override
    public void executeAsync(RequestI request) throws Exception {
        if (request == null) {
            throw new Exception("request is null");
        }
        if (this.requestURIs.contains(request.requestURI())) {
            this.logMessage("Request URI: " + request.requestURI() + " already executed");
            return;
        }

        if (request.getQueryCode() == null) {
            throw new Exception("Query is null");
        }
        this.requestURIs.add(request.requestURI());

        this.logMessage("received async request: " + request.toString());

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
            }
            // Propagate the request to neighbours and update the result
            this.directionalPropagationAsync(state, request);
        }
    }

    private void returnResultToClient(RequestI request, QueryResultI result) throws Exception {
        this.doPortConnection(this.requestResultOutboundPort.getClientPortURI(),
                ((BCM4JavaEndPointDescriptorI) request.clientConnectionInfo().endPointInfo()).getInboundPortURI(),
                ClientRequestResult.class.getCanonicalName());

        this.logMessage("Sending result to client");
        this.requestResultOutboundPort.acceptRequestResult(request.requestURI(), result);

        this.doPortDisconnection(this.requestResultOutboundPort.getClientPortURI());
    }

    private void directionalPropagationAsync(ExecutionStateI state, RequestI request)
            throws Exception {
        for (NodeInfoI neighbour : neighbours) {
            if (state.getDirections()
                    .contains(processingNode.getPosition().directionFrom(neighbour.nodePosition()))) {
                SensorNodeP2POutboundPort nodePort = this.nodeInfoToP2POutboundPortMap.get(neighbour);
                if (nodePort != null) {
                    ((ExecutionStateIMPL) state).incrementHops();
                    RequestContinuationI continuation = new RequestContinuationIMPL(
                            request.requestURI(), request.getQueryCode(), request.isAsynchronous(),
                            request.clientConnectionInfo(), state);
                    nodePort.executeAsync(continuation);
                }
            }
        }
    }

    private void floodingPropagationAsync(ExecutionStateI state, RequestI request)
            throws Exception {
        for (NodeInfoI neighbour : neighbours) {
            if (state.withinMaximalDistance(neighbour.nodePosition())) {
                SensorNodeP2POutboundPort nodePort = this.nodeInfoToP2POutboundPortMap.get(neighbour);
                if (nodePort != null) {
                    double newMaximalDistance = ((ExecutionStateIMPL) state).getMaxDistance()
                            - processingNode.getPosition().distance(neighbour.nodePosition());
                    ((ExecutionStateIMPL) state).updateMaxDistance(newMaximalDistance);

                    RequestContinuationI continuation = new RequestContinuationIMPL(
                            request.requestURI(), request.getQueryCode(), request.isAsynchronous(),
                            request.clientConnectionInfo(), state);
                    nodePort.executeAsync(continuation);
                }
            }
        }
    }

    @Override
    public void executeAsync(RequestContinuationI requestContinuation) throws Exception {
        if (requestContinuation == null) {
            throw new Exception("request is null");
        }
        if (this.requestURIs.contains(requestContinuation.requestURI())) {
            this.logMessage("Request URI: " + requestContinuation.requestURI() + " already executed");
            return;
        }

        if (requestContinuation.getQueryCode() == null) {
            throw new Exception("Query is null");
        }
        this.requestURIs.add(requestContinuation.requestURI());

        this.logMessage("received async request: " + requestContinuation.toString());

        AbstractQuery query = (AbstractQuery) requestContinuation.getQueryCode();
        ExecutionStateI state = ((RequestContinuationIMPL) requestContinuation).getExecutionState();
        ((ExecutionStateIMPL) state).updateProcessingNode(this.processingNode);
        QueryResultI result = query.eval(state);
        if (state.isFlooding()) {
            // Propagate the request to neighbours and update the result
            this.floodingPropagationAsync(state, requestContinuation);
        } else if (state.isDirectional()) {
            // Can't propagate if no more hops
            // Return local result
            if (((ExecutionStateIMPL) state).noMoreHops()) {
                this.logMessage("No more hops");
                returnResultToClient(requestContinuation, result); // return the result to the client
            }
            // Propagate the request to neighbours and update the result
            this.directionalPropagationAsync(state, requestContinuation);
        }

        // every node will return the result to the client after executing the request
        // and propagating it to the neighbours
        returnResultToClient(requestContinuation, result); // return the result to the client
    }
}
