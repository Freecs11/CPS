package bcm.components;

import java.io.FileNotFoundException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import bcm.CVM;
import bcm.connectors.LookUpRegistryConnector;
import bcm.connectors.RequestingConnector;
import bcm.ports.LookupOutboundPort;
import bcm.ports.RequestResultInboundPort;
import bcm.ports.RequestingOutboundPort;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestResultCI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import fr.sorbonne_u.utils.aclocks.ClocksServerCI;
import fr.sorbonne_u.utils.aclocks.ClocksServerConnector;
import fr.sorbonne_u.utils.aclocks.ClocksServerOutboundPort;
import implementation.ConnectionInfoImpl;
import implementation.EndPointDescIMPL;
import implementation.QueryResultIMPL;
import implementation.RequestIMPL;
import query.ast.BooleanQuery;
import query.ast.ConditionalExprBooleanExpr;
import query.ast.ConstantRand;
import query.ast.DirectionContinuation;
import query.ast.EmptyContinuation;
import query.ast.EqualConditionalExpr;
import query.ast.FinalDirections;
import query.ast.FinalGather;
import query.ast.FloodingContinuation;
import query.ast.GatherQuery;
import query.ast.OrBooleanExpr;
import query.ast.RecursiveDirections;
import query.ast.RecursiveGather;
import query.ast.RelativeBase;
import query.ast.SensorRand;

@OfferedInterfaces(offered = { RequestResultCI.class })
@RequiredInterfaces(required = { RequestingCI.class, LookupCI.class, ClocksServerCI.class })
public class ClientComponent extends AbstractComponent {

        // protected RequestingOutboundPort RequestingOutboundPort;

        protected LookupOutboundPort LookupOutboundPort;

        protected RequestResultInboundPort clientRequestResultInboundPort;

        protected long startAfter;
        protected String registryInboundPortURI;

        protected AcceleratedClock clock;
        protected Instant startInstant;

        private ConcurrentHashMap<String, List<QueryResultI>> resultsMap;
        private String clientIdentifer = "client1";

        private long asyncTimeout = TimeUnit.SECONDS.toNanos(20L);

        protected ClientComponent(String uri, String registryInboundPortURI,
                        int nbThreads, int nbSchedulableThreads) throws Exception {
                super(uri, nbThreads, nbSchedulableThreads);

                // ---------Init the ports---------
                this.LookupOutboundPort = new LookupOutboundPort(
                                AbstractOutboundPort.generatePortURI(),
                                this);
                this.LookupOutboundPort.publishPort();
                this.registryInboundPortURI = registryInboundPortURI;
                this.resultsMap = new ConcurrentHashMap<>();
                this.clientRequestResultInboundPort = new RequestResultInboundPort(this);
                this.clientRequestResultInboundPort.publishPort();
                this.getTracer().setTitle("Client Component");
                this.getTracer().setRelativePosition(1, 1);
                AbstractComponent.checkImplementationInvariant(this);
                AbstractComponent.checkInvariant(this);
        }

        protected ClientComponent(String uri, String registryInboundPortURI, Instant startInstant,
                        int nbThreads,
                        int nbSchedulableThreads) throws Exception {
                super(uri, nbThreads, nbSchedulableThreads);

                // ---------------Init the ports----------------
                this.LookupOutboundPort = new LookupOutboundPort(
                                AbstractOutboundPort.generatePortURI(),
                                this);
                this.LookupOutboundPort.publishPort();
                this.registryInboundPortURI = registryInboundPortURI;
                this.startInstant = startInstant;
                this.clientRequestResultInboundPort = new RequestResultInboundPort(this);
                this.resultsMap = new ConcurrentHashMap<>();

                this.clientRequestResultInboundPort.publishPort();

                this.getTracer().setTitle("Client Component");
                this.getTracer().setRelativePosition(1, 1);
                AbstractComponent.checkImplementationInvariant(this);
                AbstractComponent.checkInvariant(this);
        }

