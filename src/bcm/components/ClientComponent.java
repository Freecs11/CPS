package bcm.components;

import java.io.File;
import java.io.FileWriter;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import bcm.CVM;
import bcm.DistributedCVM;
import bcm.plugin.ClientPlugin;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import fr.sorbonne_u.utils.aclocks.ClocksServerCI;
import fr.sorbonne_u.utils.aclocks.ClocksServerConnector;
import fr.sorbonne_u.utils.aclocks.ClocksServerOutboundPort;
import query.ast.BooleanQuery;
import query.ast.ConditionalExprBooleanExpr;
import query.ast.ConstantRand;
import query.ast.DirectionContinuation;
import query.ast.EqualConditionalExpr;
import query.ast.FinalDirections;
import query.ast.FinalGather;
import query.ast.FloodingContinuation;
import query.ast.GatherQuery;
import query.ast.RecursiveDirections;
import query.ast.RecursiveGather;
import query.ast.RelativeBase;
import query.ast.SensorRand;

@RequiredInterfaces(required = { ClocksServerCI.class })
public class ClientComponent extends AbstractComponent {

        // --------- clock and start instant---------
        protected long startAfter;

        // --------- clock and start instant---------
        protected AcceleratedClock clock;
        protected Instant startInstant;

        // ---------plugin---------
        protected ClientPlugin plugin;

        // ---------Async timeout , the time to wait for the results to be gathered and
        // combined---------
        private long asyncTimeout = TimeUnit.SECONDS.toNanos(30L);
        // -----Hash map to store queries and target nodes---------
        private Map<String, List<QueryI>> queries;
        // -----List of intervals to wait before sending the requests---------
        private List<Long> intervals;

        private Map<String, Long> syncResults = new HashMap<>();
        private Map<String, Long> asyncResults = new HashMap<>();

        // protected ClientComponent(String uri, String registryInboundPortURI,
        // int nbThreads, int nbSchedulableThreads, String clientIdentifer) throws
        // Exception {
        // super(uri, nbThreads, nbSchedulableThreads);

        // // ---------Init the plugin---------
        // this.plugin = new ClientPlugin(registryInboundPortURI, clientIdentifer);
        // this.plugin.setPluginURI(AbstractOutboundPort.generatePortURI());
        // this.installPlugin(plugin);

        // this.getTracer().setTitle("Client Component");
        // this.getTracer().setRelativePosition(1, 1);
        // AbstractComponent.checkImplementationInvariant(this);
        // AbstractComponent.checkInvariant(this);
        // }

        protected ClientComponent(String uri, String registryInboundPortURI, Instant startInstant,
                        int nbThreads,
                        int nbSchedulableThreads, String clientIdentifer,
                        Map<String, List<QueryI>> queries,
                        List<Long> intervals) throws Exception {
                super(uri, nbThreads, nbSchedulableThreads);

                this.startInstant = startInstant;
                this.queries = queries;
                this.intervals = intervals;
                // ---------Init the plugin---------
                this.plugin = new ClientPlugin(registryInboundPortURI, clientIdentifer);
                this.plugin.setPluginURI(AbstractOutboundPort.generatePortURI());
                this.installPlugin(plugin);

                this.getTracer().setTitle("Client Component");
                this.getTracer().setRelativePosition(1, 1);
                AbstractComponent.checkImplementationInvariant(this);
                AbstractComponent.checkInvariant(this);
        }

        @Override
        public synchronized void start() throws ComponentStartException {
                this.logMessage("starting client component.");
                super.start();
        }

        @Override
        public synchronized void execute() throws Exception {

                // ---------Connection to the clock component ------------
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

                this.clock.waitUntilStart();

                long delayTilStart = this.clock.nanoDelayUntilInstant(this.startInstant);
                this.logMessage("Waiting " + delayTilStart + " ns before starting the client component.");
                this.scheduleTask(
                                nil -> {
                                        this.logMessage("Client " + " component starting...");
                                }, delayTilStart, TimeUnit.NANOSECONDS);
                long elapsedTime = 0l;
                for (Long interval : this.intervals) {
                        for (Entry<String, List<QueryI>> entry : this.queries.entrySet()) {
                                String nodeID = entry.getKey();
                                for (QueryI query : entry.getValue()) {
                                        delayTilStart += interval;
                                        Instant instantBefore = this.clock.currentInstant();
                                        String requestURI = "req" + instantBefore.toString();
                                        this.asyncResults.put(requestURI, instantBefore.toEpochMilli());
                                        this.plugin.executeAsyncRequest(
                                                        requestURI, query,
                                                        nodeID,
                                                        delayTilStart, this.asyncTimeout);
                                        instantBefore = this.clock.currentInstant();
                                        requestURI = "req" + instantBefore.toString();
                                        this.syncResults.put(requestURI, instantBefore.toEpochMilli());
                                        this.plugin.executeSyncRequest(requestURI, query, nodeID, delayTilStart);
                                        // System.err.println("RequestURI: " + requestURI + " Query: " +
                                        // query.toString()
                                        // + " NodeID: " + nodeID + " Interval: " + delayTilStart);
                                        Instant instantAfter = this.clock.currentInstant();
                                        elapsedTime = instantAfter.toEpochMilli() - instantBefore.toEpochMilli();
                                        this.syncResults.put(requestURI, elapsedTime);
                                }
                        }
                }

        }

        @Override
        public synchronized void finalise() throws Exception {
                this.plugin.finalise();
                super.finalise();
                try {
                        FileWriter testResults = new FileWriter(
                                        new File("testResults" + this.reflectionInboundPortURI + ".csv"));
                        testResults.write("RequestURI,SyncTime,AsyncTime\n");
                        for (Entry<String, Long> entry : this.syncResults.entrySet()) {
                                testResults.write(entry.getKey() + "," + entry.getValue() + ","
                                                + this.asyncResults.get(entry.getKey()) + "\n");
                        }
                        testResults.close();
                        this.printExecutionLogOnFile("logRegistry");
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        @Override
        public synchronized void shutdown() throws ComponentShutdownException {
                try {
                        this.plugin.uninstall();
                } catch (Exception e) {
                        throw new ComponentShutdownException(e);
                }
                super.shutdown();
        }

        @Override
        public synchronized void shutdownNow() throws ComponentShutdownException {
                super.shutdown();
        }

        public void acceptRequestResult(String requestURI, QueryResultI result) throws Exception {
                this.plugin.acceptRequestResult(requestURI, result);
                long elapsedTime = this.clock.currentInstant().toEpochMilli()
                                - this.asyncResults.get(requestURI);
                this.asyncResults.put(requestURI, elapsedTime);
        }
}
