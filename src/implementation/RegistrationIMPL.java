package implementation;

import java.util.Map;
import java.util.Set;

import java.util.HashSet;
import java.util.HashMap;

import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI;

public class RegistrationIMPL implements RegistrationCI {
    String uri = "registration";
    private Map<String, NodeInfoI> nodesMap;

    public RegistrationIMPL() {
        this.nodesMap = new HashMap<>();
    }

    public RegistrationIMPL(Map<String, NodeInfoI> nodesMap) {
        this.nodesMap = nodesMap;
    }

    @Override
    public boolean registered(String nodeIdentifier) throws Exception {
        return this.nodesMap.containsKey(nodeIdentifier);
    }

    @Override
    public Set<NodeInfoI> register(NodeInfoI nodeInfo) throws Exception {
        Set<NodeInfoI> result = new HashSet<>();
        for (NodeInfoI n : nodesMap.values()) {
            if (n.nodePosition().distance(nodeInfo.nodePosition()) < n.nodeRange()
                    || nodeInfo.nodePosition().distance(n.nodePosition()) < nodeInfo.nodeRange()
                            && (!n.nodeIdentifier().equals(nodeInfo.nodeIdentifier()))) {
                result.add(nodeInfo);
            }
        }
        this.nodesMap.put(nodeInfo.nodeIdentifier(), nodeInfo);
        return result;
    }

    @Override
    public NodeInfoI findNewNeighbour(NodeInfoI nodeInfo, Direction d) throws Exception {
        for (NodeInfoI node : nodesMap.values()) {
            PositionI nodePosition = node.nodePosition();
            if (nodePosition.directionFrom(nodeInfo.nodePosition()) == d) {
                return node;
            }
        }
        return null;
    }

    @Override
    public void unregister(String nodeIdentifier) throws Exception {
        nodesMap.remove(nodeIdentifier);
    }

}