        @Override
        public synchronized void start() throws ComponentStartException {
                this.logMessage("starting client component.");
                super.start();
                // ---------Connection to the registry component---------
                try {
                        this.doPortConnection(this.LookupOutboundPort.getPortURI(),
                                        registryInboundPortURI,
                                        LookUpRegistryConnector.class.getCanonicalName());
                } catch (Exception e) {
                        throw new ComponentStartException(e);
                }
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

                // --------- Wait until the clock is started ------------
                this.logMessage("Client component waiting.......");
                this.clock.waitUntilStart(); // wait until the clock is started

                long delayTilStart = this.clock.nanoDelayUntilInstant(this.startInstant);
                this.logMessage("Waiting " + delayTilStart + " ns before starting the client component.");
                this.scheduleTask(
                                nil -> {
                                        this.logMessage("Client " + " component starting...");
                                }, delayTilStart, TimeUnit.NANOSECONDS);

                // ------------------- Gather Query Test 1 : Flooding continuation , Sync
                // Request , ===> Result : 26 sensors
                // ----------------------
                GatherQuery query = new GatherQuery(
                                new RecursiveGather("temperature",
                                                new FinalGather("humidity")),
                                new FloodingContinuation(new RelativeBase(), 45.0));
                String nodeIdentifier = "node4";
                long delayTilRequest2 = this.clock.nanoDelayUntilInstant(this.startInstant.plusSeconds(40L));
                this.executeSyncRequest("req1", query, nodeIdentifier, delayTilRequest2);

                // -------------------Gather Query Test 2 : flooding continuation , Async
                // Request , ===> Result : 26 sensors
                // -------------------
                long delayTilRequest3 = this.clock.nanoDelayUntilInstant(this.startInstant.plusSeconds(40L));
                this.executeAsyncRequest("req2", query, nodeIdentifier, delayTilRequest3);

                // -------------------Gather Query Test 3 : direction continuation , Sync
                // Request , ===> Result : 16 sensors
                // -------------------
                GatherQuery query2 = new GatherQuery(
                                new RecursiveGather("temperature",
                                                new FinalGather("humidity")),
                                new DirectionContinuation(3, new RecursiveDirections(Direction.SE,
                                                new FinalDirections(Direction.NE))));

                long delayTilRequest4 = this.clock.nanoDelayUntilInstant(this.startInstant.plusSeconds(50L));
                this.executeSyncRequest("req3", query2, nodeIdentifier, delayTilRequest4);

                // -------------------Gather Query Test 4 : direction continuation , Async
                // Request
                // -------------------
                long delayTilRequest5 = this.clock.nanoDelayUntilInstant(this.startInstant.plusSeconds(55L));
                this.executeAsyncRequest("req4", query2, nodeIdentifier, delayTilRequest5);

                boolean req = true;
                if (req) {
                        return;
                }
                // ------------------- Boolean Query Test 1 : Flooding continuation , Sync
                // Request
                // -------------------
                BooleanQuery query3 = new BooleanQuery(
                                new ConditionalExprBooleanExpr(
                                                new EqualConditionalExpr(new SensorRand("humidity"),
                                                                new ConstantRand(80.0))),
                                new FloodingContinuation(new RelativeBase(), 45.0));

                long delayTilRequest6 = this.clock.nanoDelayUntilInstant(this.startInstant.plusSeconds(50L));
                this.executeSyncRequest("req5", query3, nodeIdentifier, delayTilRequest6);

                // ------------------- Boolean Query Test 2 : Flooding continuation , Async
                // Request
                // -------------------
                long delayTilRequest7 = this.clock.nanoDelayUntilInstant(this.startInstant.plusSeconds(52L));
                this.executeAsyncRequest("req6", query3, nodeIdentifier, delayTilRequest7);

        }

