package itfIMP;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import abstractClass.ABSQuery;
import fr.sorbonne_u.cps.sensor_network.interfaces.EndPointDescriptorI;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingImplI;
import itfIMP.requestsItfIMP.ExecutionState;
import itfIMP.requestsItfIMP.ProcessingNodeImp;

public class SensorNode implements RequestingImplI, NodeInfoI {
    private ExecutionState context;
    private String nodeIdentifier;
    private EndPointDescriptorI endPointInfo;
    private PositionIMP position;
    private double range;
    private EndPointDescriptorI p2pEndPointInfo;
    private Map<String, SensorDataI> sensors;

    public SensorNode(String nodeIdentifier, EndPointDescriptorI endPointInfo, PositionIMP position, double range,
            EndPointDescriptorI p2pEndPointInfo) {
        this.nodeIdentifier = nodeIdentifier;
        this.endPointInfo = endPointInfo;
        this.position = position;
        this.range = range;
        this.p2pEndPointInfo = p2pEndPointInfo;
        ProcessingNodeImp node = new ProcessingNodeImp(position, null, nodeIdentifier);
        sensors = new HashMap<>();
        SensorDataIMP sensor = new SensorDataIMP(nodeIdentifier, "temperature", 20.0, Instant.now());
        SensorDataIMP sensor2 = new SensorDataIMP(nodeIdentifier, "humidity", 50.0, Instant.now());
        SensorDataIMP sensor3 = new SensorDataIMP(nodeIdentifier, "light", 100.0, Instant.now());
        sensors.put("light", sensor3);
        sensors.put("humidity", sensor2);
        sensors.put("temperature", sensor);
        node.setSensorDataMap(sensors);
        this.context = new ExecutionState();
        this.context.updateProcessingNode(node);
    }

    public SensorNode(String nodeIdentifier, EndPointDescriptorI endPointInfo, PositionIMP position, double range,
            EndPointDescriptorI p2pEndPointInfo, Map<String, SensorDataI> sensors) {
        this.nodeIdentifier = nodeIdentifier;
        this.endPointInfo = endPointInfo;
        this.position = position;
        this.range = range;
        this.p2pEndPointInfo = p2pEndPointInfo;
        ProcessingNodeImp node = new ProcessingNodeImp(position, null, nodeIdentifier);
        node.setSensorDataMap(sensors);
        this.context = new ExecutionState();
        this.context.updateProcessingNode(node);
    }

    @Override
    public QueryResultI execute(RequestI request) throws Exception {
        if (request == null) {
            throw new Exception("Request is null");
        }
        RequestIMP req = (RequestIMP) request;
        if (req.getQueryCode() == null) {
            throw new Exception("Query is null");
        }
        ABSQuery query = (ABSQuery) req.getQueryCode();
        return (QueryResultIMP) query.eval(this.context);
    }

    @Override
    public String nodeIdentifier() {
        return this.nodeIdentifier;
    }

    @Override
    public EndPointDescriptorI endPointInfo() {
        return this.endPointInfo;
    }

    @Override
    public PositionI nodePosition() {
        return this.position;
    }

    @Override
    public double nodeRange() {
        return this.range;
    }

    @Override
    public EndPointDescriptorI p2pEndPointInfo() {
        return this.p2pEndPointInfo;
    }

    @Override
    public void executeAsync(RequestI request) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'executeAsync'");
    }

}
