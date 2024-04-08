package bcm.components;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import implementation.RequestIMPL;
import query.ast.BooleanQuery;
import query.ast.ConditionalExprBooleanExpr;
import query.ast.ConstantRand;
import query.ast.EmptyContinuation;
import query.ast.EqualConditionalExpr;
import query.ast.FinalGather;
import query.ast.FloodingContinuation;
import query.ast.GatherQuery;
import query.ast.OrBooleanExpr;
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

        private Map<String, List<QueryResultI>> resultsMap;
        private String clientIdentifer = "client1";

        protected ClientComponent(String uri, String registryInboundPortURI) throws Exception {
                super(uri, 1, 0);
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
                this.resultsMap = new HashMap<>();
                this.clientRequestResultInboundPort = new RequestResultInboundPort(this);
                this.clientRequestResultInboundPort.publishPort();
                this.getTracer().setTitle("Client Component");
                this.getTracer().setRelativePosition(1, 1);
                AbstractComponent.checkImplementationInvariant(this);
                AbstractComponent.checkInvariant(this);
        }

        protected ClientComponent(String uri, String registryInboundPortURI, Instant startInstant) throws Exception {
                super(uri, 1, 1);
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
                this.startInstant = startInstant;
                this.clientRequestResultInboundPort = new RequestResultInboundPort(this);
                this.resultsMap = new HashMap<>();

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
                long delayTilStart = this.clock.nanoDelayUntilInstant(this.startInstant);
                this.logMessage("Waiting " + delayTilStart + " ns before starting the client component.");
                this.scheduleTask(
                                nil -> {
                                        this.logMessage("Client " + " component starting...");
                                }, delayTilStart, TimeUnit.NANOSECONDS);

                ConnectionInfoImpl clientInfo = new ConnectionInfoImpl(this.clientIdentifer,
                                new EndPointDescIMPL(this.RequestingOutboundPort.getPortURI(), RequestingCI.class));

                // -------------------Gather Query Test-------------------
                GatherQuery query = new GatherQuery(
                                new RecursiveGather("temperature",
                                                new FinalGather("humidity")),
                                // new EmptyContinuation());
                                new FloodingContinuation(new RelativeBase(), 45.0));
                // new DirectionContinuation(15, new FinalDirections(Direction.SE)));
                // new DirectionContinuation(8, new RecursiveDirections(Direction.NW,
                // new FinalDirections(Direction.NE))));

                // -------------Boolean Query Test---------------
                OrBooleanExpr res = new OrBooleanExpr(
                                new ConditionalExprBooleanExpr(
                                                new EqualConditionalExpr(new SensorRand("humidity"),
                                                                new ConstantRand(20.0))),
                                new ConditionalExprBooleanExpr(
                                                new EqualConditionalExpr(new SensorRand("temperature"),
                                                                new ConstantRand(20.0))));

                BooleanQuery query2 = new BooleanQuery(res,
                                // new DirectionContinuation(3, new RecursiveDirections(Direction.SE,
                                // new FinalDirections(Direction.NE))));
                                new EmptyContinuation());
                // ------Actual Request to look up-------
                this.request = new RequestIMPL("req1",
                                query2,
                                // query2,
                                false,
                                clientInfo); // change later

                // Finding node with identifier "node1"
                String nodeIdentifier = "node3";
                long delayTilStart1 = this.clock.nanoDelayUntilInstant(this.startInstant.plusSeconds(1));
                long waitForResponse = this.clock.nanoDelayUntilInstant(this.startInstant.plusSeconds(20));

                RequestI request2 = new RequestIMPL("req2",
                                query,
                                false,
                                null);
                long delayTilRequest2 = this.clock.nanoDelayUntilInstant(startInstant.plusSeconds(10));
                this.scheduleTask(new AbstractTask() {
                        @Override
                        public void run() {
                                try {
                                        ConnectionInfoI nodeInfo = ClientComponent.this.LookupOutboundPort
                                                        .findByIdentifier(nodeIdentifier); // modify to return an
                                        // implementation of
                                        // connectionInfo
                                        // System.err.println("NodeInfo: " + nodeInfo.nodeIdentifier());
                                        ClientComponent.this.doPortConnection(
                                                        ClientComponent.this.RequestingOutboundPort.getPortURI(),
                                                        ((EndPointDescIMPL) nodeInfo.endPointInfo())
                                                                        .getInboundPortURI(),
                                                        RequestingConnector.class.getCanonicalName());
                                        QueryResultI res = ClientComponent.this.RequestingOutboundPort
                                                        .execute(request2);
                                        ClientComponent.this.logMessage("Query result: " + res.toString());
                                        ClientComponent.this.doPortDisconnection(
                                                        ClientComponent.this.RequestingOutboundPort.getPortURI());
                                } catch (Exception e) {
                                        e.printStackTrace();
                                }
                        }
                }, delayTilRequest2, TimeUnit.NANOSECONDS);
                long delayTilRequest3 = this.clock.nanoDelayUntilInstant(startInstant.plusSeconds(20));

                // this.scheduleTask(
                // new AbstractTask() {
                // @Override
                // public void run() {
                // try {
                // ConnectionInfoI nodeInfo = ClientComponent.this.client2RegistryOutboundPort
                // .findByIdentifier(nodeIdentifier); // modify to
                // // return an
                // // implementation of
                // // connectionInfo
                // // System.err.println("NodeInfo: " + nodeInfo.nodeIdentifier());
                // ClientComponent.this.doPortConnection(
                // ClientComponent.this.client2NodeOutboundPort
                // .getPortURI(),
                // ((EndPointDescIMPL) nodeInfo.endPointInfo())
                // .getURI(),
                // NodeConnector.class.getCanonicalName());
                // ClientComponent.this.client2NodeOutboundPort
                // .executeAsync(request);
                // // ClientComponent.this.logMessage("Query result: " +
                // // res.toString());
                // ClientComponent.this.doPortDisconnection(
                // ClientComponent.this.client2NodeOutboundPort
                // .getPortURI());
                // ClientComponent.this.resultsMap.put("req1", new ArrayList<>());
                // } catch (Exception e) {
                // e.printStackTrace();
                // }
                // }
                // }, delayTilStart1, TimeUnit.NANOSECONDS);

                // this.scheduleTask(
                // new AbstractTask() {
                // @Override
                // public void run() {
                // try {
                // List<QueryResultI> results = ClientComponent.this.resultsMap
                // .get("req1");
                // if (results.isEmpty()) {
                // return;
                // }
                // QueryResultIMPL result = (QueryResultIMPL) results.get(0);
                // results.remove(0);
                // if (results != null) {
                // for (QueryResultI res : results) {
                // result.update(res);
                // }
                // ClientComponent.this.logMessage(
                // "Final Query result: "
                // + result.toString());
                // }
                // } catch (Exception e) {
                // e.printStackTrace();
                // }
                // }
                // }, waitForResponse, TimeUnit.NANOSECONDS);

                // GatherQuery query3 = new GatherQuery(
                // new RecursiveGather("temperature",
                // new FinalGather("humidity")),
                // // new FloodingContinuation(new RelativeBase(), 15.0));
                // // new DirectionContinuation(3, new FinalDirections(Direction.NE)));
                // new DirectionContinuation(3, new RecursiveDirections(Direction.SE,
                // new FinalDirections(Direction.NE))));

                // RequestI request3 = new RequestIMPL("req3",
                // query3,
                // false,
                // null);
                // this.scheduleTask(new AbstractTask() {
                // @Override
                // public void run() {
                // try {
                // ConnectionInfoI nodeInfo = ClientComponent.this.client2RegistryOutboundPort
                // .findByIdentifier(nodeIdentifier); // modify to return an
                // // implementation of
                // // connectionInfo
                // // System.err.println("NodeInfo: " + nodeInfo.nodeIdentifier());
                // ClientComponent.this.doPortConnection(
                // ClientComponent.this.client2NodeOutboundPort.getPortURI(),
                // ((EndPointDescIMPL) nodeInfo.endPointInfo()).getURI(),
                // NodeConnector.class.getCanonicalName());
                // QueryResultI res = ClientComponent.this.client2NodeOutboundPort
                // .execute(request3);
                // ClientComponent.this.logMessage("Query result: " + res.toString());
                // ClientComponent.this.doPortDisconnection(
                // ClientComponent.this.client2NodeOutboundPort.getPortURI());
                // } catch (Exception e) {
                // e.printStackTrace();
                // }
                // }
                // }, delayTilRequest3, TimeUnit.NANOSECONDS);
                // super.execute();

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
                if (this.resultsMap.containsKey(requestURI)) {
                        List<QueryResultI> results = this.resultsMap.get(requestURI);
                        results.add(result);
                        this.resultsMap.put(requestURI, results);
                        // this.logMessage("Request result received: " + ((QueryResultIMPL)
                        // result).toString());
                } else {
                        ArrayList<QueryResultI> results = new ArrayList<>();
                        results.add(result);
                        this.resultsMap.put(requestURI, results);
                        // this.logMessage("Request result received: " + ((QueryResultIMPL)
                        // result).toString());
                }
        }
}
