package tests;

import java.util.Map;

public class iparamBouchon implements cps.interfaces.IParamContext {
    private cps.ast.Position position;
    private String nodeId;

    Map<String, Double> map;

    public iparamBouchon(cps.ast.Position position, String nodeId) {
        this.position = position;
        this.nodeId = nodeId;
        this.map = Map.of("vent", 15.0, "eau", 30.0);
    }

    @Override
    public cps.ast.Position getPosition() {
        return this.position;
    }

    @Override
    public void setPosition(cps.ast.Position pos) {
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
