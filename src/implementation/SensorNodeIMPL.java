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

public class SensorNodeIMPL implements RequestingImplI {
    /**
     * 
     */
    private ExecutionStateIMPL context;
    private NodeInfoI nodeInfo;
    private Map<String, SensorDataI> sensors;

    public SensorNodeIMPL(NodeInfoI nodeInfo) {
        this.nodeInfo = nodeInfo;
        PositionI position = nodeInfo.nodePosition();
        String nodeIdentifier = nodeInfo.nodeIdentifier();

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

    public SensorNodeIMPL(NodeInfoI nodeInfo, Map<String, SensorDataI> sensors) {
        this.nodeInfo = nodeInfo;
        String nodeIdentifier = nodeInfo.nodeIdentifier();
        PositionI position = nodeInfo.nodePosition();
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
    public void executeAsync(RequestI request) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'executeAsync'");
    }

}
