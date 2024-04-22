package tests;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import bcm.components.ClientComponent;
import bcm.components.NodeComponent;
import bcm.components.RegistryComponent;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.helpers.CVMDebugModes;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import implementation.SensorDataIMPL;
import utils.NodeComponentInfo;

public class CVMMockup extends AbstractCVM {
        public static final String CLOCK_URI = "CLOCK-SERVER";
        protected static final long TIME_TO_START = 6000L;
        protected static final long unixEpochStartTimeInNanos = TimeUnit.MILLISECONDS.toNanos(
                        System.currentTimeMillis() + TIME_TO_START);
        public static final Instant CLOCK_START_INSTANT = Instant.parse("2024-01-31T09:00:00.00Z");
        protected static final double accelerationFactor = 10.0;
        public static final Instant REG_START_INSTANT = CLOCK_START_INSTANT.plusSeconds(1L);

        /** URI of the consumer component (convenience). */
        protected static final String CLIENT_COMPONENT_URI = "client-URI";
        protected static final String REGISTER_COMPONENT_URI = "register-URI";

        protected static final String REGISTER_IN_BOUND_PORT_URI = "register-inbound-port";
        protected static final String LOOKUP_IN_BOUND_PORT_URI = "lookup_inbound-port";
        protected static final String CLIENTS_OUT_BOUND_PORT_URI = "client-outbound-port";

        public CVMMockup() throws Exception {
                super();
        }

        protected String clockURI;
        /**
         * Reference to the provider component to share between deploy
         * and shutdown.
         */
        // protected String uriNodeURI;
        // protected String uriNode2URI;
        // protected String uriNode3URI;
        // protected String uriNode4URI;
        // protected String uriNode5URI;
        // protected String uriNode6URI;
        /**
         * Reference to the consumer component to share between deploy
         * and shutdown.
         */
        protected String uriClientURI;

        protected String uriRegisterURI;

        public static ArrayList<NodeComponentInfo> buildMap(int gridSize) {
                ArrayList<NodeComponentInfo> nodes = new ArrayList<>();
                int nodeId = 1;
                for (int y = 1; y <= gridSize; y++) {
                        // Itération pour les lignes impaires de la grille
                        if (y % 2 != 0) {
                                for (int x = 1; x <= gridSize; x += 2) {
                                        nodes.add(new NodeComponentInfo("node" + nodeId, (double) x, (double) y, 10.0));
                                        nodeId++;
                                }
                        } else {
                                // Itération pour les lignes paires de la grille
                                for (int x = 2; x <= gridSize; x += 2) {
                                        nodes.add(new NodeComponentInfo("node" + nodeId, (double) x, (double) y, 10.0));
                                        nodeId++;
                                }
                        }
                }
                return nodes;
        }

