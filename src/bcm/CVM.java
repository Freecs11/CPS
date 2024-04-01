package bcm;

import java.lang.reflect.Array;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import bcm.components.ClientComponent;
import bcm.components.NodeComponent;
import bcm.components.RegistryComponent;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.AbstractPort;
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
        protected static final double accelerationFactor = 60.0;

        /** URI of the provider component (convenience). */
        protected static final String NODE_COMPONENT_URI = "node-URI";
        protected static final String NODE2_COMPONENT_URI = "node2-URI";
        protected static final String NODE3_COMPONENT_URI = "node3-URI";
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

        public static Set<NodeComponentInfo> buildMap(int nbNode) {
                // build map of nbNodes , map goes +5 in and -5 in positions to expand
                double initialRange = 100.0;
                double x = 50.0;
                double y = 50.0;
                Set<NodeComponentInfo> nodes = new HashSet<>();
                Stack<Double> stack = new Stack<>();
                stack.push(initialRange);
                // no equals postions allowed
                for (int i = 0; i < nbNode; i++) {
                        double range = stack.pop();
                        if (i % 2 == 0) {
                                x += range;
                                y += range;
                        } else {
                                x -= range;
                                y -= range;
                        }
                        nodes.add(new NodeComponentInfo("node" + i, x, y, range));
                        stack.push(range + 5);
                        stack.push(range - 5);
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
                this.uriRegisterURI = AbstractComponent.createComponent(RegistryComponent.class.getCanonicalName(),
                                new Object[] {
                                                REGISTER_COMPONENT_URI,
                                                1, 1,
                                                LOOKUP_IN_BOUND_PORT_URI,
                                                REGISTER_IN_BOUND_PORT_URI });
                
                // create the node components
                Set<NodeComponentInfo> nodes = buildMap(9);
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
                                                        RegistryComponent.REG_START_INSTANT.plusSeconds(5) });
                        if (i < 7) {
                                assert this.isDeployedComponent(uri);
                                this.toggleTracing(uri);
                                this.toggleLogging(uri);
                                i++;
                        }
                }
                
                // create the client component
                this.uriClientURI = AbstractComponent.createComponent(ClientComponent.class.getCanonicalName(),
                                new Object[] { CLIENT_COMPONENT_URI, LOOKUP_IN_BOUND_PORT_URI,
                                                RegistryComponent.REG_START_INSTANT.plusSeconds(35+nodes.size())
                                }); // to be changed
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
                // Set<NodeComponentInfo> result = CVM.buildMap(10);
                // System.out.println(result.size());
                // System.out.println(result);
        }

}
