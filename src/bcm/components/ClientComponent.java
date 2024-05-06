package bcm.components;

import java.time.Instant;
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

        protected ClientComponent(String uri, String registryInboundPortURI,
                        int nbThreads, int nbSchedulableThreads, String clientIdentifer) throws Exception {
                super(uri, nbThreads, nbSchedulableThreads);

                // ---------Init the plugin---------
                this.plugin = new ClientPlugin(registryInboundPortURI, clientIdentifer);
                this.plugin.setPluginURI(AbstractOutboundPort.generatePortURI());
                this.installPlugin(plugin);

                this.getTracer().setTitle("Client Component");
                this.getTracer().setRelativePosition(1, 1);
                AbstractComponent.checkImplementationInvariant(this);
                AbstractComponent.checkInvariant(this);
        }

        protected ClientComponent(String uri, String registryInboundPortURI, Instant startInstant,
                        int nbThreads,
                        int nbSchedulableThreads, String clientIdentifer) throws Exception {
                super(uri, nbThreads, nbSchedulableThreads);

                this.startInstant = startInstant;

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
                this.clock = clockPort.getClock(DistributedCVM.CLOCK_URI);
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

                // ------------------- Gather Query Test 1 : Flooding continuation , Sync
                // Request , ===> Result : 26 sensors
                // ----------------------
                GatherQuery query = new GatherQuery(
                                new RecursiveGather("temperature",
                                                new FinalGather("humidity")),
                                new FloodingContinuation(new RelativeBase(), 45.0));
                String nodeIdentifier = "node4";
                long delayTilRequest2 = this.clock.nanoDelayUntilInstant(this.startInstant.plusSeconds(40L));
                this.plugin.executeSyncRequest("req1", query, nodeIdentifier, delayTilRequest2);

                // -------------------Gather Query Test 2 : flooding continuation , Async
                // Request , ===> Result : 26 sensors
                // -------------------
                long delayTilRequest3 = this.clock.nanoDelayUntilInstant(this.startInstant.plusSeconds(40L));
                this.plugin.executeAsyncRequest("req2", query, nodeIdentifier, delayTilRequest3, this.asyncTimeout);

                // -------------------Gather Query Test 3 : direction continuation , Sync
                // Request , ===> Result : 16 sensors
                // -------------------
                GatherQuery query2 = new GatherQuery(
                                new RecursiveGather("temperature",
                                                new FinalGather("humidity")),
                                new DirectionContinuation(3, new RecursiveDirections(Direction.SE,
                                                new FinalDirections(Direction.NE))));

                long delayTilRequest4 = this.clock.nanoDelayUntilInstant(this.startInstant.plusSeconds(50L));
                this.plugin.executeSyncRequest("req3", query2, nodeIdentifier, delayTilRequest4);

                // -------------------Gather Query Test 4 : direction continuation , Async
                // Request
                // -------------------
                long delayTilRequest5 = this.clock.nanoDelayUntilInstant(this.startInstant.plusSeconds(55L));
                this.plugin.executeAsyncRequest("req4", query2, nodeIdentifier, delayTilRequest5, this.asyncTimeout);

                // launch 2 async requests with at the same time
                // we launch the previous gather requests ( flooding and directionnal)
                long delayTilRequest8 = this.clock.nanoDelayUntilInstant(this.startInstant.plusSeconds(60L));
                this.plugin.executeAsyncRequest("req7", query, nodeIdentifier, delayTilRequest8, this.asyncTimeout);
                long delayTilRequest9 = this.clock.nanoDelayUntilInstant(this.startInstant.plusSeconds(60L));
                this.plugin.executeAsyncRequest("req8", query2, nodeIdentifier, delayTilRequest9, this.asyncTimeout);

                // just a little
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
                                                                new ConstantRand(70.0))),
                                new FloodingContinuation(new RelativeBase(), 45.0));

                long delayTilRequest6 = this.clock.nanoDelayUntilInstant(this.startInstant.plusSeconds(70L));
                this.plugin.executeSyncRequest("req5", query3, nodeIdentifier, delayTilRequest6);

                // ------------------- Boolean Query Test 2 : Flooding continuation , Async
                // Request
                // -------------------
                long delayTilRequest7 = this.clock.nanoDelayUntilInstant(this.startInstant.plusSeconds(72L));
                this.plugin.executeAsyncRequest("req6", query3, nodeIdentifier, delayTilRequest7, this.asyncTimeout);

        }

        @Override
        public synchronized void finalise() throws Exception {
                this.plugin.finalise();
                super.finalise();
                try {
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
        }
}
