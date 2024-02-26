package implementation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.GeographicalZoneI;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI;

public class LookUpIMPL implements LookupCI {
    public String uri = "lookup";
    private Map<String, NodeInfoI> nodesMap;

    public LookUpIMPL() {
        this.nodesMap = new HashMap<>();
    }

    public String getURI() {
        return this.uri;
    }

    public LookUpIMPL(Map<String, NodeInfoI> hashMap) {
        this.nodesMap = hashMap;
    }

    public Map<String, NodeInfoI> getNodesMap() {
        return this.nodesMap;
    }

    public void addNodeToMap(NodeInfoI nodeInfo) {
        this.nodesMap.put(nodeInfo.nodeIdentifier(), nodeInfo);
    }

    @Override
    public ConnectionInfoI findByIdentifier(String sensorNodeId) throws Exception {
        return this.nodesMap.get(sensorNodeId);
    }

    @Override
    public Set<ConnectionInfoI> findByZone(GeographicalZoneI z) throws Exception {
        assert z != null;
        Set<ConnectionInfoI> result = new HashSet<>();
        for (NodeInfoI nodeInfo : nodesMap.values()) {
            if (z.in(nodeInfo.nodePosition())) {
                result.add(nodeInfo);
            }
        }
        return result;
    }

}
