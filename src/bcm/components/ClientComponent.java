package bcm.components;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import bcm.CVM;
import bcm.plugin.ClientPlugin;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.AbstractPort;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import fr.sorbonne_u.utils.aclocks.ClocksServerCI;
import fr.sorbonne_u.utils.aclocks.ClocksServerConnector;
import fr.sorbonne_u.utils.aclocks.ClocksServerOutboundPort;
import utils.QueryMetrics;

/**
 * <p>
 * <strong> Description </strong>
 * </p>
 * <p>
 * The class <code>ClientComponent</code> represents a client component that
 * sends queries to the target node and waits for the results to be
 * gathered and combined. The client component can be in test mode or not. In
 * test mode, the client sends the queries in the list with the intervals
 * specified in the list. If not in test mode, the client sends the queries in
 * the list with a default interval of 20 seconds.
 * </p>
 */
@RequiredInterfaces(required = { ClocksServerCI.class })
public class ClientComponent extends AbstractComponent {

        // --------- clock and start instant---------
        protected long startAfter;

        // --------- clock and start instant---------
        public AcceleratedClock clock;
        protected Instant startInstant;

        // ---------plugin---------
        protected ClientPlugin plugin;

        // ---------Async timeout , the time to wait for the results to be gathered and
        // combined---------
        private long asyncTimeout = TimeUnit.SECONDS.toNanos(20L);
        // -----Hash map to store queries and target nodes---------
        private Map<String, List<QueryI>> queries;
        // -----List of intervals to wait before sending the requests---------
        private List<Long> intervals;

        public Map<String, QueryMetrics> queryMetrics = new HashMap<>();
        private String filename;
        private boolean TESTMODE;

        /**
         * Constructor of the client component
         * 
         * @param uri
         * @param registryInboundPortURI
         * @param startInstant
         * @param nbThreads
         * @param nbSchedulableThreads
         * @param clientIdentifer
         * @param queries                Hash map to store queries and target nodes
         * @param intervals              List of intervals to wait before sending the
         *                               requests
         * @param isTestMode             Boolean to indicate if the client is in test
         *                               mode
         * @param filename               CSV file to store the results
         * @throws Exception
         */
        protected ClientComponent(String uri, String registryInboundPortURI, Instant startInstant,
                        int nbThreads,
                        int nbSchedulableThreads, String clientIdentifer,
                        Map<String, List<QueryI>> queries,
                        List<Long> intervals,
                        boolean isTestMode,
                        String filename) throws Exception {
                super(uri, nbThreads, nbSchedulableThreads);
                assert uri != null;
                assert registryInboundPortURI != null;
                assert startInstant != null;
                assert nbThreads > 0;
                assert nbSchedulableThreads >= 0;
                assert clientIdentifer != null;
                assert queries != null;
                assert intervals != null;
                this.TESTMODE = isTestMode;
                this.startInstant = startInstant;
                this.queries = queries;
                this.intervals = intervals;
                if (TESTMODE) {
                        this.filename = filename;
                } else {
                        this.filename = "results.csv";
                }
                // ---------Init the plugin---------
                this.plugin = new ClientPlugin(registryInboundPortURI, clientIdentifer, TESTMODE, filename);
                this.plugin.setPluginURI(AbstractPort.generatePortURI());
                this.installPlugin(plugin);

                this.getTracer().setTitle("Client Component");
                this.getTracer().setRelativePosition(1, 1);
                AbstractComponent.checkImplementationInvariant(this);
                AbstractComponent.checkInvariant(this);
        }

        /**
         * See {@link fr.sorbonne_u.components.AbstractComponent#start()}
         */
        @Override
        public synchronized void start() throws ComponentStartException {
                this.logMessage("starting client component.");
                super.start();
        }

