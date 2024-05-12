package bcm;

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
import fr.sorbonne_u.components.cvm.AbstractDistributedCVM;
import fr.sorbonne_u.components.helpers.CVMDebugModes;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import implementation.SensorDataIMPL;
import tests.Queries;
import utils.NodeComponentInfo;

/**
 * The class <code>DistributedCVM</code> deploys the whole system on multiple
 * JVMs.
 * 
 * 
 * <p>
 * Problem
 * </p>
 * this class doesn't work as expected, the nodes are not created in the right
 * JVMs, but we have difficulties to establish communication between the JVMs
 * we get a connection Timeout exception when we try to connect to the registry
 * 
 */
public class DistributedCVM
        extends AbstractDistributedCVM {

    // ---------------------------------------------------------------------
    // Clock component specific constants
    public static final String CLOCK_URI = "CLOCK-SERVER_REP";
    protected long TIME_TO_START;
    protected long unixEpochStartTimeInNanos;
    protected Instant CLOCK_START_INSTANT = Instant.parse("2024-01-31T09:00:00.00Z");
    protected double accelerationFactor;
    protected Instant REG_START_INSTANT = CLOCK_START_INSTANT.plusSeconds(5L);

    // Inbound port URI for the registry component
    protected static final String REGISTER_IN_BOUND_PORT_URI = "register-inbound-port";
    protected static final String LOOKUP_IN_BOUND_PORT_URI = "lookup_inbound-port";

    // ---------JVM uris -------------
    // 5 JVMs ( 1 client , 10 nodes in each JVM) + 1 JVM for the registry
    protected static String CLUSTER_JVM1_URI = "clusterjvm1";
    protected static String CLUSTER_JVM2_URI = "clusterjvm2";
    protected static String CLUSTER_JVM3_URI = "clusterjvm3";
    protected static String CLUSTER_JVM4_URI = "clusterjvm4";
    protected static String CLUSTER_JVM5_URI = "clusterjvm5";
    protected static String REGISTRY_JVM_URI = "registryjvm";

    private String filename; // the name of the file to write the results to

    public DistributedCVM(String[] args) throws Exception {
        super(args);
        this.filename = "testResultsDistributed.csv";
    }

    /**
     * Calculate the grid size for a given number of nodes.
     * 
     * @param desiredNodes the number of nodes to place on the grid
     * @return the grid size that can accommodate the desired number of nodes, to be
     *         used for the buildmap method
     */
    public static int calculateGridSize(int desiredNodes) {
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

            if (nodesCount >= desiredNodes) {
                return gridSize;
            }

            gridSize++; // Increment grid size and try again
        }
    }

    /**
     * Build a map of nodes for a given grid size.
     * it will follow the pattern in the subject example
     * 
     * @param gridSize
     * @return
     */
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

    /**
     * @see fr.sorbonne_u.components.cvm.AbstractDistributedCVM#initialise()
     * @throws Exception
     */
    @Override
    public void initialise() throws Exception {
        super.initialise();

        String[] jvmURIs = this.configurationParameters.getJvmURIs();
        boolean clusterJVM1_URI_OK = false;
        boolean clusterJVM2_URI_OK = false;
        boolean clusterJVM3_URI_OK = false;
        boolean clusterJVM4_URI_OK = false;
        boolean clusterJVM5_URI_OK = false;
        boolean registryJVM_URI_OK = false;
        for (int i = 0; i < jvmURIs.length
                && (!clusterJVM1_URI_OK || !clusterJVM2_URI_OK || !clusterJVM3_URI_OK ||
                        !clusterJVM4_URI_OK
                        || !clusterJVM5_URI_OK || !registryJVM_URI_OK); i++) {
            if (jvmURIs[i].equals(CLUSTER_JVM1_URI)) {
                clusterJVM1_URI_OK = true;
            } else if (jvmURIs[i].equals(CLUSTER_JVM2_URI)) {
                clusterJVM2_URI_OK = true;
            } else if (jvmURIs[i].equals(CLUSTER_JVM3_URI)) {
                clusterJVM3_URI_OK = true;
            } else if (jvmURIs[i].equals(CLUSTER_JVM4_URI)) {
                clusterJVM4_URI_OK = true;
            } else if (jvmURIs[i].equals(CLUSTER_JVM5_URI)) {
                clusterJVM5_URI_OK = true;
            } else if (jvmURIs[i].equals(REGISTRY_JVM_URI)) {
                registryJVM_URI_OK = true;
            }
        }
        assert clusterJVM1_URI_OK && clusterJVM2_URI_OK && clusterJVM3_URI_OK &&
                clusterJVM4_URI_OK
                && clusterJVM5_URI_OK && registryJVM_URI_OK;
    }

    /**
     * 
     * <p>
     * <strong>Contract</strong>
     * </p>
     * 
     * <pre>
     * pre	true				// no more preconditions.
     * post	true				// no more postconditions.
     * </pre>
     * 
     * @see fr.sorbonne_u.components.cvm.AbstractDistributedCVM#instantiateAndPublish()
     */
    @Override
    public void instantiateAndPublish() throws Exception {

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

        // ---------------------------------------------------------------------
        // Creation phase
        // ---------------------------------------------------------------------

        int desiredTotalNodes = 50;
        int gridSize = calculateGridSize(desiredTotalNodes);
        ArrayList<NodeComponentInfo> nodes = buildMap(gridSize);
        List<List<Double>> valuesList = createValuesList(desiredTotalNodes);
        int index = 0; // index to use in the values list ( used to separate the values for each jvm)

        // we could add more intervals if we want to test the system with different
        // intervals
        List<Long> intervals = new ArrayList<>();
        intervals.add(60L);

        // JVM 1 ( regitery) , we also use the registery JVM to host the clock server
        if (thisJVMURI.equals(REGISTRY_JVM_URI)) {
            TIME_TO_START = 2000L;
            unixEpochStartTimeInNanos = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis() + TIME_TO_START);
            accelerationFactor = 60.0;

            // Create the clock server
            AbstractComponent.createComponent(ClocksServer.class.getCanonicalName(),
                    new Object[] {
                            CLOCK_URI,
                            unixEpochStartTimeInNanos,
                            CLOCK_START_INSTANT,
                            accelerationFactor
                    });

            // Register is starting directly with no link to the clock
            String uriRegisterURI = AbstractComponent.createComponent(RegistryComponent.class.getCanonicalName(),
                    new Object[] {
                            1, 0,
                            LOOKUP_IN_BOUND_PORT_URI,
                            REGISTER_IN_BOUND_PORT_URI,
                            "registeryPoolURI",
                            50 });
            this.toggleTracing(uriRegisterURI);
            this.toggleLogging(uriRegisterURI);
        }

        // JVM 2
        else if (thisJVMURI.equals(CLUSTER_JVM1_URI)) {
            // Create the nodes
            for (int i = 0; i < 10; i++) {
                NodeComponentInfo node = nodes.get(i);
                ArrayList<SensorDataI> data = createSensorDataForNode(node, valuesList, index);
                String uri = AbstractComponent.createComponent(NodeComponent.class.getCanonicalName(),
                        new Object[] { node.getName(), node.getX(),
                                node.getY(),
                                node.getRange(),
                                REGISTER_IN_BOUND_PORT_URI,
                                data,
                                REG_START_INSTANT.plusSeconds(5L + i),
                                nodes.size(), nodes.size(),
                                "syncRequestPool_" + node.getName(),
                                "asyncRequestPool_" + node.getName(),
                                "syncContPool_" + node.getName(),
                                "asyncContPool_" + node.getName()
                        });
                this.toggleTracing(uri);
                this.toggleLogging(uri);
                index++;

            }

            HashMap<String, List<QueryI>> queriesClient = new HashMap<>();
            queriesClient.put("node4", Queries.queries1);
            // Create the client
            String uriClientURI1 = AbstractComponent.createComponent(ClientComponent.class.getCanonicalName(),
                    new Object[] {
                            LOOKUP_IN_BOUND_PORT_URI,
                            REG_START_INSTANT.plusSeconds(150L),
                            nodes.size(), nodes.size(),
                            "client1",
                            queriesClient,
                            intervals,
                            false,
                            filename
                    });
            this.toggleTracing(uriClientURI1);
            this.toggleLogging(uriClientURI1);
        }

        // JVM 3
        else if (thisJVMURI.equals(CLUSTER_JVM2_URI)) {
            for (int i = 10; i < 20; i++) {
                NodeComponentInfo node = nodes.get(i);
                ArrayList<SensorDataI> data = createSensorDataForNode(node, valuesList, index);
                String uri = AbstractComponent.createComponent(NodeComponent.class.getCanonicalName(),
                        new Object[] { node.getName(),
                                node.getX(), node.getY(),
                                node.getRange(),
                                REGISTER_IN_BOUND_PORT_URI,
                                data,
                                REG_START_INSTANT.plusSeconds(5L + index),
                                nodes.size(), nodes.size(),
                                "syncRequestPool_" + node.getName(),
                                "asyncRequestPool_" + node.getName(),
                                "syncContPool_" + node.getName(),
                                "asyncContPool_" + node.getName()
                        });
                this.toggleTracing(uri);
                this.toggleLogging(uri);
                index++;
            }

            HashMap<String, List<QueryI>> queriesClient2 = new HashMap<>();
            queriesClient2.put("node14", Queries.queries2);
            String uriClientURI2 = AbstractComponent.createComponent(ClientComponent.class.getCanonicalName(),
                    new Object[] {
                            LOOKUP_IN_BOUND_PORT_URI,
                            REG_START_INSTANT.plusSeconds(150L),
                            nodes.size(), nodes.size(),
                            "client2",
                            queriesClient2,
                            intervals,
                            false,
                            filename
                    });
            this.toggleTracing(uriClientURI2);
            this.toggleLogging(uriClientURI2);
        }

        if (thisJVMURI.equals(CLUSTER_JVM3_URI)) {

            for (int i = 20; i < 30; i++) {
                NodeComponentInfo node = nodes.get(i);
                ArrayList<SensorDataI> data = createSensorDataForNode(node, valuesList, index);
                String uri = AbstractComponent.createComponent(NodeComponent.class.getCanonicalName(),
                        new Object[] { node.getName(), node.getX(), node.getY(),
                                node.getRange(),
                                REGISTER_IN_BOUND_PORT_URI,
                                data,
                                REG_START_INSTANT.plusSeconds(1L),
                                nodes.size(), nodes.size(),
                                "syncRequestPool_" + node.getName(),
                                "asyncRequestPool_" + node.getName(),
                                "syncContPool_" + node.getName(),
                                "asyncContPool_" + node.getName()
                        });
                this.toggleTracing(uri);
                this.toggleLogging(uri);
                index++;
            }

            // Create the client
            HashMap<String, List<QueryI>> queriesClient3 = new HashMap<>();
            queriesClient3.put("node24", Queries.queries3);
            String uriClientURI3 = AbstractComponent.createComponent(ClientComponent.class.getCanonicalName(),
                    new Object[] {
                            LOOKUP_IN_BOUND_PORT_URI,
                            REG_START_INSTANT.plusSeconds(150L),
                            nodes.size(), nodes.size(),
                            "client3",
                            queriesClient3,
                            intervals,
                            false,
                            filename
                    });
            this.toggleTracing(uriClientURI3);
            this.toggleLogging(uriClientURI3);
        }

        if (thisJVMURI.equals(CLUSTER_JVM4_URI)) {
            for (int i = 30; i < 40; i++) {
                NodeComponentInfo node = nodes.get(i);
                ArrayList<SensorDataI> data = createSensorDataForNode(node, valuesList, index);
                String uri = AbstractComponent.createComponent(NodeComponent.class.getCanonicalName(),
                        new Object[] { node.getName(), node.getX(), node.getY(),
                                node.getRange(),
                                REGISTER_IN_BOUND_PORT_URI,
                                data,
                                REG_START_INSTANT.plusSeconds(1L),
                                nodes.size(), nodes.size(),
                                "syncRequestPool_" + node.getName(),
                                "asyncRequestPool_" + node.getName(),
                                "syncContPool_" + node.getName(),
                                "asyncContPool_" + node.getName()
                        });
                this.toggleTracing(uri);
                this.toggleLogging(uri);
                index++;
            }

            // Create the client
            HashMap<String, List<QueryI>> queriesClient4 = new HashMap<>();
            queriesClient4.put("node34", Queries.queries4);
            String uriClientURI4 = AbstractComponent.createComponent(ClientComponent.class.getCanonicalName(),
                    new Object[] {
                            LOOKUP_IN_BOUND_PORT_URI,
                            REG_START_INSTANT.plusSeconds(150L),
                            nodes.size(), nodes.size(),
                            "client4",
                            queriesClient4,
                            intervals,
                            false,
                            filename
                    });
            this.toggleTracing(uriClientURI4);
            this.toggleLogging(uriClientURI4);
        }
        if (thisJVMURI.equals(CLUSTER_JVM5_URI)) {
            for (int i = 40; i < 50; i++) {
                NodeComponentInfo node = nodes.get(i);
                ArrayList<SensorDataI> data = createSensorDataForNode(node, valuesList, index);
                String uri = AbstractComponent.createComponent(NodeComponent.class.getCanonicalName(),
                        new Object[] { node.getName(), node.getX(), node.getY(),
                                node.getRange(), REGISTER_IN_BOUND_PORT_URI,
                                data,
                                REG_START_INSTANT.plusSeconds(1L),
                                nodes.size(), nodes.size(),
                                "syncRequestPool_" + node.getName(),
                                "asyncRequestPool_" + node.getName(),
                                "syncContPool_" + node.getName(),
                                "asyncContPool_" + node.getName()
                        });
                this.toggleTracing(uri);
                this.toggleLogging(uri);
                index++;
            }

            // Create the client
            HashMap<String, List<QueryI>> queriesClient5 = new HashMap<>();
            queriesClient5.put("node44", Queries.queries5);
            String uriClientURI5 = AbstractComponent.createComponent(ClientComponent.class.getCanonicalName(),
                    new Object[] {
                            LOOKUP_IN_BOUND_PORT_URI,
                            REG_START_INSTANT.plusSeconds(150L),
                            nodes.size(), nodes.size(),
                            "client5",
                            queriesClient5,
                            intervals,
                            false,
                            filename
                    });
            this.toggleTracing(uriClientURI5);
            this.toggleLogging(uriClientURI5);

        } else {
            System.err.println("Unknown JVM URI: " + AbstractCVM.getThisJVMURI());
        }

        super.instantiateAndPublish();

    }

    /**
     * Create a list of values to be used to create the sensor data for the nodes.
     * 
     * @param size the number of values to create
     * @return the list of values created
     */
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

    /**
     * Create the sensor data for a given node and a given list of values.
     * 
     * @param node       the node for which to create the sensor data
     * @param valuesList the list of values to use to create the sensor data
     * @param index      the index of the values to use in the list
     * @return the list of sensor data created for the node
     */
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

    public static void main(String[] args) {
        try {
            DistributedCVM dda = new DistributedCVM(args);
            dda.startStandardLifeCycle(210000L);
            Thread.sleep(10000L);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
