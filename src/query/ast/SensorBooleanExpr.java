package query.ast;

import abstractQuery.AbstractBooleanExpr;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import implementation.requestsIMPL.ExecutionStateIMPL;
import query.interfaces.IParamContext;

public class SensorBooleanExpr extends AbstractBooleanExpr {
	private final String sensorId;

	public SensorBooleanExpr(String sensorId) {
		this.sensorId = sensorId;
	}

	public String getSensorId() {
		return sensorId;
	}

	@Override
	public Boolean eval(ExecutionStateI context) {
		ExecutionStateIMPL contextIMP = (ExecutionStateIMPL) context;
		SensorDataI sensorData = contextIMP.getProcessingNode().getSensorData(sensorId);
		if (sensorData.getType() == Boolean.class) {

			Boolean resB = (Boolean) sensorData.getValue();
			if (resB) {
				contextIMP.addPositiveSN(sensorId);
			}
			return resB;
		}
		return null; // should throw error
	}

	@Override
	public Object eval(IParamContext context) {
		return context.get(sensorId); // test is using Double for get, not using SBExp in test
	}
}
