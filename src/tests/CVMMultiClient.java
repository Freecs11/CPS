package tests;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.channels.FileChannel;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import bcm.components.ClientComponent;
import bcm.components.NodeComponent;
import bcm.components.RegistryComponent;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.helpers.CVMDebugModes;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import implementation.SensorDataIMPL;
import utils.NodeComponentInfo;

public class CVMMultiClient extends AbstractCVM {
    public static final String CLOCK_URI = "CLOCK-SERVER";
    protected static final long TIME_TO_START = 7000L;
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

    public CVMMultiClient() throws Exception {
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
        // clean up CSV files
        // ---------------------------------------------------------------------
        this.cleanUpFiles();
        // ---------------------------------------------------------------------
        // Creation phase
        // ---------------------------------------------------------------------
        // Register is starting directly with no link to the clock
        this.uriRegisterURI = AbstractComponent.createComponent(RegistryComponent.class.getCanonicalName(),
                new Object[] {
                        REGISTER_COMPONENT_URI,
                        50, 50,
                        LOOKUP_IN_BOUND_PORT_URI,
                        REGISTER_IN_BOUND_PORT_URI,
                        "registeryPoolURI",
                        50 });
        // create the node components
        int desiredNumberNodes = 50;
        int gridSize = calculateGridSize(desiredNumberNodes);
        ArrayList<NodeComponentInfo> nodes = buildMap(gridSize);
        int i = 0;
        List<List<Double>> valuesList = createValuesList(desiredNumberNodes);

        while (i < desiredNumberNodes) {
            NodeComponentInfo node = nodes.get(i);
            ArrayList<SensorDataI> data = createSensorDataForNode(node, valuesList, i);
            String uri = AbstractComponent.createComponent(NodeComponent.class.getCanonicalName(),
                    new Object[] { node.getName(), node.getName(), node.getX(),
                            node.getY(),
                            node.getRange(),
                            REGISTER_IN_BOUND_PORT_URI,
                            data,
                            REG_START_INSTANT.plusSeconds(1L + i),
                            desiredNumberNodes * 2,
                            desiredNumberNodes * 2,
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
            i++;
        }
        // Query
        // HashMap<String, List<QueryI>> queries = new HashMap<>();
        // queries.put("node4", Queries.queries);
        List<Long> intervals = new ArrayList<>();
        intervals.add(100L);
        intervals.add(90L);
        intervals.add(80L);
        intervals.add(70L);
        intervals.add(60L);
        intervals.add(50L);
        intervals.add(40L);
        intervals.add(30L);
        intervals.add(20L);

        // HashMap<String, List<QueryI>> queriesClient = new HashMap<>();
        // queriesClient.put("node4", Queries.queries1);

        // create the client component
        // this.uriClientURI =
        // AbstractComponent.createComponent(ClientComponent.class.getCanonicalName(),
        // new Object[] { CLIENT_COMPONENT_URI, LOOKUP_IN_BOUND_PORT_URI,
        // REG_START_INSTANT.plusSeconds(100L), 10, 10, "client1"
        // });
        for (int j = 0; j < 5; j++) {
            int randNode = (int) (Math.random() * (nodes.size() / 2));
            HashMap<String, List<QueryI>> queriesClient = new HashMap<>();
            queriesClient.put("node" + randNode, Queries.queries1);

            String uri = AbstractComponent.createComponent(ClientComponent.class.getCanonicalName(),
                    new Object[] { CLIENT_COMPONENT_URI + (j + 1), LOOKUP_IN_BOUND_PORT_URI,
                            REG_START_INSTANT.plusSeconds(150L + nodes.size()),
                            desiredNumberNodes,
                            desiredNumberNodes,
                            "client" + (j + 1),
                            queriesClient,
                            intervals,
                            true
                    });
            assert this.isDeployedComponent(uri);
            this.toggleTracing(uri);
            this.toggleLogging(uri);
        }

        // ---------------------------------------------------------------------
        // Deployment phase
        // ---------------------------------------------------------------------
        // assert this.isDeployedComponent(this.uriClientURI);
        // this.toggleTracing(this.uriClientURI);
        // this.toggleLogging(this.uriClientURI);

        assert this.isDeployedComponent(this.uriRegisterURI);
        this.toggleTracing(this.uriRegisterURI);
        this.toggleLogging(this.uriRegisterURI);

        // ---------------------------------------------------------------------
        // Deployment done
        // ---------------------------------------------------------------------
        super.deploy();

    }

    private List<List<Double>> createValuesList(int size) {
        List<List<Double>> valuesList = new ArrayList<>();
        for (int j = 0; j < size; j++) {
            List<Double> values = new ArrayList<>();
            values.add(10 * j + 1.0);
            values.add(10 * j + 2.0);
            values.add(10 * j + 3.0);
            values.add(10 * j + 4.0);
            valuesList.add(values);
        }
        return valuesList;
    }

    private ArrayList<SensorDataI> createSensorDataForNode(NodeComponentInfo node, List<List<Double>> valuesList,
            int index) {
        ArrayList<SensorDataI> data = new ArrayList<>();
        String uriNode = "uri" + node.getName();
        data.add(new SensorDataIMPL(uriNode, "temperature", valuesList.get(index % valuesList.size()).get(0)));
        data.add(new SensorDataIMPL(uriNode, "humidity", valuesList.get(index % valuesList.size()).get(1)));
        data.add(new SensorDataIMPL(uriNode, "pressure", valuesList.get(index % valuesList.size()).get(2)));
        data.add(new SensorDataIMPL(uriNode, "wind", valuesList.get(index % valuesList.size()).get(3)));
        return data;
    }

    private void cleanUpFiles() {
        // clean up the testResult.csv file
        // need to flush the file and then write the header
        // RequestURI,StartTime,EndTime,Interval,Duration
        File file = new File("testResults.csv");
        try {
            FileWriter writer = new FileWriter(file);
            writer.write("RequestURI,StartTime,EndTime,Interval,Duration\n");
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            CVMMultiClient cvm = new CVMMultiClient();
            cvm.startStandardLifeCycle(150000L);
            Thread.sleep(150L);
            System.exit(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // ArrayList<NodeComponentInfo> result = CVM.buildMap(5);
        // System.out.println(result.size());
        // System.out.println(result);
    }
}
