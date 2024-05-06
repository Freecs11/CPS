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
import fr.sorbonne_u.components.cvm.AbstractDistributedCVM;
import fr.sorbonne_u.components.helpers.CVMDebugModes;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import implementation.SensorDataIMPL;
import utils.NodeComponentInfo;

public class DistributedCVM
        extends AbstractDistributedCVM {

    public static final String CLOCK_URI = "CLOCK-SERVER_REP";
    protected long TIME_TO_START;
    protected long unixEpochStartTimeInNanos;
    protected Instant CLOCK_START_INSTANT = Instant.parse("2024-01-31T09:00:00.00Z");
    protected double accelerationFactor;
    protected Instant REG_START_INSTANT = CLOCK_START_INSTANT.plusSeconds(5L);

    protected final int NB_NODES = 10;
    protected final int NB_CLIENTS = 1;

    /** URI of the consumer component (convenience). */
    protected static final String CLIENT_COMPONENT_URI = "client-URI";
    protected static final String REGISTER_COMPONENT_URI = "register-URI";

    protected static final String REGISTER_IN_BOUND_PORT_URI = "register-inbound-port";
    protected static final String LOOKUP_IN_BOUND_PORT_URI = "lookup_inbound-port";
    protected static final String CLIENTS_OUT_BOUND_PORT_URI = "client-outbound-port";

    // ---------JVM uris -------------
    // 5 JVMs ( 1 client , 10 nodes in each JVM) + 1 JVM for the registry
    protected static String CLUSTER_JVM1_URI = "cluster-jvm1";
    protected static String CLUSTER_JVM2_URI = "cluster-jvm2";
    protected static String CLUSTER_JVM3_URI = "cluster-jvm3";
    protected static String CLUSTER_JVM4_URI = "cluster-jvm4";
    protected static String CLUSTER_JVM5_URI = "cluster-jvm5";
    protected static String REGISTRY_JVM_URI = "registry-jvm";


    protected String uriRegisterURI;

    public DistributedCVM(String[] args) throws Exception {
        super(args);
    }

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

    /**
     * Documentation a faire
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

    // /**
    // * @see fr.sorbonne_u.components.cvm.AbstractDistributedCVM#initialise()
    // */
    // @Override
    // public void initialise() throws Exception {
    // super.initialise();
    //
    // String[] jvmURIs = this.configurationParameters.getJvmURIs();
    // boolean clusterJVM1_URI_OK = false;
    // boolean clusterJVM2_URI_OK = false;
    // boolean clusterJVM3_URI_OK = false;
    // boolean clusterJVM4_URI_OK = false;
    // boolean clusterJVM5_URI_OK = false;
    // boolean registryJVM_URI_OK = false;
    // for (int i = 0; i < jvmURIs.length
    // && (!clusterJVM1_URI_OK || !clusterJVM2_URI_OK || !clusterJVM3_URI_OK ||
    // !clusterJVM4_URI_OK
    // || !clusterJVM5_URI_OK || !registryJVM_URI_OK); i++) {
    // if (jvmURIs[i].equals(CLUSTER_JVM1_URI)) {
    // clusterJVM1_URI_OK = true;
    // } else if (jvmURIs[i].equals(CLUSTER_JVM2_URI)) {
    // clusterJVM2_URI_OK = true;
    // } else if (jvmURIs[i].equals(CLUSTER_JVM3_URI)) {
    // clusterJVM3_URI_OK = true;
    // } else if (jvmURIs[i].equals(CLUSTER_JVM4_URI)) {
    // clusterJVM4_URI_OK = true;
    // } else if (jvmURIs[i].equals(CLUSTER_JVM5_URI)) {
    // clusterJVM5_URI_OK = true;
    // } else if (jvmURIs[i].equals(REGISTRY_JVM_URI)) {
    // registryJVM_URI_OK = true;
    // }
    // }
    // assert clusterJVM1_URI_OK && clusterJVM2_URI_OK && clusterJVM3_URI_OK &&
    // clusterJVM4_URI_OK
    // && clusterJVM5_URI_OK && registryJVM_URI_OK;
    // }

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
        int index = 0;

        // JVM 1 ( regitery) , we also use the registery JVM to host the clock server
        if (thisJVMURI.equals(REGISTRY_JVM_URI)) {
            TIME_TO_START = 1000L;
            unixEpochStartTimeInNanos = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis() + TIME_TO_START);
            accelerationFactor = 10.0;

            AbstractComponent.createComponent(ClocksServer.class.getCanonicalName(),
                    new Object[] {
                            CLOCK_URI,
                            unixEpochStartTimeInNanos,
                            CLOCK_START_INSTANT,
                            accelerationFactor
                    });

            // Register is starting directly with no link to the clock
            AbstractComponent.createComponent(RegistryComponent.class.getCanonicalName(),
                    new Object[] {
                            REGISTER_COMPONENT_URI,
                            1, 0,
                            LOOKUP_IN_BOUND_PORT_URI,
                            REGISTER_IN_BOUND_PORT_URI,
                            "registeryPoolURI",
                            nodes.size() });
            this.toggleTracing(uriRegisterURI);
            this.toggleLogging(uriRegisterURI);

        }
        // JVM 2
        // we're gonna use a simple approach , first we create the map of the nodes (
        // 50) then we just take 10 nodes for each JVM
        // and we create the nodes and the client

        else if (AbstractCVM.getThisJVMURI().equals(CLUSTER_JVM1_URI)) {

            // Create the nodes
            for (int i = 0; i < 10; i++) {
                NodeComponentInfo node = nodes.get(i);
                ArrayList<SensorDataI> data = createSensorDataForNode(node, valuesList, index);
                String uriNode = "uri" + node.getName();
                String uri = AbstractComponent.createComponent(NodeComponent.class.getCanonicalName(),
                        new Object[] {
                                uriNode, node.getName(), node.getX(),
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

            // Create the client
            String uriClientURI1 = AbstractComponent.createComponent(ClientComponent.class.getCanonicalName(),
                    new Object[] {
                            CLIENT_COMPONENT_URI + "1",
                            LOOKUP_IN_BOUND_PORT_URI,
                            REG_START_INSTANT.plusSeconds(150L),
                            nodes.size(), nodes.size(),
                            "client1"
                    });
            this.toggleTracing(uriClientURI1);
            this.toggleLogging(uriClientURI1);
        }

        else if (AbstractCVM.getThisJVMURI().equals(CLUSTER_JVM2_URI)) {

            // Create the nodes
            for (int i = 10; i < 20; i++) {
                NodeComponentInfo node = nodes.get(i);
                ArrayList<SensorDataI> data = createSensorDataForNode(node, valuesList, index);
                String uriNode = "uri" + node.getName();
                String uri = AbstractComponent.createComponent(NodeComponent.class.getCanonicalName(),
                        new Object[] {
                                uriNode, node.getName(), node.getX(), node.getY(),
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

            // Create the client
            // AbstractComponent.createComponent(ClientComponent.class.getCanonicalName(),
            // new Object[] {
            // CLIENT_COMPONENT_URI + "2",
            // LOOKUP_IN_BOUND_PORT_URI,
            // REG_START_INSTANT.plusSeconds(100L),
            // nodes.size(), nodes.size(),
            // "client2"
            // });
        }

        if (AbstractCVM.getThisJVMURI().equals(CLUSTER_JVM3_URI)) {

            // Create the nodes
            for (int i = 20; i < 30; i++) {
                NodeComponentInfo node = nodes.get(i);
                ArrayList<SensorDataI> data = createSensorDataForNode(node, valuesList, index);
                String uriNode = "uri" + node.getName();
                String uri = AbstractComponent.createComponent(NodeComponent.class.getCanonicalName(),
                        new Object[] {
                                uriNode, node.getName(), node.getX(), node.getY(),
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
            // AbstractComponent.createComponent(ClientComponent.class.getCanonicalName(),
            // new Object[] {
            // CLIENT_COMPONENT_URI + "3",
            // LOOKUP_IN_BOUND_PORT_URI,
            // REG_START_INSTANT.plusSeconds(100L),
            // nodes.size(), nodes.size(),
            // "client3"
            // });
        }

        if (AbstractCVM.getThisJVMURI().equals(CLUSTER_JVM4_URI)) {

            // Create the nodes
            for (int i = 30; i < 40; i++) {
                NodeComponentInfo node = nodes.get(i);
                ArrayList<SensorDataI> data = createSensorDataForNode(node, valuesList, index);
                String uriNode = "uri" + node.getName();
                String uri = AbstractComponent.createComponent(NodeComponent.class.getCanonicalName(),
                        new Object[] {
                                uriNode, node.getName(), node.getX(), node.getY(),
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
            // AbstractComponent.createComponent(ClientComponent.class.getCanonicalName(),
            // new Object[] {
            // CLIENT_COMPONENT_URI + "4",
            // LOOKUP_IN_BOUND_PORT_URI,
            // REG_START_INSTANT.plusSeconds(100L),
            // nodes.size(), nodes.size(),
            // "client4"
            // });
        }

        if (AbstractCVM.getThisJVMURI().equals(CLUSTER_JVM5_URI)) {

            // Create the nodes
            for (int i = 40; i < 50; i++) {
                NodeComponentInfo node = nodes.get(i);
                ArrayList<SensorDataI> data = createSensorDataForNode(node, valuesList, index);
                String uriNode = "uri" + node.getName();
                String uri = AbstractComponent.createComponent(NodeComponent.class.getCanonicalName(),
                        new Object[] {
                                uriNode, node.getName(), node.getX(), node.getY(),
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
            // AbstractComponent.createComponent(ClientComponent.class.getCanonicalName(),
            // new Object[] {
            // CLIENT_COMPONENT_URI + "5",
            // LOOKUP_IN_BOUND_PORT_URI,
            // REG_START_INSTANT.plusSeconds(100L),
            // nodes.size(), nodes.size(),
            // "client5"
            // });
        } else {
            System.err.println("Unknown JVM URI: " + AbstractCVM.getThisJVMURI());
        }

        super.instantiateAndPublish();

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

    public static void main(String[] args) {
        try {
            DistributedCVM dda = new DistributedCVM(args);
            dda.startStandardLifeCycle(85000L);
            Thread.sleep(10000L);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
