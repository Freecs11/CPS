package bcm.components;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import bcm.CVM;
import bcm.ports.LookupInboundPort;
import bcm.ports.RegistryInboundPort;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.GeographicalZoneI;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import fr.sorbonne_u.utils.aclocks.ClocksServerCI;
import fr.sorbonne_u.utils.aclocks.ClocksServerConnector;
import fr.sorbonne_u.utils.aclocks.ClocksServerOutboundPort;

@OfferedInterfaces(offered = {
        LookupCI.class, RegistrationCI.class })
@RequiredInterfaces(required = {
        ClocksServerCI.class })
public class RegistryComponent extends AbstractComponent {
    protected LookupInboundPort lookUpInboundPort;
    protected RegistryInboundPort registryInboundPort;
    protected Map<String, NodeInfoI> nodeIDToNodeInfoMap;
    public static final Instant REG_START_INSTANT = CVM.CLOCK_START_INSTANT.plusSeconds(1);
    private AcceleratedClock clock;

    protected RegistryComponent(String uri,
            int nbThreads, int nbSchedulableThreads,
            String lookupInboundPortURI,
            String registerInboundPortURI) {
        super(uri, nbThreads, nbSchedulableThreads);
        this.nodeIDToNodeInfoMap = new HashMap<>();
        try {
            this.lookUpInboundPort = new LookupInboundPort(lookupInboundPortURI, this);
            this.registryInboundPort = new RegistryInboundPort(registerInboundPortURI, this);
            this.lookUpInboundPort.publishPort();
            this.registryInboundPort.publishPort();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void start() throws ComponentStartException {
        super.start();
        try {
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
            this.clock.waitUntilStart();
            this.logMessage("Registry component waiting.......");
            long delayTilStart = this.clock.nanoDelayUntilInstant(REG_START_INSTANT);
            this.scheduleTask(
                    nil -> {
                        this.logMessage("Waiting " + delayTilStart + " ns before starting the registry component.");
                    }, delayTilStart, TimeUnit.NANOSECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void execute() throws Exception {

    }

    public ConnectionInfoI findByIdentifier(String sensorNodeId) throws Exception {
        try {
            return this.nodeIDToNodeInfoMap.get(sensorNodeId);
        } catch (Exception e) {
            throw new Exception("No node with id " + sensorNodeId + " found.");
        }
    }

    public Set<ConnectionInfoI> findByZone(GeographicalZoneI z) throws Exception {
        try {
            assert z != null;
            Set<ConnectionInfoI> result = new HashSet<>();
            for (NodeInfoI nodeInfo : nodeIDToNodeInfoMap.values()) {
                if (z.in(nodeInfo.nodePosition())) {
                    result.add(nodeInfo);
                }
            }
            return result;
        } catch (Exception e) {
            throw new Exception("No node found in zone " + z + ".");
        }
    }

    public boolean registered(String nodeIdentifier) throws Exception {
        return this.nodeIDToNodeInfoMap.containsKey(nodeIdentifier);
    }

    private String printAllNodes() {
        StringBuilder sb = new StringBuilder();
        sb.append("Nodes " + nodeIDToNodeInfoMap.size() + ": ");
        for (NodeInfoI n : nodeIDToNodeInfoMap.values()) {
            sb.append(n.nodeIdentifier() + " ");
        }
        return sb.toString();
    }

    private boolean inRangeOfEachOther(NodeInfoI n, NodeInfoI nodeInfo) {
        return n.nodePosition().distance(nodeInfo.nodePosition()) < n.nodeRange()
                || nodeInfo.nodePosition().distance(n.nodePosition()) < nodeInfo.nodeRange()
                        && (!n.nodeIdentifier().equals(nodeInfo.nodeIdentifier()));
    }

    public Set<NodeInfoI> register(NodeInfoI nodeInfo) throws Exception {
        try { // Init the result set
            Set<NodeInfoI> result = new HashSet<>();

            // Find the neighbours of the new node in the 4 directions
            Map<Direction, NodeInfoI> directionalNeighbours = new HashMap<>();

            // Look at the hashmap of all nodes and find the neighbours of the new node
            for (NodeInfoI n : nodeIDToNodeInfoMap.values()) {
                if (inRangeOfEachOther(n, nodeInfo)) {
                    Direction nDir = nodeInfo.nodePosition().directionFrom(n.nodePosition());
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
            return result;
        } catch (Exception e) {
            throw new Exception("Error registering node " + nodeInfo.nodeIdentifier() + ".");
        }
    }

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

    public void unregister(String nodeIdentifier) throws Exception {
        try {
            this.nodeIDToNodeInfoMap.remove(nodeIdentifier);
            this.logMessage("unregistered node " + nodeIdentifier + ".");
        } catch (Exception e) {
            throw new Exception("Error unregistering node " + nodeIdentifier + ".");
        }
    }

    @Override
    public synchronized void finalise() throws Exception {
        this.logMessage("finalising Registry component.");
        this.logMessage(printAllNodes());
        super.finalise();
    }

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
