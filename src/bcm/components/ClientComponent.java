package bcm.components;

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

        protected RequestingOutboundPort RequestingOutboundPort;
        protected LookupOutboundPort LookupOutboundPort;

        protected RequestResultInboundPort clientRequestResultInboundPort;

        protected long startAfter;
        protected String registryInboundPortURI;
        protected RequestI request;

        protected AcceleratedClock clock;
        protected Instant startInstant;

        private ConcurrentHashMap<String, List<QueryResultI>> resultsMap;
        private String clientIdentifer = "client1";

        private long asyncTimeout = TimeUnit.SECONDS.toNanos(20L);

        protected ClientComponent(String uri, String registryInboundPortURI) throws Exception {
                super(uri, 5, 5);
                // ---------Init the ports---------
                this.LookupOutboundPort = new LookupOutboundPort(
                                AbstractOutboundPort.generatePortURI(),
                                this);
                this.LookupOutboundPort.publishPort();
                this.registryInboundPortURI = registryInboundPortURI;
                this.RequestingOutboundPort = new RequestingOutboundPort(
                                AbstractOutboundPort.generatePortURI(),
                                this);
                this.RequestingOutboundPort.publishPort();
                this.resultsMap = new ConcurrentHashMap<>();
                this.clientRequestResultInboundPort = new RequestResultInboundPort(this);
                this.clientRequestResultInboundPort.publishPort();
                this.getTracer().setTitle("Client Component");
                this.getTracer().setRelativePosition(1, 1);
                AbstractComponent.checkImplementationInvariant(this);
                AbstractComponent.checkInvariant(this);
        }

        protected ClientComponent(String uri, String registryInboundPortURI, Instant startInstant) throws Exception {
                super(uri, 5, 15);
                // ---------------Init the ports----------------
                this.LookupOutboundPort = new LookupOutboundPort(
                                AbstractOutboundPort.generatePortURI(),
                                this);
                this.LookupOutboundPort.publishPort();
                this.registryInboundPortURI = registryInboundPortURI;
                this.RequestingOutboundPort = new RequestingOutboundPort(
                                AbstractOutboundPort.generatePortURI(),
                                this);
                this.RequestingOutboundPort.publishPort();
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

                // ---------Connection to the clock component---------
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

                this.logMessage("Client component waiting.......");
                this.clock.waitUntilStart(); // wait until the clock is started

                long delayTilStart = this.clock.nanoDelayUntilInstant(this.startInstant);
                this.logMessage("Waiting " + delayTilStart + " ns before starting the client component.");
                this.scheduleTask(
                                nil -> {
                                        this.logMessage("Client " + " component starting...");
                                }, delayTilStart, TimeUnit.NANOSECONDS);

                ConnectionInfoImpl clientInfo = new ConnectionInfoImpl(this.clientIdentifer,
                                new EndPointDescIMPL(this.RequestingOutboundPort.getPortURI(), RequestingCI.class));
                ConnectionInfoImpl clientInfoAsync = new ConnectionInfoImpl(this.clientIdentifer,
                                new EndPointDescIMPL(this.clientRequestResultInboundPort.getPortURI(),
                                                RequestResultCI.class));

                // -------------Boolean Query Test---------------
                OrBooleanExpr res = new OrBooleanExpr(
                                new ConditionalExprBooleanExpr(
                                                new EqualConditionalExpr(new SensorRand("humidity"),
                                                                new ConstantRand(80.0))),
                                new ConditionalExprBooleanExpr(
                                                new EqualConditionalExpr(new SensorRand("temperature"),
                                                                new ConstantRand(60.0))));

                BooleanQuery query2 = new BooleanQuery(res, new EmptyContinuation());

                // -------------------Gather Query Test 1 : Flooding
                // continuation , Sync Request-------------------
                GatherQuery query = new GatherQuery(
                                new RecursiveGather("temperature",
                                                new FinalGather("humidity")),
                                // new FloodingContinuation(new RelativeBase(), 45.0));
                                new DirectionContinuation(3, new RecursiveDirections(Direction.SE,
                                                new FinalDirections(Direction.NE))));
                String nodeIdentifier = "node4";
                RequestI request1 = new RequestIMPL("req1",
                                query,
                                false,
                                clientInfo);
                long delayTilRequest2 = this.clock.nanoDelayUntilInstant(this.startInstant.plusSeconds(40L));
                this.executeSyncRequest(request1, nodeIdentifier, delayTilRequest2);

                // // -------------------Gather Query Test 2 : flooding continuation , Async
                // // Request
                // // -------------------
                RequestI request2 = new RequestIMPL("req2",
                                query,
                                true,
                                clientInfoAsync);
                long delayTilRequest3 = this.clock.nanoDelayUntilInstant(this.startInstant.plusSeconds(40));
                this.executeAsyncRequest(request2, nodeIdentifier, delayTilRequest3);

                // -------------------Gather Query Test 2 : flooding continuation , Async
                // Request
                // -------------------
                // RequestI request6 = new RequestIMPL("req6",
                // query,
                // true,
                // clientInfoAsync);
                // long delayTilRequest6 =
                // this.clock.nanoDelayUntilInstant(this.startInstant.plusSeconds(40L));
                // this.executeAsyncRequest(request6, nodeIdentifier, delayTilRequest6,
                // nodeInfo);

                // // -------------------Gather Query Test 2 : flooding continuation , Async
                // // Request
                // // -------------------
                // RequestI request7 = new RequestIMPL("req7",
                // query,
                // true,
                // clientInfoAsync);
                // long delayTilRequest7 =
                // this.clock.nanoDelayUntilInstant(this.startInstant.plusSeconds(40L));
                // this.executeAsyncRequest(request7, nodeIdentifier, delayTilRequest7,
                // nodeInfo);
                // ------------------- Boolean Query Test 1 : no continuation , Sync Request
                // -------------------
                RequestI request3 = new RequestIMPL("req3",
                                query2,
                                false,
                                clientInfo);
                long delayTilRequest4 = this.clock.nanoDelayUntilInstant(this.startInstant.plusSeconds(60L));

                // this.executeSyncRequest(request3, nodeIdentifier, delayTilRequest4);

                // ------------------- Boolean Query Test 2 : no continuation , Async Request
                // -------------------
                RequestI request4 = new RequestIMPL("req3",
                                query2,
                                true,
                                clientInfoAsync);
                long delayTilRequest5 = this.clock.nanoDelayUntilInstant(this.startInstant.plusSeconds(80L));

                // this.executeAsyncRequest(request4, nodeIdentifier, delayTilRequest5);

                // async request flooding

        }

        private void executeSyncRequest(RequestI request, String nodeId, long delay) {
                this.scheduleTask(
                                new AbstractTask() {
                                        @Override
                                        public void run() {
                                                try {
                                                        ConnectionInfoI nodeInfo = ClientComponent.this.LookupOutboundPort
                                                                        .findByIdentifier(nodeId);
                                                        // System.err.println("NodeInfo: " + nodeInfo.nodeIdentifier());

                                                        RequestingOutboundPort clientRequestingOutboundPort = new RequestingOutboundPort(
                                                                        AbstractOutboundPort.generatePortURI(),
                                                                        ClientComponent.this);
                                                        clientRequestingOutboundPort.publishPort();
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

                                                        } else {
                                                                ClientComponent.this
                                                                                .logMessage("Floading size : " + res
                                                                                                .positiveSensorNodes()
                                                                                                .size());
                                                        }
                                                        ClientComponent.this
                                                                        .logMessage("Query result, sent at : "
                                                                                        + Instant.now()
                                                                                        + " : " + res.toString());
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

        private void executeAsyncRequest(RequestI request, String nodeId, long delay)
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

                                                        // ClientComponent.this.doPortConnection(
                                                        // ClientComponent.this.RequestingOutboundPort
                                                        // .getPortURI(),
                                                        // ((EndPointDescIMPL) nodeInfo.endPointInfo())
                                                        // .getInboundPortURI(),
                                                        // RequestingConnector.class.getCanonicalName());
                                                        // ClientComponent.this.resultsMap.put(request.requestURI(),
                                                        // new ArrayList<>());
                                                        // ClientComponent.this.RequestingOutboundPort
                                                        // .executeAsync(request);
                                                        // ClientComponent.this.logMessage("Async request sent to node "
                                                        // + nodeId + " with URI " + request.requestURI()
                                                        // + " at " + Instant.now());
                                                        // ClientComponent.this.doPortDisconnection(
                                                        // ClientComponent.this.RequestingOutboundPort
                                                        // .getPortURI());

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
                                                                        .get(request.requestURI());
                                                        if (results == null || results.isEmpty()) {
                                                                return;
                                                        }
                                                        QueryResultI result = results.get(0);
                                                        results.remove(0);
                                                        if (results != null) {
                                                                for (QueryResultI res : results) {
                                                                        ((QueryResultIMPL) result).update(res);

                                                                }
                                                                if (result.isGatherRequest()) {
                                                                        ClientComponent.this
                                                                                        .logMessage("Gathered size : "
                                                                                                        + result.gatheredSensorsValues()
                                                                                                                        .size());
                                                                } else {
                                                                        ClientComponent.this
                                                                                        .logMessage("Floading size : "
                                                                                                        + result.positiveSensorNodes()
                                                                                                                        .size());
                                                                }

                                                                ClientComponent.this.logMessage(
                                                                                "Final Query result , received at : "
                                                                                                + Instant.now()
                                                                                                + " , URI : "
                                                                                                + request.requestURI()
                                                                                                + " : "
                                                                                                + result.toString());

                                                                ClientComponent.this.resultsMap
                                                                                .remove(request.requestURI());
                                                        }

                                                } catch (Exception e) {
                                                        e.printStackTrace();
                                                }
                                        }
                                }, delay + asyncTimeout, TimeUnit.NANOSECONDS);

        }

        @Override
        public synchronized void finalise() throws Exception {
                if (this.RequestingOutboundPort.connected()) {
                        this.doPortDisconnection(this.RequestingOutboundPort.getPortURI());
                }
                this.RequestingOutboundPort.unpublishPort();
                if (this.LookupOutboundPort.connected()) {
                        this.doPortDisconnection(this.LookupOutboundPort.getPortURI());
                }
                this.LookupOutboundPort.unpublishPort();
                super.finalise();
                // System.out.println("finalise ClientComponent");
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
