package bcm.components;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import bcm.CVM;
import bcm.connectors.LookUpRegistryConnector;
import bcm.connectors.NodeConnector;
import bcm.ports.Client2RegisterOutboundPort;
import bcm.ports.ClientComponentOutboundPort;
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
import implementation.EndPointDescIMPL;
import implementation.RequestIMPL;
import query.ast.BooleanQuery;
import query.ast.ConditionalExprBooleanExpr;
import query.ast.ConstantRand;
import query.ast.DirectionContinuation;
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
        protected ClientComponentOutboundPort client2NodeOutboundPort;
        protected Client2RegisterOutboundPort client2RegistryOutboundPort;
        protected long startAfter;
        protected String registryInboundPortURI;
        protected RequestI request;
        protected AcceleratedClock clock;
        protected Instant startInstant;

        protected ClientComponent(String uri, String registryInboundPortURI) throws Exception {
                super(uri, 1, 0);
                // ---------Init the ports---------
                this.client2RegistryOutboundPort = new Client2RegisterOutboundPort(
                                AbstractOutboundPort.generatePortURI(),
                                this);
                this.client2RegistryOutboundPort.publishPort();
                this.registryInboundPortURI = registryInboundPortURI;
                this.client2NodeOutboundPort = new ClientComponentOutboundPort(
                                AbstractOutboundPort.generatePortURI(),
                                this);
                this.client2NodeOutboundPort.publishPort();

                this.getTracer().setTitle("Client Component");
                this.getTracer().setRelativePosition(1, 1);
                AbstractComponent.checkImplementationInvariant(this);
                AbstractComponent.checkInvariant(this);
        }

        protected ClientComponent(String uri, String registryInboundPortURI, Instant startInstant) throws Exception {
                super(uri, 1, 1);
                // ---------Init the ports---------
                this.client2RegistryOutboundPort = new Client2RegisterOutboundPort(
                                AbstractOutboundPort.generatePortURI(),
                                this);
                this.client2RegistryOutboundPort.publishPort();
                this.registryInboundPortURI = registryInboundPortURI;
                this.client2NodeOutboundPort = new ClientComponentOutboundPort(
                                AbstractOutboundPort.generatePortURI(),
                                this);
                this.client2NodeOutboundPort.publishPort();
                this.startInstant = startInstant;

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
                        this.doPortConnection(this.client2RegistryOutboundPort.getPortURI(),
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

                // -------------------Gather Query Test-------------------
                GatherQuery query = new GatherQuery(
                                new RecursiveGather("temperature",
                                                new FinalGather("humidity")),
                                // new FloodingContinuation(new RelativeBase(), 15.0));
                                // new DirectionContinuation(3, new FinalDirections(Direction.SE)));
                                new DirectionContinuation(3, new RecursiveDirections(Direction.SE,
                                                new FinalDirections(Direction.NE))));

                // -------------Boolean Query Test---------------
                OrBooleanExpr res = new OrBooleanExpr(
                                new ConditionalExprBooleanExpr(
                                                new EqualConditionalExpr(new SensorRand("humidity"),
                                                                new ConstantRand(20.0))),
                                new ConditionalExprBooleanExpr(
                                                new EqualConditionalExpr(new SensorRand("temperature"),
                                                                new ConstantRand(20.0))));

                BooleanQuery query2 = new BooleanQuery(res,
                                new DirectionContinuation(3, new RecursiveDirections(Direction.SE,
                                                new FinalDirections(Direction.NE))));

                // ------Actual Request to look up-------
                this.request = new RequestIMPL("req1",
                                query,
                                // query2,
                                false,
                                null); // change later

                // Finding node with identifier "node1"
                String nodeIdentifier = "node3";
                long delayTilStart1 = this.clock.nanoDelayUntilInstant(this.startInstant.plusSeconds(1));
                this.scheduleTask(new AbstractTask() {
                        @Override
                        public void run() {
                                try {
                                        ConnectionInfoI nodeInfo = ClientComponent.this.client2RegistryOutboundPort
                                                        .findByIdentifier(nodeIdentifier); // modify to return an
                                                                                           // implementation of
                                                                                           // connectionInfo
                                        // System.err.println("NodeInfo: " + nodeInfo.nodeIdentifier());
                                        ClientComponent.this.doPortConnection(
                                                        ClientComponent.this.client2NodeOutboundPort.getPortURI(),
                                                        ((EndPointDescIMPL) nodeInfo.endPointInfo()).getURI(),
                                                        NodeConnector.class.getCanonicalName());
                                        QueryResultI res = ClientComponent.this.client2NodeOutboundPort
                                                        .execute(request);
                                        ClientComponent.this.logMessage("Query result: " + res.toString());
                                        ClientComponent.this.doPortDisconnection(
                                                        ClientComponent.this.client2NodeOutboundPort.getPortURI());
                                } catch (Exception e) {
                                        e.printStackTrace();
                                }
                        }
                }, delayTilStart1, TimeUnit.NANOSECONDS);
                RequestI request2 = new RequestIMPL("req2",
                                query2,
                                false,
                                null);
                long delayTilRequest2 = this.clock.nanoDelayUntilInstant(startInstant.plusSeconds(15));
                this.scheduleTask(new AbstractTask() {
                        @Override
                        public void run() {
                                try {
                                        ConnectionInfoI nodeInfo = ClientComponent.this.client2RegistryOutboundPort
                                                        .findByIdentifier(nodeIdentifier); // modify to return an
                                                                                           // implementation of
                                                                                           // connectionInfo
                                        // System.err.println("NodeInfo: " + nodeInfo.nodeIdentifier());
                                        ClientComponent.this.doPortConnection(
                                                        ClientComponent.this.client2NodeOutboundPort.getPortURI(),
                                                        ((EndPointDescIMPL) nodeInfo.endPointInfo()).getURI(),
                                                        NodeConnector.class.getCanonicalName());
                                        QueryResultI res = ClientComponent.this.client2NodeOutboundPort
                                                        .execute(request2);
                                        ClientComponent.this.logMessage("Query result: " + res.toString());
                                        ClientComponent.this.doPortDisconnection(
                                                        ClientComponent.this.client2NodeOutboundPort.getPortURI());
                                } catch (Exception e) {
                                        e.printStackTrace();
                                }
                        }
                }, delayTilRequest2, TimeUnit.NANOSECONDS);
                long delayTilRequest3 = this.clock.nanoDelayUntilInstant(startInstant.plusSeconds(20));

                GatherQuery query3 = new GatherQuery(
                                new RecursiveGather("temperature",
                                                new FinalGather("humidity")),
                                // new FloodingContinuation(new RelativeBase(), 15.0));
                                // new DirectionContinuation(3, new FinalDirections(Direction.NE)));
                                new DirectionContinuation(3, new RecursiveDirections(Direction.SE,
                                                new FinalDirections(Direction.NE))));

                RequestI request3 = new RequestIMPL("req3",
                                query3,
                                false,
                                null);
                this.scheduleTask(new AbstractTask() {
                        @Override
                        public void run() {
                                try {
                                        ConnectionInfoI nodeInfo = ClientComponent.this.client2RegistryOutboundPort
                                                        .findByIdentifier(nodeIdentifier); // modify to return an
                                                                                           // implementation of
                                                                                           // connectionInfo
                                        // System.err.println("NodeInfo: " + nodeInfo.nodeIdentifier());
                                        ClientComponent.this.doPortConnection(
                                                        ClientComponent.this.client2NodeOutboundPort.getPortURI(),
                                                        ((EndPointDescIMPL) nodeInfo.endPointInfo()).getURI(),
                                                        NodeConnector.class.getCanonicalName());
                                        QueryResultI res = ClientComponent.this.client2NodeOutboundPort
                                                        .execute(request3);
                                        ClientComponent.this.logMessage("Query result: " + res.toString());
                                        ClientComponent.this.doPortDisconnection(
                                                        ClientComponent.this.client2NodeOutboundPort.getPortURI());
                                } catch (Exception e) {
                                        e.printStackTrace();
                                }
                        }
                }, delayTilRequest3, TimeUnit.NANOSECONDS);
                super.execute();

        }

        @Override
        public synchronized void finalise() throws Exception {
                if (this.client2NodeOutboundPort.connected()) {
                        this.doPortDisconnection(this.client2NodeOutboundPort.getPortURI());
                }
                this.client2NodeOutboundPort.unpublishPort();
                if (this.client2RegistryOutboundPort.connected()) {
                        this.doPortDisconnection(this.client2RegistryOutboundPort.getPortURI());
                }
                this.client2RegistryOutboundPort.unpublishPort();
                super.finalise();
                // System.out.println("finalise ClientComponent");
        }
}