        @Override
        public void deploy() throws Exception {
                assert !this.deploymentDone();

                // ---------------------------------------------------------------------
                // Configuration phase
                // ---------------------------------------------------------------------

                // debugging mode configuration; comment and uncomment the line to see
                // the difference
                AbstractCVM.DEBUG_MODE.add(CVMDebugModes.LIFE_CYCLE);
                AbstractCVM.DEBUG_MODE.add(CVMDebugModes.INTERFACES);
                AbstractCVM.DEBUG_MODE.add(CVMDebugModes.PORTS);
                AbstractCVM.DEBUG_MODE.add(CVMDebugModes.CONNECTING);
                AbstractCVM.DEBUG_MODE.add(CVMDebugModes.CALLING);
                AbstractCVM.DEBUG_MODE.add(CVMDebugModes.EXECUTOR_SERVICES);
                this.clockURI = AbstractComponent.createComponent(ClocksServer.class.getCanonicalName(),
                                new Object[] {
                                                CLOCK_URI,
                                                unixEpochStartTimeInNanos,
                                                CLOCK_START_INSTANT,
                                                accelerationFactor
                                });

                // ---------------------------------------------------------------------
                // Creation phase
                // ---------------------------------------------------------------------
                // Register is starting directly with no link to the clock
                this.uriRegisterURI = AbstractComponent.createComponent(RegistryComponent.class.getCanonicalName(),
                                new Object[] {
                                                REGISTER_COMPONENT_URI,
                                                15, 15,
                                                LOOKUP_IN_BOUND_PORT_URI,
                                                REGISTER_IN_BOUND_PORT_URI,
                                                "registeryPoolURI",
                                                10 });

                // create the node components
                ArrayList<NodeComponentInfo> nodes = buildMap(5);
                int i = 0;

                List<List<Double>> valuesList = new ArrayList<>();

                for (int j = 0; j < 14; j++) {
                        List<Double> values = new ArrayList<>();
                        values.add(10 * j + 1.0);
                        values.add(10 * j + 2.0);
                        values.add(10 * j + 3.0);
                        values.add(10 * j + 4.0);
                        valuesList.add(values);
                }

                for (NodeComponentInfo node : nodes) {
                        ArrayList<SensorDataI> data = new ArrayList<>();
                        String uriNode = "uri" + node.getName();
                        data.add(new SensorDataIMPL(uriNode, "temperature", valuesList.get(i % 13).get(0)));
                        data.add(new SensorDataIMPL(uriNode, "humidity", valuesList.get(i % 13).get(1)));
                        data.add(new SensorDataIMPL(uriNode, "pressure", valuesList.get(i % 13).get(2)));
                        data.add(new SensorDataIMPL(uriNode, "wind", valuesList.get(i % 13).get(3)));

                        String uri = AbstractComponent.createComponent(NodeComponent.class.getCanonicalName(),
                                        new Object[] { uriNode, node.getName(), node.getX(),
                                                        node.getY(),
                                                        node.getRange(),
                                                        REGISTER_IN_BOUND_PORT_URI,
                                                        data,
                                                        REG_START_INSTANT.plusSeconds(5L),
                                                        nodes.size(),
                                                        nodes.size(),
                                                        "aysncPool_" + node.getName(),
                                                        "syncPool_" + node.getName()
                                        });

                        // if (i < 5) {
                        assert this.isDeployedComponent(uri);
                        this.toggleTracing(uri);
                        this.toggleLogging(uri);
                        i += 5;
                        // }
                }

                // create the client component
                this.uriClientURI = AbstractComponent.createComponent(ClientComponent.class.getCanonicalName(),
                                new Object[] { CLIENT_COMPONENT_URI, LOOKUP_IN_BOUND_PORT_URI,
                                                REG_START_INSTANT.plusSeconds(100L), 10, 10
                                });
                // to be changed
                // ---------------------------------------------------------------------
                // Deployment phase
                // ---------------------------------------------------------------------
                assert this.isDeployedComponent(this.uriClientURI);
                this.toggleTracing(this.uriClientURI);
                this.toggleLogging(this.uriClientURI);

                assert this.isDeployedComponent(this.uriRegisterURI);
                this.toggleTracing(this.uriRegisterURI);
                this.toggleLogging(this.uriRegisterURI);

                // ---------------------------------------------------------------------
                // Deployment done
                // ---------------------------------------------------------------------
                super.deploy();

        }

        @Override
        public void finalise() throws Exception {
                super.finalise();
                // System.out.println("CVM finalise");
        }

        @Override
        public void shutdown() throws Exception {
                assert this.allFinalised();

                super.shutdown();
                // System.out.println("CVM shutdown");
        }

        public static void main(String[] args) {
                try {
                        CVMMockup cvm = new CVMMockup();
                        cvm.startStandardLifeCycle(70000L);
                        Thread.sleep(10000L);
                        System.err.println("End of the simulation");
                        System.err.println(
                                        "------------------------------TESTING INTEGRATION---------------------------------");
                        Result result = JUnitCore.runClasses(IntegrationTests.class);

                        if (result.wasSuccessful()) {
                                System.err.println("All tests passed.");
                        } else {
                                System.err.println("Some tests failed.");
                                for (Failure failure : result.getFailures()) {
                                        System.err.println(failure.toString());
                                }
                        }
                        System.exit(0);
                } catch (Exception e) {
                        throw new RuntimeException(e);
                }
                // ArrayList<NodeComponentInfo> result = CVM.buildMap(5);
                // System.out.println(result.size());
                // System.out.println(result);
        }

}
