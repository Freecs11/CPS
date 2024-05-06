package bcm;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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
        public static final Instant REG_START_INSTANT = CLOCK_START_INSTANT.plusSeconds(1L);

        /** URI of the consumer component (convenience). */
        protected static final String CLIENT_COMPONENT_URI = "client-URI";
        protected static final String REGISTER_COMPONENT_URI = "register-URI";

        protected static final String REGISTER_IN_BOUND_PORT_URI = "register-inbound-port";
        protected static final String LOOKUP_IN_BOUND_PORT_URI = "lookup_inbound-port";
        protected static final String CLIENTS_OUT_BOUND_PORT_URI = "client-outbound-port";

        public CVM() throws Exception {
                super();
        }

        protected String clockURI;

        protected String uriClientURI;

        protected String uriRegisterURI;

        /**
         * Calculate the grid size for a given number of nodes.
         * 
         * @param desiredNodes the number of nodes to place on the grid
         * @return the grid size that can accommodate the desired number of nodes, to be
         *         used for the buildmap method
         */
        public static int calculateGridSize(int desiredNodes) {
                // Minimum grid size starts at 1x1
                int gridSize = 1;

                while (true) {
                        int nodesCount = 0;
                        int nodeId = 1;

                        // Calculate nodes for a given grid size
                        for (int y = 1; y <= gridSize; y++) {
                                if (y % 2 != 0) { // Odd rows
                                        for (int x = 1; x <= gridSize; x += 2) {
                                                nodeId++;
                                        }
                                } else { // Even rows
                                        for (int x = 2; x <= gridSize; x += 2) {
                                                nodeId++;
                                        }
                                }
                        }

                        nodesCount = nodeId - 1; // Correct the last increment

                        // Check if we have enough nodes
                        if (nodesCount >= desiredNodes) {
                                return gridSize; // Return the current grid size
                        }

                        gridSize++; // Increment grid size and try again
                }
        }

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
                                                1, 0,
                                                LOOKUP_IN_BOUND_PORT_URI,
                                                REGISTER_IN_BOUND_PORT_URI,
                                                "registeryPoolURI",
                                                10 });

                // create the node components
                int desiredNumberNodes = 13;
                int gridSize = calculateGridSize(desiredNumberNodes);
                ArrayList<NodeComponentInfo> nodes = buildMap(gridSize);
                int i = 0;

                List<List<Double>> valuesList = new ArrayList<>();

                for (int j = 0; j < desiredNumberNodes + 1; j++) {
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
                                                        REG_START_INSTANT.plusSeconds(5L + i),
                                                        nodes.size(),
                                                        nodes.size(),
                                                        "syncRequestPool_" + node.getName(),
                                                        "asyncRequestPool_" + node.getName(),
                                                        "syncContPool_" + node.getName(),
                                                        "asyncContPool_" + node.getName()
                                        });

                        // if (i < 5) {
                        assert this.isDeployedComponent(uri);
                        this.toggleTracing(uri);
                        this.toggleLogging(uri);
                        // }
                }

                // create the client component
                this.uriClientURI = AbstractComponent.createComponent(ClientComponent.class.getCanonicalName(),
                                new Object[] { CLIENT_COMPONENT_URI, LOOKUP_IN_BOUND_PORT_URI,
                                                REG_START_INSTANT.plusSeconds(100L), 10, 10, "client1"
                                });

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
        }

        @Override
        public void shutdown() throws Exception {
                assert this.allFinalised();

                super.shutdown();
        }

        public static void main(String[] args) {
                try {
                        CVM cvm = new CVM();
                        cvm.startStandardLifeCycle(50000L);
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
