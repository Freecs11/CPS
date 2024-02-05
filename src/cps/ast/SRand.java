package cps.ast;

import abstractClass.ABSRand;
import cps.interfaces.IParamContext;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

public class SRand extends ABSRand {
	private final String sensorId;

	public SRand(String sensorId) {
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