        /**
         * Execute a sync request to a node with a certain delay to launch the request
         * 
         * @param request the request to be sent
         * @param nodeId  the id of the node to send the request to
         * @param delay   the delay to wait before sending the request
         */
        private void executeSyncRequest(String requestURI, QueryI query, String nodeId, long delay) {
                this.scheduleTask(
                                new AbstractTask() {
                                        @Override
                                        public void run() {
                                                try {
                                                        ConnectionInfoI nodeInfo = ClientComponent.this.LookupOutboundPort
                                                                        .findByIdentifier(nodeId);
                                                        RequestingOutboundPort clientRequestingOutboundPort = new RequestingOutboundPort(
                                                                        AbstractOutboundPort.generatePortURI(),
                                                                        ClientComponent.this);
                                                        clientRequestingOutboundPort.publishPort();

                                                        RequestI request = new RequestIMPL(requestURI, query, false,
                                                                        new ConnectionInfoImpl(
                                                                                        ClientComponent.this.clientIdentifer,
                                                                                        new EndPointDescIMPL(
                                                                                                        clientRequestingOutboundPort
                                                                                                                        .getPortURI(),
                                                                                                        RequestingCI.class)));

                                                        ClientComponent.this.doPortConnection(
                                                                        clientRequestingOutboundPort.getPortURI(),
                                                                        ((EndPointDescIMPL) nodeInfo.endPointInfo())
                                                                                        .getInboundPortURI(),
                                                                        RequestingConnector.class.getCanonicalName());
                                                        QueryResultI res = clientRequestingOutboundPort
                                                                        .execute(request);
                                                        if (res.isGatherRequest()) {
                                                                ClientComponent.this
                                                                                .logMessage("Gathered size : " + res
                                                                                                .gatheredSensorsValues()
                                                                                                .size()
                                                                                                + " for request with URI "
                                                                                                + request.requestURI());

                                                        } else if (res.isBooleanRequest()) {
                                                                ClientComponent.this
                                                                                .logMessage("Floading size : " + res
                                                                                                .positiveSensorNodes()
                                                                                                .size());
                                                        }
                                                        ClientComponent.this
                                                                        .logMessage("SYNC Query result, sent at : "
                                                                                        + Instant.now()
                                                                                        + " , URI : "
                                                                                        + request.requestURI()
                                                                                        + " : "
                                                                                        + res.toString());
                                                        ClientComponent.this.doPortDisconnection(
                                                                        clientRequestingOutboundPort.getPortURI());
                                                        clientRequestingOutboundPort.unpublishPort();
                                                        clientRequestingOutboundPort.destroyPort();
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
        private void executeAsyncRequest(String requestURI, QueryI query, String nodeId, long delay)
                        throws Exception {

                this.scheduleTask(
                                new AbstractTask() {
                                        @Override
                                        public void run() {
                                                try {
                                                        // implementation of
                                                        // connectionInfo
                                                        // System.err.println("NodeInfo: " + nodeInfo.nodeIdentifier());

                                                        ConnectionInfoI nodeInfo = ClientComponent.this.LookupOutboundPort
                                                                        .findByIdentifier(nodeId);
                                                        RequestingOutboundPort clientRequestingOutboundPort = new RequestingOutboundPort(
                                                                        AbstractOutboundPort.generatePortURI(),
                                                                        ClientComponent.this);
                                                        clientRequestingOutboundPort.publishPort();

                                                        RequestI request = new RequestIMPL(requestURI, query, true,
                                                                        new ConnectionInfoImpl(
                                                                                        ClientComponent.this.clientIdentifer,
                                                                                        new EndPointDescIMPL(
                                                                                                        ClientComponent.this.clientRequestResultInboundPort
                                                                                                                        .getPortURI(),
                                                                                                        RequestResultCI.class)));

                                                        ClientComponent.this.doPortConnection(
                                                                        clientRequestingOutboundPort.getPortURI(),
                                                                        ((EndPointDescIMPL) nodeInfo.endPointInfo())
                                                                                        .getInboundPortURI(),
                                                                        RequestingConnector.class.getCanonicalName());

                                                        ClientComponent.this.resultsMap.put(request.requestURI(),
                                                                        new ArrayList<>());
                                                        clientRequestingOutboundPort.executeAsync(request);
                                                        ClientComponent.this.logMessage("Async request sent to node "
                                                                        + nodeId + " with URI " + request.requestURI()
                                                                        + " at " + Instant.now());

                                                        ClientComponent.this.doPortDisconnection(
                                                                        clientRequestingOutboundPort.getPortURI());
                                                        clientRequestingOutboundPort.unpublishPort();
                                                        clientRequestingOutboundPort.destroyPort();
                                                } catch (Exception e) {
                                                        e.printStackTrace();
                                                }
                                        }
                                }, delay, TimeUnit.NANOSECONDS);

                // after a certain delay the client component will combine the results he got
                // from the nodes and print them and then delete the query and don't wait for
                // more results
                this.scheduleTask(
                                new AbstractTask() {
                                        @Override
                                        public void run() {
                                                try {
                                                        List<QueryResultI> results = ClientComponent.this.resultsMap
                                                                        .get(requestURI);
                                                        if (results == null || results.isEmpty()) {
                                                                return;
                                                        }
                                                        QueryResultI result = results.get(0);
                                                        results.remove(0);
                                                        if (!results.isEmpty()) {
                                                                for (QueryResultI res : results) {
                                                                        ((QueryResultIMPL) result).update(res);
                                                                }
                                                                if (result.isGatherRequest()) {
                                                                        ClientComponent.this
                                                                                        .logMessage("Gathered size : "
                                                                                                        + result.gatheredSensorsValues()
                                                                                                                        .size());
                                                                } else if (result.isBooleanRequest()) {
                                                                        ClientComponent.this
                                                                                        .logMessage("Floading size : "
                                                                                                        + result.positiveSensorNodes()
                                                                                                                        .size());
                                                                }
                                                                ClientComponent.this.logMessage(
                                                                                "ASYNC Final Query result , received at : "
                                                                                                + Instant.now()
                                                                                                + " , URI : "
                                                                                                + requestURI
                                                                                                + " : "
                                                                                                + result.toString());
                                                                ClientComponent.this.resultsMap
                                                                                .remove(requestURI);
                                                        }
                                                } catch (Exception e) {
                                                        e.printStackTrace();
                                                }
                                        }
                                }, delay + asyncTimeout, TimeUnit.NANOSECONDS);
        }

        @Override
        public synchronized void finalise() throws Exception {
                if (this.LookupOutboundPort.connected()) {
                        this.doPortDisconnection(this.LookupOutboundPort.getPortURI());
                }
                this.LookupOutboundPort.unpublishPort();

                super.finalise();
                try {
                        this.printExecutionLogOnFile("logRegistry");
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        public void acceptRequestResult(String requestURI, QueryResultI result) throws Exception {
                this.logMessage("Received result for request with URI " + requestURI + " at " + Instant.now());
                if (this.resultsMap.containsKey(requestURI)) {
                        // we add the result to the list of results for this request URI
                        this.resultsMap.get(requestURI).add(result);
                } else {
                        System.out.println("No request with URI " + requestURI + " found.");
                }
        }
}
