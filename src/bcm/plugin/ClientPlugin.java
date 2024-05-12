package bcm.plugin;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import bcm.components.ClientComponent;
import bcm.connectors.LookUpRegistryConnector;
import bcm.connectors.RequestingConnector;
import bcm.ports.LookupOutboundPort;
import bcm.ports.RequestResultInboundPort;
import bcm.ports.RequestingOutboundPort;
import fr.sorbonne_u.components.AbstractComponent.AbstractTask;
import fr.sorbonne_u.components.AbstractPlugin;
import fr.sorbonne_u.components.AbstractPort;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestResultCI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import implementation.ConnectionInfoImpl;
import implementation.EndPointDescIMPL;
import implementation.QueryResultIMPL;
import implementation.RequestIMPL;
import utils.QueryMetrics;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>ClientPlugin</code> acts as the plugin that allows the client
 * component to send requests to the nodes and accept the results of the
 * requests.
 * </p>
 */
public class ClientPlugin
                extends AbstractPlugin {

        protected RequestResultInboundPort clientRequestResultInboundPort;

        protected LookupOutboundPort LookupOutboundPort;
        protected String registryInboundPortURI;

        public final String clientIdentifer;
        private boolean TESTMODE;
        private ConcurrentMap<String, List<QueryResultI>> resultsMap;

        public ConcurrentMap<String, QueryMetrics> queryMetrics;

        private final String filename;

        /**
         * Constructor of the client plugin
         * 
         * @param registryInboundPortURI
         * @param clientIdentifer
         * @param TESTMODE
         * @param filename
         */
        public ClientPlugin(String registryInboundPortURI, String clientIdentifer,
                        boolean TESTMODE,
                        String filename) {
                this.registryInboundPortURI = registryInboundPortURI;
                this.clientIdentifer = clientIdentifer;
                this.TESTMODE = TESTMODE;
                this.filename = filename;
                this.resultsMap = new ConcurrentHashMap<>();
                this.queryMetrics = new ConcurrentHashMap<>();
        }

        /**
         * See
         * {@link fr.sorbonne_u.components.AbstractPlugin#installOn(fr.sorbonne_u.components.ComponentI)}
         */
        @Override
        public void installOn(ComponentI owner) throws Exception {
                super.installOn(owner);
                this.addRequiredInterface(LookupCI.class);
                this.addRequiredInterface(RequestingCI.class);
                this.addOfferedInterface(RequestResultCI.class);
        }

        /**
         * See {@link fr.sorbonne_u.components.AbstractPlugin#initialise()}
         */
        @Override
        public void initialise() throws Exception {
                // ---------Init the ports---------
                this.clientRequestResultInboundPort = new RequestResultInboundPort(
                                AbstractOutboundPort.generatePortURI(),
                                this.getOwner());
                this.clientRequestResultInboundPort.publishPort();

                super.initialise();
        }

        /**
         * See {@link fr.sorbonne_u.components.AbstractPlugin#finalise()}
         */
        @Override
        public void finalise() throws Exception {
                if (TESTMODE) {
                        storeTestResults();
                }
                super.finalise();
        }

        /**
         * See {@link fr.sorbonne_u.components.AbstractPlugin#uninstall()}
         */
        @Override
        public void uninstall() throws Exception {
                if (this.clientRequestResultInboundPort.isPublished())
                        this.clientRequestResultInboundPort.unpublishPort();

                super.uninstall();
        }

        // --------- Methodes du plugin ---------

        /**
         * Execute a sync request to a node with a certain delay to launch the request
         * 
         * @param request the request to be sent
         * @param nodeId  the id of the node to send the request to
         * @param delay   the delay to wait before sending the request
         */
        public void executeSyncRequest(String requestURI, QueryI query, String nodeId, long delay) {

                this.getOwner().scheduleTask(
                                new AbstractTask() {
                                        @Override
                                        public void run() {
                                                try {
                                                        LookupOutboundPort LookupOutboundPort = new LookupOutboundPort(
                                                                        AbstractPort.generatePortURI(),
                                                                        ClientPlugin.this.getOwner());
                                                        LookupOutboundPort.publishPort();

                                                        ClientPlugin.this.getOwner().doPortConnection(
                                                                        LookupOutboundPort.getPortURI(),
                                                                        registryInboundPortURI,
                                                                        LookUpRegistryConnector.class
                                                                                        .getCanonicalName());
                                                        ConnectionInfoI nodeInfo = LookupOutboundPort
                                                                        .findByIdentifier(nodeId);
                                                        RequestingOutboundPort clientRequestingOutboundPort = new RequestingOutboundPort(
                                                                        AbstractPort.generatePortURI(),
                                                                        ClientPlugin.this.getOwner());
                                                        clientRequestingOutboundPort.publishPort();

                                                        RequestI request = new RequestIMPL(requestURI, query, false,
                                                                        new ConnectionInfoImpl(
                                                                                        ClientPlugin.this.clientIdentifer,
                                                                                        new EndPointDescIMPL(
                                                                                                        clientRequestingOutboundPort
                                                                                                                        .getPortURI(),
                                                                                                        RequestingCI.class)));

                                                        ClientPlugin.this.getOwner().doPortConnection(
                                                                        clientRequestingOutboundPort.getPortURI(),
                                                                        ((EndPointDescIMPL) nodeInfo.endPointInfo())
                                                                                        .getInboundPortURI(),
                                                                        RequestingConnector.class.getCanonicalName());
                                                        QueryMetrics metric = new QueryMetrics(0L, 0L,
                                                                        0L, 0L, 0);
                                                        long ti = ((ClientComponent) ClientPlugin.this.getOwner()).clock
                                                                        .currentInstant().toEpochMilli();
                                                        metric.setStartTime(ti);
                                                        QueryResultI res = clientRequestingOutboundPort
                                                                        .execute(request);
                                                        long tf = ((ClientComponent) ClientPlugin.this.getOwner()).clock
                                                                        .currentInstant().toEpochMilli();
                                                        metric.setEndTime(tf);
                                                        metric.setDuration(metric.getEndTime() - metric.getStartTime());

                                                        if (res.isGatherRequest()) {
                                                                ClientPlugin.this.getOwner()
                                                                                .logMessage("Gathered size : " + res
                                                                                                .gatheredSensorsValues()
                                                                                                .size()
                                                                                                + " for request with URI "
                                                                                                + request.requestURI());
                                                        } else if (res.isBooleanRequest()) {
                                                                ClientPlugin.this.getOwner()
                                                                                .logMessage("Floading size : " + res
                                                                                                .positiveSensorNodes()
                                                                                                .size());
                                                        }
                                                        ClientPlugin.this.getOwner()
                                                                        .logMessage("SYNC Query result, sent at : "
                                                                                        + Instant.now()
                                                                                        + " , URI : "
                                                                                        + request.requestURI()
                                                                                        + " : "
                                                                                        + res.toString());
                                                        ClientPlugin.this.queryMetrics.put(requestURI, metric);
                                                        ClientPlugin.this.getOwner().doPortDisconnection(
                                                                        clientRequestingOutboundPort.getPortURI());
                                                        clientRequestingOutboundPort.unpublishPort();
                                                        clientRequestingOutboundPort.destroyPort();
                                                        LookupOutboundPort.unpublishPort();
                                                        LookupOutboundPort.destroyPort();
                                                } catch (Exception e) {
                                                        e.printStackTrace();
                                                }
                                        }
                                }, delay, TimeUnit.NANOSECONDS);
        }

        /**
         * Execute an async request to a node with a certain delay to launch the request
         * and a certain timeout to wait for the results to be gathered and combined
         * 
         * @param request the request to be sent
         * @param nodeId  the id of the node to send the request to
         * @param delay   the delay to wait before sending the request
         * @throws Exception
         */
        public void executeAsyncRequest(String requestURI, QueryI query, String nodeId, long delay, long asyncTimeout)
                        throws Exception {

                this.getOwner().scheduleTask(
                                new AbstractTask() {
                                        @Override
                                        public void run() {
                                                try {
                                                        LookupOutboundPort LookupOutboundPort = new LookupOutboundPort(
                                                                        AbstractPort.generatePortURI(),
                                                                        ClientPlugin.this.getOwner());
                                                        LookupOutboundPort.publishPort();

                                                        ClientPlugin.this.getOwner().doPortConnection(
                                                                        LookupOutboundPort.getPortURI(),
                                                                        registryInboundPortURI,
                                                                        LookUpRegistryConnector.class
                                                                                        .getCanonicalName());

                                                        ConnectionInfoI nodeInfo = LookupOutboundPort
                                                                        .findByIdentifier(nodeId);
                                                        RequestingOutboundPort clientRequestingOutboundPort = new RequestingOutboundPort(
                                                                        AbstractPort.generatePortURI(),
                                                                        ClientPlugin.this.getOwner());
                                                        clientRequestingOutboundPort.publishPort();

                                                        RequestI request = new RequestIMPL(requestURI, query, true,
                                                                        new ConnectionInfoImpl(
                                                                                        ClientPlugin.this.clientIdentifer,
                                                                                        new EndPointDescIMPL(
                                                                                                        ClientPlugin.this.clientRequestResultInboundPort
                                                                                                                        .getPortURI(),
                                                                                                        RequestResultCI.class)));

                                                        ClientPlugin.this.getOwner().doPortConnection(
                                                                        clientRequestingOutboundPort.getPortURI(),
                                                                        ((EndPointDescIMPL) nodeInfo.endPointInfo())
                                                                                        .getInboundPortURI(),
                                                                        RequestingConnector.class.getCanonicalName());

                                                        ClientPlugin.this.resultsMap.put(request.requestURI(),
                                                                        new ArrayList<>());
                                                        long ti = ((ClientComponent) ClientPlugin.this.getOwner()).clock
                                                                        .currentInstant().toEpochMilli();
                                                        QueryMetrics metric = new QueryMetrics(0L, 0L, 0L, 0L, 0);
                                                        metric.setStartTime(ti);
                                                        metric.setEndTime(ti + delay + asyncTimeout);
                                                        clientRequestingOutboundPort.executeAsync(request);

                                                        ClientPlugin.this.getOwner()
                                                                        .logMessage("Async request sent to node "
                                                                                        + nodeId + " with URI "
                                                                                        + request.requestURI()
                                                                                        + " at " + Instant.now());

                                                        ClientPlugin.this.queryMetrics.put(requestURI, metric);
                                                        ClientPlugin.this.getOwner().doPortDisconnection(
                                                                        clientRequestingOutboundPort.getPortURI());
                                                        clientRequestingOutboundPort.unpublishPort();
                                                        clientRequestingOutboundPort.destroyPort();
                                                        LookupOutboundPort.unpublishPort();
                                                        LookupOutboundPort.destroyPort();
                                                } catch (Exception e) {
                                                        e.printStackTrace();
                                                }
                                        }
                                }, delay, TimeUnit.NANOSECONDS);

                // after a certain delay the client component will combine the results he got
                // from the nodes and print them and then delete the query and don't wait for
                // more results
                this.getOwner().scheduleTask(
                                new AbstractTask() {
                                        @Override
                                        public void run() {
                                                try {
                                                        List<QueryResultI> results = ClientPlugin.this.resultsMap
                                                                        .get(requestURI);

                                                        if (results == null || results.isEmpty()) {
                                                                return;
                                                        }
                                                        int size = results.size();
                                                        QueryResultI result = results.get(0);
                                                        results.remove(0);
                                                        if (!results.isEmpty()) {
                                                                for (QueryResultI res : results) {
                                                                        ((QueryResultIMPL) result).update(res);
                                                                }
                                                                if (result.isGatherRequest()) {
                                                                        ClientPlugin.this.getOwner()
                                                                                        .logMessage("Gathered size : "
                                                                                                        + result.gatheredSensorsValues()
                                                                                                                        .size());
                                                                } else if (result.isBooleanRequest()) {
                                                                        ClientPlugin.this.getOwner()
                                                                                        .logMessage("Floading size : "
                                                                                                        + result.positiveSensorNodes()
                                                                                                                        .size());
                                                                }
                                                                ClientPlugin.this.getOwner().logMessage(
                                                                                "ASYNC Final Query result , received at : "
                                                                                                + Instant.now()
                                                                                                + " , URI : "
                                                                                                + requestURI
                                                                                                + " : "
                                                                                                + result.toString());
                                                                ClientPlugin.this.resultsMap
                                                                                .remove(requestURI);
                                                                QueryMetrics metric = ClientPlugin.this.queryMetrics
                                                                                .get(requestURI);
                                                                metric.setNbSensors(size);
                                                                ClientPlugin.this.queryMetrics.put(requestURI, metric);

                                                        }
                                                } catch (Exception e) {
                                                        e.printStackTrace();
                                                }
                                        }
                                }, delay + asyncTimeout, TimeUnit.NANOSECONDS);
        }

        /**
         * Accept the result of a request sent by a node component
         * 
         * @param requestURI the URI of the request
         * @param result     the result of the request
         * @throws Exception
         */
        public void acceptRequestResult(String requestURI, QueryResultI result) throws Exception {
                this.getOwner().logMessage(
                                "Received result for request with URI " + requestURI + " at " + Instant.now());
                if (this.resultsMap.containsKey(requestURI)) {
                        // we add the result to the list of results for this request URI
                        this.resultsMap.get(requestURI).add(result);
                        // metrics
                        QueryMetrics metric = this.queryMetrics.get(requestURI);
                        long ti = ((ClientComponent) ClientPlugin.this.getOwner()).clock.currentInstant()
                                        .toEpochMilli();
                        if (metric != null && ti < metric.getEndTime()) {
                                metric.setDuration(ti - metric.getStartTime());
                                // this.queryMetrics.put(requestURI, metric);
                        }
                } else {
                        System.out.println("No request with URI " + requestURI + " found.");
                }
        }

        /**
         * Store the test results in a file
         * 
         * @throws Exception
         */
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
                                        randomAccessFile.writeBytes(
                                                        "RequestURI,StartTime,EndTime,Interval,Duration,nbSensors\n");
                                }

                                // Write each entry to the file
                                for (Map.Entry<String, QueryMetrics> entry : queryMetrics.entrySet()) {
                                        QueryMetrics metrics = entry.getValue();
                                        StringBuilder data = new StringBuilder();
                                        data.append(entry.getKey()).append(",");
                                        data.append(metrics.getStartTime()).append(",");
                                        data.append(metrics.getEndTime()).append(",");
                                        data.append(metrics.getInterval()).append(",");
                                        data.append(metrics.getDuration()).append(",");
                                        data.append(metrics.getNbSensors()).append("\n");

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
}
