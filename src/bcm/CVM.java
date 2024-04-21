package bcm;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

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

public class CVM extends AbstractCVM {
        public static final String CLOCK_URI = "CLOCK-SERVER";
        protected static final long TIME_TO_START = 6000L;
        protected static final long unixEpochStartTimeInNanos = TimeUnit.MILLISECONDS.toNanos(
                        System.currentTimeMillis() + TIME_TO_START);
        public static final Instant CLOCK_START_INSTANT = Instant.parse("2024-01-31T09:00:00.00Z");
        protected static final double accelerationFactor = 10.0;
        public static final Instant REG_START_INSTANT = CLOCK_START_INSTANT.plusSeconds(10L);

        /** URI of the consumer component (convenience). */
        protected static final String CLIENT_COMPONENT_URI = "client-URI";
        protected static final String REGISTER_COMPONENT_URI = "register-URI";
        // /** URI of the provider outbound port (simplifies the connection). */
        // protected static final String URIGetterOutboundPortURI = "oport";
        // /** URI of the consumer inbound port (simplifies the connection). */
        // protected static final String URIProviderInboundPortURI = "iport";

        protected static final String REGISTER_IN_BOUND_PORT_URI = "register-inbound-port";
        protected static final String LOOKUP_IN_BOUND_PORT_URI = "lookup_inbound-port";
        protected static final String CLIENTS_OUT_BOUND_PORT_URI = "client-outbound-port";
        protected static final String NODE_TO_REG_OUT_BOUND_PORT_URI = "node2reg-outbound-port";
        protected static final String NODE_IN_BOUND_PORT_URI = "node-inbound-port";
        protected static final String NODE2_TO_REG_OUT_BOUND_PORT_URI = "node2_2reg-outbound-port";
        protected static final String NODE2_IN_BOUND_PORT_URI = "node2-inbound-port";

        protected static final String NODE3_TO_REG_OUT_BOUND_PORT_URI = "node3_reg-outbound-port";
        protected static final String NODE3_IN_BOUND_PORT_URI = "node3-inbound-port";

        public CVM() throws Exception {
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

        public static int getGridSize(int nbNodes) {
                int gridSize = 0;
                int i = 1;
                while (i * i < nbNodes) {
                        i++;
                }
                gridSize = i;
                return gridSize;
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
                                                10, 10,
                                                LOOKUP_IN_BOUND_PORT_URI,
                                                REGISTER_IN_BOUND_PORT_URI });

                // create the node components
                ArrayList<NodeComponentInfo> nodes = buildMap(5);
                int i = 0;
                for (NodeComponentInfo node : nodes) {
                        ArrayList<SensorDataI> data = new ArrayList<>();
                        String uriNode = "uri" + node.getName();
                        data.add(new SensorDataIMPL(uriNode, "temperature", 20.0));
                        data.add(new SensorDataIMPL(uriNode, "humidity", 50.0));
                        data.add(new SensorDataIMPL(uriNode, "pressure", 1013.0));
                        data.add(new SensorDataIMPL(uriNode, "wind", 10.0));

                        String uri = AbstractComponent.createComponent(NodeComponent.class.getCanonicalName(),
                                        new Object[] { uriNode, node.getName(), node.getX(),
                                                        node.getY(),
                                                        node.getRange(),
                                                        REGISTER_IN_BOUND_PORT_URI,
                                                        data,
                                                        REG_START_INSTANT.plusSeconds(20L + i),
                                                        20,
                                                        20,
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
                                                REG_START_INSTANT.plusSeconds(100L)
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
                        CVM cvm = new CVM();
                        cvm.startStandardLifeCycle(1500000L);
                        Thread.sleep(10000L);
                        System.exit(0);
                } catch (Exception e) {
                        throw new RuntimeException(e);
                }
                // ArrayList<NodeComponentInfo> result = CVM.buildMap(5);
                // System.out.println(result.size());
                // System.out.println(result);
        }

}
