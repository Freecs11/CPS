package bcm.components;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import bcm.CVM;
import bcm.plugin.ClientPlugin;
import fr.sorbonne_u.components.AbstractComponent;
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

        private Map<String, QueryMetrics> queryMetrics = new HashMap<>();

        private boolean TESTMODE;

        protected ClientComponent(String uri, String registryInboundPortURI, Instant startInstant,
                        int nbThreads,
                        int nbSchedulableThreads, String clientIdentifer,
                        Map<String, List<QueryI>> queries,
                        List<Long> intervals,
                        boolean isTestMode) throws Exception {
                super(uri, nbThreads, nbSchedulableThreads);
                this.TESTMODE = isTestMode;
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
                this.prepareClockConnection();

                long delayTilStart = this.clock.nanoDelayUntilInstant(this.startInstant);
                this.logMessage("Waiting " + delayTilStart + " ns before starting the client component.");
                this.scheduleTask(
                                nil -> {
                                        this.logMessage("Client " + " component starting...");
                                }, delayTilStart, TimeUnit.NANOSECONDS);

                // on bloque le thread courant jusqu'à ce que le client soit prêt à démarrer (
                // on utilisant l'instant de démarrage calculé précédemment)
                this.wait(TimeUnit.NANOSECONDS.toMillis(delayTilStart));

                if (this.TESTMODE) {
                        this.logMessage("queryList received: " + queries.toString() + " intervals: "
                                        + intervals.toString());
                        for (Long interval : intervals) {
                                for (Map.Entry<String, List<QueryI>> entry : queries.entrySet()) {
                                        for (QueryI query : entry.getValue()) {
                                                final String nodeID = entry.getKey();
                                                long currentDelay = this.clock
                                                                .nanoDelayUntilInstant(
                                                                                startInstant.plusSeconds(interval));
                                                String requestURI = generateRequestURI(Instant.now());
                                                long startTime = System.nanoTime();
                                                try {
                                                        plugin.executeAsyncRequest(requestURI, query, nodeID,
                                                                        currentDelay, asyncTimeout);
                                                } catch (Exception e) {
                                                        logError(e);
                                                }
                                                queryMetrics.put(requestURI,
                                                                new QueryMetrics(startTime + currentDelay,
                                                                                startTime + currentDelay + asyncTimeout,
                                                                                interval, 0));
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

        private synchronized void storeTestResults() throws Exception {
                File file = new File("testResults.csv");
                try (FileWriter testResults = new FileWriter(file, true); // Open in append mode
                                FileChannel channel = new FileOutputStream(file, true).getChannel()) {
                        // Try acquiring the lock without blocking
                        try (FileLock lock = channel.tryLock()) {
                                if (lock != null) {
                                        for (Map.Entry<String, QueryMetrics> entry : queryMetrics.entrySet()) {
                                                QueryMetrics metrics = entry.getValue();
                                                testResults.write(entry.getKey() + "," + metrics.startTime + ","
                                                                + metrics.endTime + "," +
                                                                metrics.interval + "," + metrics.duration + "\n");
                                        }
                                } else {
                                        System.err.println("Could not lock the file for writing: testResults.csv");
                                }
                        }
                }
        }

        private String generateRequestURI(Instant start) {
                return "req" + start.toString() + Math.random();
        }

        private void logError(Exception e) {
                this.logMessage("Error: " + e.getMessage());
        }

        public void acceptRequestResult(String requestURI, QueryResultI result) throws Exception {
                this.plugin.acceptRequestResult(requestURI, result);

                if (this.TESTMODE) {
                        long endTime = System.nanoTime();
                        QueryMetrics metrics = queryMetrics.get(requestURI);
                        this.logMessage("Request " + requestURI + " , endTime: " + endTime + " , registered endTime: "
                                        + metrics.endTime);

                        // si le temps de la récéoption du résultat est inférieur au temps de fin (
                        // temps que le client regroupe les résultats)
                        if (endTime < metrics.endTime) {
                                metrics.duration = endTime - metrics.startTime;
                                queryMetrics.put(requestURI, metrics);
                        }
                }
        }

        @Override
        public synchronized void finalise() throws Exception {
                this.plugin.finalise();
                super.finalise();
                if (this.TESTMODE)
                        try {
                                storeTestResults();
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

        private class QueryMetrics {
                long startTime;
                long endTime;
                long interval;
                long duration;

                QueryMetrics(long startTime, long endTime, long interval, long duration) {
                        this.startTime = startTime;
                        this.endTime = endTime;
                        this.interval = interval;
                        this.duration = duration;
                }
        }
}
