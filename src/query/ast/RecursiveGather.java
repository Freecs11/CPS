package query.ast;

import java.util.Map;

import abstractQuery.AbstractGather;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.interfaces.IParamContext;

public class RecursiveGather extends AbstractGather {
	private final String sensorId;
	private final AbstractGather gather;

	public RecursiveGather(String sensorId, AbstractGather gather) {
		this.sensorId = sensorId;
		this.gather = gather;
	}

	public String getSensorId() {
		return sensorId;
	}

	public AbstractGather getGather() {
		return gather;
	}

	@Override
	public Map<String, Object> eval(ExecutionStateI context) {
		@SuppressWarnings("unchecked")
		Map<String, Object> identsCont = (Map<String, Object>) gather.eval(context);
		SensorDataI sensorData = context.getProcessingNode().getSensorData(sensorId);
		Object value = sensorData.getValue();
		identsCont.put(sensorId, value);
		return identsCont;
	}

	@Override
	public Map<String, Double> eval(IParamContext context) {
		@SuppressWarnings("unchecked")
		Map<String, Double> identsCont = (Map<String, Double>) gather.eval(context);
		Double value = context.get(sensorId);
		identsCont.put(sensorId, value);
		return identsCont;
	}
}
