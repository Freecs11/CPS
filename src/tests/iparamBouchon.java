package tests;

import java.util.HashMap;
import java.util.Map;

import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;

public class iparamBouchon implements query.interfaces.IParamContext {
    private PositionI position;
    private String nodeId;

    private Map<String, Double> map;

    public iparamBouchon(PositionI position, String nodeId) {
        this.position = position;
        this.nodeId = nodeId;
        this.map = new HashMap<String, Double>();
        this.map.put("vent", 15.0);
        this.map.put("eau", 30.0);

    }

    @Override
    public PositionI getPosition() {
        return this.position;
    }

    @Override
    public void setPosition(PositionI pos) {
        this.position = pos;
    }

    @Override
    public String getNodeId() {
        return this.nodeId;
    }

    @Override
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public Double get(String key) {
        return this.map.get(key);
    }

    public void setKey(String key, Double value) {
        this.map.put(key, value);
    }

}
