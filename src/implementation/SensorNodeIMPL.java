package implementation;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import abstractQuery.AbstractQuery;
import fr.sorbonne_u.cps.sensor_network.interfaces.EndPointDescriptorI;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingImplI;
import implementation.requestsIMPL.ExecutionStateIMPL;
import implementation.requestsIMPL.ProcessingNodeIMPL;

public class SensorNodeIMPL implements RequestingImplI, NodeInfoI {
    /**
     * 
     */
    private static final long serialVersionUID = -3864106092235943836L;
    private ExecutionStateIMPL context;
    private String nodeIdentifier;
    private EndPointDescriptorI endPointInfo;
    private PositionIMPL position;
    private double range;
    private EndPointDescriptorI p2pEndPointInfo;
    private Map<String, SensorDataI> sensors;

    public SensorNodeIMPL(String nodeIdentifier, EndPointDescriptorI endPointInfo, PositionIMPL position, double range,
            EndPointDescriptorI p2pEndPointInfo) {
        this.nodeIdentifier = nodeIdentifier;
        this.endPointInfo = endPointInfo;
        this.position = position;
        this.range = range;
        this.p2pEndPointInfo = p2pEndPointInfo;
        ProcessingNodeIMPL node = new ProcessingNodeIMPL(position, null, nodeIdentifier);
        sensors = new HashMap<>();
        SensorDataIMPL sensor = new SensorDataIMPL(nodeIdentifier, "temperature", 20.0, Instant.now(), Double.class);
        SensorDataIMPL sensor2 = new SensorDataIMPL(nodeIdentifier, "humidity", 50.0, Instant.now(), Double.class);
        SensorDataIMPL sensor3 = new SensorDataIMPL(nodeIdentifier, "light", 100.0, Instant.now(), Double.class);
        sensors.put("light", sensor3);
        sensors.put("humidity", sensor2);
        sensors.put("temperature", sensor);
        node.setSensorDataMap(sensors);
        this.context = new ExecutionStateIMPL();
        this.context.updateProcessingNode(node);
    }

    public SensorNodeIMPL(String nodeIdentifier, EndPointDescriptorI endPointInfo, PositionIMPL position, double range,
            EndPointDescriptorI p2pEndPointInfo, Map<String, SensorDataI> sensors) {
        this.nodeIdentifier = nodeIdentifier;
        this.endPointInfo = endPointInfo;
        this.position = position;
        this.range = range;
        this.p2pEndPointInfo = p2pEndPointInfo;
        ProcessingNodeIMPL node = new ProcessingNodeIMPL(position, null, nodeIdentifier);
        node.setSensorDataMap(sensors);
        this.context = new ExecutionStateIMPL();
        this.context.updateProcessingNode(node);
    }

    @Override
    public QueryResultI execute(RequestI request) throws Exception {
        if (request == null) {
            throw new Exception("Request is null");
        }
        RequestIMPL req = (RequestIMPL) request;
        if (req.getQueryCode() == null) {
            throw new Exception("Query is null");
        }
        AbstractQuery query = (AbstractQuery) req.getQueryCode();
        return (QueryResultIMPL) query.eval(this.context);
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