        /**
         * See {@link fr.sorbonne_u.components.AbstractComponent#execute()}
         */
        @Override
        public synchronized void execute() throws Exception {
                this.prepareClockConnection();

                long delayTilStart = this.clock.nanoDelayUntilInstant(this.startInstant);
                this.logMessage("Waiting " + delayTilStart + " ns before starting the client component.");
                this.scheduleTask(
                                nil -> {
                                        this.logMessage("Client " + " component starting...");
                                }, delayTilStart, TimeUnit.NANOSECONDS);

                // Wait until the start instant
                while (true) {
                        long delay = this.clock.nanoDelayUntilInstant(this.startInstant);
                        if (delay <= 0) {
                                break;
                        }
                }

                if (this.TESTMODE) {
                        for (Long interval : intervals) {
                                for (Map.Entry<String, List<QueryI>> entry : queries.entrySet()) {
                                        for (QueryI query : entry.getValue()) {
                                                final String nodeID = entry.getKey();
                                                long currentDelay = this.clock
                                                                .nanoDelayUntilInstant(
                                                                                startInstant.plusSeconds(interval));
                                                String requestURI = generateRequestURI(Instant.now());

                                                try {
//                                                        plugin.executeSyncRequest(requestURI, query, nodeID,
//                                                                        currentDelay);
                                                         plugin.executeAsyncRequest(requestURI, query, nodeID,
                                                         currentDelay, asyncTimeout);
                                                } catch (Exception e) {
                                                        logError(e);
                                                }
                                        }
                                }
                        }
                } else {

                        // if not in test mode, we execute the queries in the list with default interval
                        // 20
                        long delay = this.clock.nanoDelayUntilInstant(this.startInstant.plusSeconds(20));
                        for (Entry<String, List<QueryI>> entry : queries.entrySet()) {
                                String nodeID = entry.getKey();
                                for (QueryI query : entry.getValue()) {
                                        delay += TimeUnit.SECONDS.toNanos(20);
                                        try {
                                                // execute the query
                                                plugin.executeSyncRequest(
                                                                generateRequestURI(Instant.now()), query, nodeID,
                                                                delay);
                                        } catch (Exception e) {
                                                logError(e);
                                        }
                                }
                        }

                }
        }

        /**
         * Init the connection to the clock component and wait to start
         * 
         * @throws Exception
         */
        private void prepareClockConnection() throws Exception {
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

                // wait until the clock is started
                this.clock.waitUntilStart();
        }

        /**
         * Generate a request URI based on the current time and a random number
         * 
         * @param start
         * @return
         */
        private String generateRequestURI(Instant start) {
                return "req" + start.toString() + Math.random();
        }

        /**
         * Log an error message
         * 
         * @param e
         */
        private void logError(Exception e) {
                this.logMessage("Error: " + e.getMessage());
        }

        /**
         * See
         * {@link fr.sorbonne_u.cps.sensor_network.interfaces.RequestResultCI#acceptRequestResult(String,
         * QueryResultI)}
         */
        public void acceptRequestResult(String requestURI, QueryResultI result) throws Exception {
                this.plugin.acceptRequestResult(requestURI, result);
        }

        /**
         * See {@link fr.sorbonne_u.components.AbstractComponent#execute()}
         */
        @Override
        public synchronized void finalise() throws Exception {
                super.finalise();
                if (this.TESTMODE)
                        try {
                                this.printExecutionLogOnFile("logRegistry");
                        } catch (Exception e) {
                                e.printStackTrace();
                        }
        }

        /**
         * See {@link fr.sorbonne_u.components.AbstractComponent#shutdown()}
         */
        @Override
        public synchronized void shutdown() throws ComponentShutdownException {
                try {
                        this.plugin.uninstall();
                } catch (Exception e) {
                        throw new ComponentShutdownException(e);
                }
                super.shutdown();
        }

        /**
         * See {@link fr.sorbonne_u.components.AbstractComponent#shutdownNow()}
         */
        @Override
        public synchronized void shutdownNow() throws ComponentShutdownException {
                super.shutdown();
        }
}
