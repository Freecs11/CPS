package query.ast;

import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.abstraction.AbstractRand;
import query.interfaces.IParamContext;

public class SensorRand extends AbstractRand {
	private final String sensorId;

	public SensorRand(String sensorId) {
		this.sensorId = sensorId;
	}

	public String getSensorId() {
		return sensorId;
	}

	@Override
	public Object eval(ExecutionStateI context) {
		SensorDataI sensorData = context.getProcessingNode().getSensorData(sensorId);
		return sensorData.getValue();
	}

	@Override
	public Object eval(IParamContext context) {
		return context.get(sensorId);
	}

}
