package bcm.components;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import bcm.ports.LookupInboundPort;
import bcm.ports.RegistrationInboundPort;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.GeographicalZoneI;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI;
import fr.sorbonne_u.utils.aclocks.ClocksServerCI;
import implementation.ConnectionInfoImpl;

/**
 * <p>
 * <strong> Description </strong>
 * </p>
 * The registry component is responsible for registering and looking up nodes in
 * the network. It is also responsible for finding neighbours of a node.
 * <p>
 * <strong>Invariant</strong>
 * </p>
 */
@OfferedInterfaces(offered = { LookupCI.class, RegistrationCI.class })
@RequiredInterfaces(required = { ClocksServerCI.class })
public class RegistryComponent extends AbstractComponent {
    protected LookupInboundPort lookUpInboundPort;
    protected RegistrationInboundPort registryInboundPort;

    protected ConcurrentHashMap<String, NodeInfoI> nodeIDToNodeInfoMap;

    // -------- poool of threads
    protected final String registeryPoolURI;
    // -------- index of the pool of threads
    protected final int registeryPoolIndex;

    /**
     * Constructor of the registry component
     * 
     * @param uri
     * @param nbThreads
     * @param nbSchedulableThreads
     * @param lookupInboundPortURI
     * @param registerInboundPortURI
     * @param registeryPoolURI
     * @param registeryPoolnbThreads
     */
    protected RegistryComponent(String uri,
            int nbThreads, int nbSchedulableThreads,
            String lookupInboundPortURI,
            String registerInboundPortURI,
            String registeryPoolURI,
            int registeryPoolnbThreads) {
        super(uri, nbThreads, nbSchedulableThreads);
        assert uri != null;
        assert lookupInboundPortURI != null;
        assert registerInboundPortURI != null;
        assert registeryPoolURI != null;
        assert registeryPoolnbThreads > 0;
        this.nodeIDToNodeInfoMap = new ConcurrentHashMap<>();
        this.registeryPoolURI = registeryPoolURI;
        this.registeryPoolIndex = createNewExecutorService(registeryPoolURI, registeryPoolnbThreads, false);
        try {
            this.lookUpInboundPort = new LookupInboundPort(lookupInboundPortURI, this);
            this.registryInboundPort = new RegistrationInboundPort(registerInboundPortURI, this);
            this.lookUpInboundPort.publishPort();
            this.registryInboundPort.publishPort();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor of the registry component with default uri
     * 
     * @param nbThreads
     * @param nbSchedulableThreads
     * @param lookupInboundPortURI
     * @param registerInboundPortURI
     * @param registeryPoolURI
     * @param registeryPoolnbThreads
     */
    protected RegistryComponent(
            int nbThreads, int nbSchedulableThreads,
            String lookupInboundPortURI,
            String registerInboundPortURI,
            String registeryPoolURI,
            int registeryPoolnbThreads) {
        super(nbThreads, nbSchedulableThreads);
        assert lookupInboundPortURI != null;
        assert registerInboundPortURI != null;
        assert registeryPoolURI != null;
        assert registeryPoolnbThreads > 0;
        this.nodeIDToNodeInfoMap = new ConcurrentHashMap<>();
        this.registeryPoolURI = registeryPoolURI;
        this.registeryPoolIndex = createNewExecutorService(registeryPoolURI, registeryPoolnbThreads, false);
        try {
            this.lookUpInboundPort = new LookupInboundPort(lookupInboundPortURI, this);
            this.registryInboundPort = new RegistrationInboundPort(registerInboundPortURI, this);
            this.lookUpInboundPort.publishPort();
            this.registryInboundPort.publishPort();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * See {@link fr.sorbonne_u.components.AbstractComponent#start()}
     */
    @Override
    public synchronized void start() throws ComponentStartException {
        super.start();
        this.logMessage("starting Registry component.");
    }

    /**
     * Get the index of the pool of threads
     * 
     * @return the index of the pool of threads
     */
    public int getRegisteryPoolIndex() {
        return registeryPoolIndex;
    }

    /**
     * See
     * {@link fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI#findByIdentifier(String)}
     * 
     * @param sensorNodeId
     * @return the connection info
     * @throws Exception
     */
    public ConnectionInfoI findByIdentifier(String sensorNodeId) throws Exception {
        try {
            NodeInfoI nodeInfo = this.nodeIDToNodeInfoMap.get(sensorNodeId);
            return new ConnectionInfoImpl(nodeInfo.nodeIdentifier(), nodeInfo.endPointInfo());
        } catch (Exception e) {
            throw new Exception("No node with id " + sensorNodeId + " found.");
        }
    }

    /**
     * See
     * {@link fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI#findByZone(GeographicalZoneI)}
     * 
     * @param z the geographical zone
     * @return the set of connection info
     * @throws Exception
     */
    public Set<ConnectionInfoI> findByZone(GeographicalZoneI z) throws Exception {
        try {
            assert z != null;
            Set<ConnectionInfoI> result = new HashSet<>();
            for (NodeInfoI n : nodeIDToNodeInfoMap.values()) {
                if (z.in(n.nodePosition())) {
                    result.add(new ConnectionInfoImpl(n.nodeIdentifier(), n.endPointInfo()));
                }
            }
            return result;
        } catch (Exception e) {
            throw new Exception("No node found in zone " + z + ".");
        }
    }

    /**
     * See
     * {@link fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI#registered(String)}
     * 
     * @param nodeIdentifier
     * @return true if the node is registered
     * @throws Exception
     */
    public boolean registered(String nodeIdentifier) throws Exception {
        return this.nodeIDToNodeInfoMap.containsKey(nodeIdentifier);
    }

    /**
     * Print all the nodes
     * 
     * @return the string to print out
     */
    private String printAllNodes() {
        StringBuilder sb = new StringBuilder();
        sb.append("Nodes " + nodeIDToNodeInfoMap.size() + ": ");
        for (NodeInfoI n : nodeIDToNodeInfoMap.values()) {
            sb.append(n.nodeIdentifier() + " ");
        }
        return sb.toString();
    }

    /**
     * Check if two nodes are in range of each other
     * 
     * @param n        the first node
     * @param nodeInfo the second node
     * @return true if the nodes are in range of each other
     */
    private boolean inRangeOfEachOther(NodeInfoI n, NodeInfoI nodeInfo) {
        return n.nodePosition().distance(nodeInfo.nodePosition()) < n.nodeRange()
                || nodeInfo.nodePosition().distance(n.nodePosition()) < nodeInfo.nodeRange()
                        && (!n.nodeIdentifier().equals(nodeInfo.nodeIdentifier()));
    }

    /**
     * See
     * {@link fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI#register(NodeInfoI)}
     * 
     * @param nodeInfo
     * @return the set of neighbours
     * @throws Exception
     */
    public Set<NodeInfoI> register(NodeInfoI nodeInfo) throws Exception {
        try { // Init the result set
            Set<NodeInfoI> result = new HashSet<>();

            // Find the neighbours of the new node in the 4 directions
            Map<Direction, NodeInfoI> directionalNeighbours = new HashMap<>();

            // Look at the hashmap of all nodes and find the neighbours of the new node
            for (NodeInfoI n : nodeIDToNodeInfoMap.values()) {
                if (inRangeOfEachOther(n, nodeInfo)) {
                    Direction nDir = nodeInfo.nodePosition().directionFrom(n.nodePosition());
                    if (nDir == null) {
                        continue;
                    }
                    if (directionalNeighbours.containsKey(nDir)) {
                        Double currentDist = directionalNeighbours.get(nDir).nodePosition()
                                .distance(nodeInfo.nodePosition());
                        Double newDist = n.nodePosition().distance(nodeInfo.nodePosition());
                        if (newDist < currentDist) {
                            directionalNeighbours.put(nDir, n);
                        }
                    } else {
                        directionalNeighbours.put(nDir, n);
                    }
                }
            }
            for (NodeInfoI node : directionalNeighbours.values()) {
                result.add(node);
            }
            this.nodeIDToNodeInfoMap.put(nodeInfo.nodeIdentifier(), nodeInfo);
            this.logMessage("registered node " + nodeInfo.nodeIdentifier() + " and has neighbours: " + result.size());
            this.logMessage(this.printAllNodes());
            for (Direction node : directionalNeighbours.keySet()) {
                logMessage(
                        "Neighbour in direction " + node + " is " + directionalNeighbours.get(node).nodeIdentifier());
            }

            return result;
        } catch (Exception e) {
            throw new Exception("Error registering node " + nodeInfo.nodeIdentifier() + ".");
        }
    }

    /**
     * See
     * {@link fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI#findNewNeighbour(NodeInfoI, Direction)}
     * 
     * @param nodeInfo
     * @param d        The direction to look for a new neighbour
     * @return The new neighbour
     * @throws Exception
     */
    public NodeInfoI findNewNeighbour(NodeInfoI nodeInfo, Direction d) throws Exception {
        try {
            Double minDist = Double.POSITIVE_INFINITY;
            NodeInfoI result = null;

            for (NodeInfoI node : nodeIDToNodeInfoMap.values()) {
                PositionI nodePosition = node.nodePosition();
                if (!node.nodeIdentifier().equals(nodeInfo.nodeIdentifier())
                        && inRangeOfEachOther(node, nodeInfo)
                        && nodeInfo.nodePosition().directionFrom(nodePosition) == d) {
                    Double dist = nodePosition.distance(nodeInfo.nodePosition());
                    if (dist <= minDist) {
                        minDist = dist;
                        result = node;
                    }
                }
            }
            this.logMessage("found new neighbour for node " + nodeInfo.nodeIdentifier() + " in direction " + d);
            return result;
        } catch (Exception e) {
            throw new Exception("Error finding new neighbour for node " + nodeInfo.nodeIdentifier() + ".");
        }
    }

    /**
     * See
     * {@link fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI#unregister(String)}
     * 
     * @param nodeIdentifier
     * @throws Exception
     */
    public void unregister(String nodeIdentifier) throws Exception {
        try {
            this.nodeIDToNodeInfoMap.remove(nodeIdentifier);
            this.logMessage("unregistered node " + nodeIdentifier + ".");
        } catch (Exception e) {
            throw new Exception("Error unregistering node " + nodeIdentifier + ".");
        }
    }

    /**
     * See {@link AbstractComponent#execute()}
     */
    @Override
    public synchronized void execute() throws Exception {
        super.execute();
        this.logMessage("executing Registry component.");
    }

    /**
     * See {@link fr.sorbonne_u.components.AbstractComponent#finalise()}
     */
    @Override
    public synchronized void finalise() throws Exception {
        this.logMessage("finalising Registry component.");
        this.logMessage(printAllNodes());
        super.finalise();
    }

    /**
     * See {@link fr.sorbonne_u.components.AbstractComponent#shutdown()}
     */
    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        // the shutdown is a good place to unpublish inbound ports.
        try {
            this.lookUpInboundPort.unpublishPort();
            this.registryInboundPort.unpublishPort();
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
    }

    /**
     * See {@link fr.sorbonne_u.components.AbstractComponent#shutdownNow()}
     */
    @Override
    public synchronized void shutdownNow() throws ComponentShutdownException {
        // the shutdown is a good place to unpublish inbound ports.
        try {
            if (this.lookUpInboundPort.isPublished())
                this.lookUpInboundPort.unpublishPort();
            if (this.registryInboundPort.isPublished())
                this.registryInboundPort.unpublishPort();
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdownNow();
    }

}
