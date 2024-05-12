package bcm.components;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
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

        protected ClientComponent(String uri, String registryInboundPortURI, Instant startInstant,
                        int nbThreads,
                        int nbSchedulableThreads, String clientIdentifer,
                        Map<String, List<QueryI>> queries,
                        List<Long> intervals,
                        boolean isTestMode,
                        String filename) throws Exception {
                super(uri, nbThreads, nbSchedulableThreads);
                this.TESTMODE = isTestMode;
                this.startInstant = startInstant;
                this.queries = queries;
                this.intervals = intervals;
                this.filename = filename;
                // ---------Init the plugin---------
                this.plugin = new ClientPlugin(registryInboundPortURI, clientIdentifer, filename);
                this.plugin.setPluginURI(AbstractPort.generatePortURI());
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
                while (true) {
                        long delay = this.clock.nanoDelayUntilInstant(this.startInstant);
                        if (delay <= 0) {
                                break;
                        }
                }

                if (this.TESTMODE) {
                        // this.logMessage("queryList received: " + queries.toString() + " intervals: "
                        // + intervals.toString());
                        for (Long interval : intervals) {
                                // long currentDelay = this.clock
                                // .nanoDelayUntilInstant(
                                // startInstant.plusSeconds(interval));
                                for (Map.Entry<String, List<QueryI>> entry : queries.entrySet()) {
                                        for (QueryI query : entry.getValue()) {
                                                final String nodeID = entry.getKey();
                                                long currentDelay = this.clock
                                                                .nanoDelayUntilInstant(
                                                                                startInstant.plusSeconds(interval));
                                                String requestURI = generateRequestURI(Instant.now());

                                                try {
                                                        // plugin.executeAsyncRequest(requestURI, query, nodeID,
                                                        // currentDelay, asyncTimeout);
                                                        plugin.executeAsyncRequest(requestURI, query, nodeID,
                                                                        currentDelay, asyncTimeout);
                                                } catch (Exception e) {
                                                        logError(e);
                                                }
                                                // queryMetrics.put(requestURI,
                                                // new QueryMetrics(startTime,
                                                // startTime + asyncTimeout / 1000000,
                                                // interval, 0));

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

        private void storeTestResults() throws Exception {
                File file = new File(filename);
                if (!file.exists()) {
                        file.createNewFile();
                }

                try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
                                FileChannel channel = randomAccessFile.getChannel()) {
                        // Acquire the lock
                        try (FileLock lock = channel.lock()) {
                                // Move to the end of the file to append data
                                randomAccessFile.seek(randomAccessFile.length());

                                // If file length is 0, write headers
                                if (randomAccessFile.length() == 0) {
                                        randomAccessFile.writeBytes("RequestURI,StartTime,EndTime,Interval,Duration\n");
                                }

                                // Write each entry to the file
                                for (Map.Entry<String, QueryMetrics> entry : queryMetrics.entrySet()) {
                                        QueryMetrics metrics = entry.getValue();
                                        StringBuilder data = new StringBuilder();
                                        data.append(entry.getKey()).append(",");
                                        data.append(metrics.getStartTime()).append(",");
                                        data.append(metrics.getEndTime()).append(",");
                                        data.append(metrics.getInterval()).append(",");
                                        data.append(metrics.getDuration()).append("\n");

                                        randomAccessFile.writeBytes(data.toString());
                                }
                        } catch (OverlappingFileLockException e) {
                                System.err.println(
                                                "File is already locked in this thread or virtual machine: testResults.csv");
                        }
                } catch (IOException e) {
                        System.err.println("Failed to write to file: " + e.getMessage());
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

                // if (this.TESTMODE) {
                // long endTime = this.clock.currentInstant().toEpochMilli();
                // QueryMetrics metrics = queryMetrics.get(requestURI);
                // this.logMessage("Request " + requestURI + " , endTime: " + endTime + " ,
                // registered endTime: "
                // + metrics.endTime);

                // // si le temps de la récéoption du résultat est inférieur au temps de fin (
                // // temps que le client regroupe les résultats)
                // if (endTime < metrics.endTime) {
                // metrics.duration = endTime - metrics.startTime;
                // queryMetrics.put(requestURI, metrics);
                // }
                // }
        }

        @Override
        public synchronized void finalise() throws Exception {
                // this.plugin.finalise();
                super.finalise();
                if (this.TESTMODE)
                        try {
                                // storeTestResults();
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
}
