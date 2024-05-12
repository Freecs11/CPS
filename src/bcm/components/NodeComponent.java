package bcm.components;

import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import bcm.CVM;
import bcm.DistributedCVM;
import bcm.plugin.NodePlugin;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PImplI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingImplI;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import fr.sorbonne_u.utils.aclocks.ClocksServerCI;
import fr.sorbonne_u.utils.aclocks.ClocksServerConnector;
import fr.sorbonne_u.utils.aclocks.ClocksServerOutboundPort;
import implementation.NodeInfoIMPL;
import implementation.PositionIMPL;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>NodeComponent</code> implements the component representing a
 * sensor node in the sensor network. It is responsible for the management of
 * the node's lifecycle, it contains the sensor data and the node's information.
 * </p>
 */
@RequiredInterfaces(required = { ClocksServerCI.class })
public class NodeComponent extends AbstractComponent
        implements RequestingImplI, SensorNodeP2PImplI {

    // -------------------- L'HORLOGE -------------------------
    protected AcceleratedClock clock;
    protected Instant startInstant;
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

    // Plugin
    protected NodePlugin nodePlugin;

    /**
     * Constructor of the class NodeComponent
     * 
     * @param uri
     * @param nodeId
     * @param x
     * @param y
     * @param range
     * @param registryInboundPortURI
     * @param sensorData
     * @param startInstant
     * @param nbAsyncThreads
     * @param nbSyncThreads
     * @param syncRequestPoolUri
     * @param asyncRequestPoolUri
     * @param syncContPoolUri
     * @param asyncContPoolUri
     * @throws Exception
     */
    protected NodeComponent(String uri,
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
        super(uri, nbSyncThreads, nbAsyncThreads);

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

        this.nodeURI = uri;
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

        // initialisation de l'objet nodeInfo
        NodeInfoI nodeInfo = new NodeInfoIMPL(nodeId, new PositionIMPL(x, y), null, null, range);
        this.nodePlugin = new NodePlugin(
                registryInboundPortURI,
                nodeInfo,
                sensorData);
        this.nodePlugin.setPluginURI(AbstractOutboundPort.generatePortURI());
        this.installPlugin(nodePlugin);

        AbstractComponent.checkImplementationInvariant(this);
        AbstractComponent.checkInvariant(this);
    }

    /**
     * Constructor of the class NodeComponent with auto-generated URI
     * 
     * @param nodeId
     * @param x
     * @param y
     * @param range
     * @param registryInboundPortURI
     * @param sensorData
     * @param startInstant
     * @param nbAsyncThreads
     * @param nbSyncThreads
     * @param syncRequestPoolUri
     * @param asyncRequestPoolUri
     * @param syncContPoolUri
     * @param asyncContPoolUri
     * @throws Exception
     */
    protected NodeComponent(
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
        super(AbstractInboundPort.generatePortURI(), nbSyncThreads, nbAsyncThreads);
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
        this.nodeURI = this.reflectionInboundPortURI;
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

        // initialisation de l'objet nodeInfo
        NodeInfoI nodeInfo = new NodeInfoIMPL(nodeId, new PositionIMPL(x, y), null, null, range);
        this.nodePlugin = new NodePlugin(
                registryInboundPortURI,
                nodeInfo,
                sensorData);
        this.nodePlugin.setPluginURI(AbstractOutboundPort.generatePortURI());
        this.installPlugin(nodePlugin);
        AbstractComponent.checkImplementationInvariant(this);
        AbstractComponent.checkInvariant(this);
    }

    /**
     * Getter for the URI of the node
     * 
     * @return index of the pool of thread of synchronous request
     */
    public int getSyncRequestPoolIndex() {
        return syncRequestPoolIndex;
    }

    /**
     * Getter for the URI of the node
     * 
     * @return index of the pool of thread of asynchronous request
     */
    public int getAsyncRequestPoolIndex() {
        return asyncRequestPoolIndex;
    }

    /**
     * Getter for the URI of the node
     * 
     * @return index of the pool of thread of synchronous continuation
     */
    public int getSyncContPoolIndex() {
        return syncContPoolIndex;
    }

    /**
     * Getter for the URI of the node
     * 
     * @return index of the pool of thread of asynchronous continuation
     */
    public int getAsyncContPoolIndex() {
        return asyncContPoolIndex;
    }

    /**
     * See {@link fr.sorbonne_u.components.AbstractComponent#start()}
     */
    @Override
    public void start() throws ComponentStartException {
        try {
            this.logMessage("Starting " + this.nodeURI);
        } catch (Exception e) {
            throw new ComponentStartException(e);
        }
        this.logMessage("Node Component successfully started: " + this.nodeURI);

        super.start();
    }

    /**
     * See {@link fr.sorbonne_u.components.AbstractComponent#execute()}
     */
    @Override
    public void execute() throws Exception {
        // ------CONNECTION TO THE CLOCK SERVER------
        prepareClockConnection();
        // ----------------- DELAYED STARTUP -----------------

        this.logMessage("Node component waiting.......");
        // We block the current thread until the client is ready to start (using the
        // calculated start instant)
        while (true) {
            long delay = this.clock.nanoDelayUntilInstant(this.startInstant);
            if (delay <= 0) {
                break;
            }
        }
        this.logMessage("Node component starting.......");

        // ------REGISTERING THE NODE------

        long delayRegister = this.clock.nanoDelayUntilInstant(this.startInstant);
        this.scheduleTask(
                o -> {
                    try {
                        this.nodePlugin.registerNode();
                        System.err.println("Node registered");
                    } catch (Exception e) {
                        System.err.println("Node registered");
                        e.printStackTrace();
                    }
                }, delayRegister, TimeUnit.NANOSECONDS);
    }

    /**
     * See
     * {@link fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI#ask4Connection(NodeInfoI)}
     */
    public void ask4Connection(NodeInfoI newNeighbour)
            throws Exception {
        this.nodePlugin.ask4Connection(newNeighbour);
    }

    /**
     * See
     * {@link fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI#ask4Disconnection(NodeInfoI)}
     */
    public void ask4Disconnection(NodeInfoI neighbour) {
        this.nodePlugin.ask4Disconnection(neighbour);
    }

    /**
     * See {@link fr.sorbonne_u.components.AbstractComponent#finalise()}
     */
    @Override
    public void finalise() throws Exception {
        this.logMessage("stopping node component : " + this.nodeURI);
        super.finalise();
    }

    /**
     * See {@link fr.sorbonne_u.components.AbstractComponent#shutdown()}
     */
    @Override
    public void shutdown() throws ComponentShutdownException {
        // the shutdown is a good place to unpublish inbound ports.
        try {
            this.nodePlugin.uninstall();
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
    }

    /**
     * See
     * {@link fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI#execute(RequestContinuationI)}
     */
    @Override
    public QueryResultI execute(RequestContinuationI request) throws Exception {
        return this.nodePlugin.execute(request);
    }

    /**
     * See
     * {@link fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI#execute(RequestI)}
     */
    @Override
    public QueryResultI execute(RequestI request) throws Exception {
        return this.nodePlugin.execute(request);
    }

    /**
     * See
     * {@link fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI#executeAsync(RequestI)}
     */
    @Override
    public void executeAsync(RequestI request) throws Exception {
        this.nodePlugin.executeAsync(request);
    }

    /**
     * See
     * {@link fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI#executeAsync(RequestContinuationI)}
     */
    @Override
    public void executeAsync(RequestContinuationI requestContinuation) throws Exception {
        this.nodePlugin.executeAsync(requestContinuation);
    }

    /**
     * Init the connection to the clock component and wait to start
     * 
     * @throws Exception
     */
    private void prepareClockConnection() throws Exception {
        ClocksServerOutboundPort clockPort = new ClocksServerOutboundPort(
                AbstractOutboundPort.generatePortURI(), this);
        this.logMessage("Node component connecting to the clock server");
        clockPort.publishPort();
        this.doPortConnection(
                clockPort.getPortURI(),
                ClocksServer.STANDARD_INBOUNDPORT_URI,
                ClocksServerConnector.class.getCanonicalName());
        this.clock = clockPort.getClock(CVM.CLOCK_URI);
        this.doPortDisconnection(clockPort.getPortURI());
        clockPort.unpublishPort();
        clockPort.destroyPort();
        this.clock.waitUntilStart();
        this.logMessage("Node component connected to the clock server");
    }
}
