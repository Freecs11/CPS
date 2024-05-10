package bcm.components;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import bcm.CVM;
import bcm.connectors.ClientRequestResult;
import bcm.connectors.RegistryConnector;
import bcm.connectors.SensorNodeConnector;
import bcm.ports.RegistrationOutboundPort;
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
public class NodeComponentP extends AbstractComponent
        implements RequestingImplI, SensorNodeP2PImplI {

    protected Set<NodeInfoI> neighbours;

    private ProcessingNodeI processingNode;

    // Liste des données des capteurs,,
    private ArrayList<SensorDataI> sensorData;

    // Uris des requetes deja executees
    protected final Set<String> requestURIs;

    protected final NodeInfoI nodeInfo;

    // -------------------- Les PORTS -------------------------
    protected final RequestingInboundPort requestingInboundPort;
    protected final SensorNodeP2PInboundPort sensorNodeP2PInboundPort;
    protected final RegistrationOutboundPort RegistrationOutboundPort;
    protected final RequestResultOutboundPort requestResultOutboundPort;

    // -------------------- HashMap pour garder les ports des voisins
    // -------------------------
    protected final ConcurrentHashMap<NodeInfoI, SensorNodeP2POutboundPort> nodeInfoToP2POutboundPortMap;

    // -------------------- L'HORLOGE -------------------------
    protected AcceleratedClock clock;
    protected Instant startInstant;

    // -------------------- URI du registre -------------------------
    protected String registerInboundPortURI;
    protected String nodeURI;

    // -------------------- POOL de thread

    protected final String syncRequestPoolUri;
    protected final String asyncRequestPoolUri;
    protected final String syncContPoolUri;
    protected final String asyncContPoolUri;

    protected final int syncRequestPoolIndex;
    protected final int asyncRequestPoolIndex;
    protected final int syncContPoolIndex;
    protected final int asyncContPoolIndex;

    // -------------------- lock pour la liste des neighbours
    // ------------------------- ca laisse les lectures en parallele pour plusieurs
    // -------------------------- mais l'écriture est bloquante et execute une à une
    // -------------------------
    private final ReadWriteLock neigboursLock = new ReentrantReadWriteLock();

    protected NodeComponentP(String uri,
            String nodeId,
            Double x, Double y,
            Double range,
            String registryInboundPortURI,
            ArrayList<SensorDataI> sensorData,
            Instant startInstant,
            int nbAsyncThreads,
            int nbSyncThreads,
            String syncRequestPoolUri,
            String asyncRequestPoolUri,
            String syncContPoolUri,
            String asyncContPoolUri) throws Exception {
        // we mainly use our own pool of threads, we use bcm's pool of threads only
        // for connections
        super(uri, 15, 15);

        assert uri != null : new PreconditionException("uri can't be null!");
        assert registryInboundPortURI != null : new PreconditionException("registryInboundPortURI can't be null!");
        assert nodeId != null : new PreconditionException("nodeId can't be null!");
        assert x != null : new PreconditionException("Position@x can't be null!");
        assert y != null : new PreconditionException("Position@y can't be null!");
        assert range != null : new PreconditionException("range can't be null!");
        assert sensorData != null : new PreconditionException("ArrayList of sensorData can't be null!");
        assert startInstant != null : new PreconditionException("startInstant can't be null!");
        assert nbAsyncThreads > 0 : new PreconditionException("nbAsyncThreads must be greater than 0!");
        assert nbSyncThreads > 0 : new PreconditionException("nbSyncThread must be greater than 0!");

        // port URI for the registry
        this.registerInboundPortURI = registryInboundPortURI;
        this.nodeURI = uri;
        // initialisation des listes
        this.requestURIs = new HashSet<>();
        this.neighbours = new HashSet<>();
        this.sensorData = sensorData;
        this.nodeInfoToP2POutboundPortMap = new ConcurrentHashMap<>();

        // initialisation des ports
        this.requestingInboundPort = new RequestingInboundPort(AbstractInboundPort.generatePortURI(), this);
        this.sensorNodeP2PInboundPort = new SensorNodeP2PInboundPort(AbstractInboundPort.generatePortURI(), this);
        this.RegistrationOutboundPort = new RegistrationOutboundPort(AbstractOutboundPort.generatePortURI(), this);
        this.requestResultOutboundPort = new RequestResultOutboundPort(
                AbstractOutboundPort.generatePortURI(), this);

        // publication des ports
        this.requestingInboundPort.publishPort();
        this.sensorNodeP2PInboundPort.publishPort();
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

        // initialisation des pools de threads
        this.syncRequestPoolUri = syncRequestPoolUri;
        this.asyncRequestPoolUri = asyncRequestPoolUri;
        this.syncContPoolUri = syncContPoolUri;
        this.asyncContPoolUri = asyncContPoolUri;
        this.syncRequestPoolIndex = this.createNewExecutorService(syncRequestPoolUri, nbSyncThreads, false);
        this.asyncRequestPoolIndex = this.createNewExecutorService(asyncRequestPoolUri, nbAsyncThreads, true);
        this.syncContPoolIndex = this.createNewExecutorService(syncContPoolUri, nbSyncThreads, false);
        this.asyncContPoolIndex = this.createNewExecutorService(asyncContPoolUri, nbAsyncThreads, true);

        AbstractComponent.checkImplementationInvariant(this);
        AbstractComponent.checkInvariant(this);
    }

    // Constructeur avec auto-génération de l'URI
    protected NodeComponentP(
            String nodeId,
            Double x, Double y,
            Double range,
            String registryInboundPortURI,
            ArrayList<SensorDataI> sensorData,
            Instant startInstant,
            int nbAsyncThreads,
            int nbSyncThreads,
            String syncRequestPoolUri,
            String asyncRequestPoolUri,
            String syncContPoolUri,
            String asyncContPoolUri) throws Exception {
        // we mainly use our own pool of threads, we use bcm's pool of threads only
        // for connections
        super(AbstractInboundPort.generatePortURI(), 15, 15);
        assert this.reflectionInboundPortURI != null : new PreconditionException("generated uri can't be null!");
        assert registryInboundPortURI != null : new PreconditionException("registryInboundPortURI can't be null!");
        assert nodeId != null : new PreconditionException("nodeId can't be null!");
        assert x != null : new PreconditionException("Position@x can't be null!");
        assert y != null : new PreconditionException("Position@y can't be null!");
        assert range != null : new PreconditionException("range can't be null!");
        assert sensorData != null : new PreconditionException("ArrayList of sensorData can't be null!");
        assert startInstant != null : new PreconditionException("startInstant can't be null!");
        assert nbAsyncThreads > 0 : new PreconditionException("nbAsyncThreads must be greater than 0!");
        assert nbSyncThreads > 0 : new PreconditionException("nbSyncThread must be greater than 0!");
        // port URI for the registry
        this.registerInboundPortURI = registryInboundPortURI;

        // initialisation des listes
        this.requestURIs = new HashSet<>();
        this.neighbours = new HashSet<>();
        this.sensorData = sensorData;
        this.nodeInfoToP2POutboundPortMap = new ConcurrentHashMap<>();

        // initialisation des ports
        this.requestingInboundPort = new RequestingInboundPort(AbstractInboundPort.generatePortURI(), this);
        this.sensorNodeP2PInboundPort = new SensorNodeP2PInboundPort(AbstractInboundPort.generatePortURI(), this);
        this.RegistrationOutboundPort = new RegistrationOutboundPort(AbstractOutboundPort.generatePortURI(), this);
        this.requestResultOutboundPort = new RequestResultOutboundPort(
                AbstractOutboundPort.generatePortURI(), this);

        // publication des ports
        this.requestingInboundPort.publishPort();
        this.sensorNodeP2PInboundPort.publishPort();
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

        // initialisation des pools de threads
        this.syncRequestPoolUri = syncRequestPoolUri;
        this.asyncRequestPoolUri = asyncRequestPoolUri;
        this.syncContPoolUri = syncContPoolUri;
        this.asyncContPoolUri = asyncContPoolUri;
        this.syncRequestPoolIndex = this.createNewExecutorService(syncRequestPoolUri, nbSyncThreads, false);
        this.asyncRequestPoolIndex = this.createNewExecutorService(asyncRequestPoolUri, nbAsyncThreads, true);
        this.syncContPoolIndex = this.createNewExecutorService(syncContPoolUri, nbSyncThreads, false);
        this.asyncContPoolIndex = this.createNewExecutorService(asyncContPoolUri, nbAsyncThreads, true);
        AbstractComponent.checkImplementationInvariant(this);
        AbstractComponent.checkInvariant(this);
    }

    public int getSyncRequestPoolIndex() {
        return syncRequestPoolIndex;
    }

    public int getAsyncRequestPoolIndex() {
        return asyncRequestPoolIndex;
    }

    public int getSyncContPoolIndex() {
        return syncContPoolIndex;
    }

    public int getAsyncContPoolIndex() {
        return asyncContPoolIndex;
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
        // on bloque le thread courant jusqu'à ce que le client soit prêt à démarrer (
        // on utilisant l'instant de démarrage calculé précédemment)
        this.wait(TimeUnit.NANOSECONDS.toMillis(delayTilStart));
        // print the current time and the start time
        // this.clock.waitUntilStart(); // wait until the start time
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
                        this.logMessage("Connecting to all the neighbours received from the registry at Time : "
                                + Instant.now() + " .....................");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    this.logMessage("Node Component successfully executed: " +
                            this.nodeInfo.nodeIdentifier());
                }, delayRegister, TimeUnit.NANOSECONDS);

        // ----------------- CONNECTING TO NEIGHBOURS -----------------
        long delayTilConnect2Neighbours = this.clock.nanoDelayUntilInstant(this.startInstant.plusSeconds(10L));
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
            Iterator<NodeInfoI> it = new HashSet<>(neighbours).iterator(); // Copie pour éviter
                                                                           // ConcurrentModificationException
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
                logMessage("Connecting to neighbour: " + neighbour.nodeIdentifier());
                p2poutboundP.ask4Connection(this.nodeInfo);
            }
        } catch (

        Exception e) {
            throw new ComponentStartException(e);
        } finally {
        }
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
                        AbstractOutboundPort.generatePortURI(), this);
                newPort.publishPort();
                this.nodeInfoToP2POutboundPortMap.put(newNeighbour, newPort);
                this.doPortConnection(newPort.getPortURI(),
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
                            AbstractOutboundPort.generatePortURI(), this);
                    newPort.publishPort();
                    this.nodeInfoToP2POutboundPortMap.put(newNeighbour, newPort);
                    this.doPortConnection(newPort.getPortURI(),
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
            this.doPortDisconnection(nodePort.getPortURI());
            nodePort.unpublishPort();

            // ----- Find new in the same direction if possible -----
            NodeInfoI newNeighbour = this.RegistrationOutboundPort.findNewNeighbour(this.nodeInfo,
                    directionOfNeighbour);
            if (newNeighbour != null && newNeighbour.nodeIdentifier() != neighbour.nodeIdentifier()) {
                SensorNodeP2POutboundPort newPort = new SensorNodeP2POutboundPort(
                        AbstractOutboundPort.generatePortURI(), this);
                newPort.publishPort();
                this.nodeInfoToP2POutboundPortMap.put(newNeighbour, newPort);
                this.doPortConnection(newPort.getPortURI(),
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

    @Override
    public synchronized void finalise() throws Exception {
        this.logMessage("stopping node component : " + this.nodeInfo.nodeIdentifier());
        // ----- NO CALL TO SERVICES IN FINALISE -----

        if (this.RegistrationOutboundPort.connected()) {
            this.doPortDisconnection(this.RegistrationOutboundPort.getPortURI());
        }
        this.RegistrationOutboundPort.unpublishPort();
        for (SensorNodeP2POutboundPort p2poutboundPort : this.nodeInfoToP2POutboundPortMap.values()) {
            if (p2poutboundPort.connected()) {
                this.doPortDisconnection(p2poutboundPort.getPortURI());
            }
            p2poutboundPort.unpublishPort();
        }

        if (this.requestResultOutboundPort.connected()) {
            this.doPortDisconnection(this.requestResultOutboundPort.getPortURI());

        }
        this.requestResultOutboundPort.unpublishPort();
        // clock

        super.finalise();
    }

    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        // the shutdown is a good place to unpublish inbound ports.
        try {
            if (this.requestingInboundPort.isPublished())
                this.requestingInboundPort.unpublishPort();
            if (this.sensorNodeP2PInboundPort.isPublished())
                this.sensorNodeP2PInboundPort.unpublishPort();

        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
    }

    @Override
    public synchronized void shutdownNow() throws ComponentShutdownException {
        // the shutdown is a good place to unpublish inbound ports.
        try {
            if (this.requestingInboundPort.isPublished())
                this.requestingInboundPort.unpublishPort();
            if (this.sensorNodeP2PInboundPort.isPublished())
                this.sensorNodeP2PInboundPort.unpublishPort();

        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdownNow();
    }

    @Override
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

    @Override
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

    @Override
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
        this.doPortConnection(this.requestResultOutboundPort.getClientPortURI(),
                ((BCM4JavaEndPointDescriptorI) request.clientConnectionInfo().endPointInfo()).getInboundPortURI(),
                ClientRequestResult.class.getCanonicalName());
        synchronized (requestURIs) {
            this.logMessage("Returning result to client: " + request.requestURI());
        }
        this.requestResultOutboundPort.acceptRequestResult(request.requestURI(), result);
        this.doPortDisconnection(this.requestResultOutboundPort.getClientPortURI());
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

    @Override
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
