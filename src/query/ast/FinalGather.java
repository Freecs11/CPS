package query.ast;

import java.util.HashMap;
import java.util.Map;

import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.abstraction.AbstractGather;
import query.interfaces.IParamContext;

public class FinalGather extends AbstractGather {
	private final String sensorId;

	public FinalGather(String sensorId) {
		this.sensorId = sensorId;
	}

	public String getSensorId() {
		return sensorId;
	}

	@Override
	public Map<String, Object> eval(ExecutionStateI context) {
		Map<String, Object> idents = new HashMap<>();
		SensorDataI sensorData = context.getProcessingNode().getSensorData(sensorId);
		Object value = sensorData.getValue();
		idents.put(sensorId, value);
		return idents;
	}

	@Override
	public Map<String, Double> eval(IParamContext context) {
		Map<String, Double> idents = new HashMap<>();
		Double value = context.get(sensorId);
		idents.put(sensorId, value);
		return idents;
	}
}
