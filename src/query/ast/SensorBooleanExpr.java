package query.ast;

import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import implementation.request.ExecutionStateIMPL;
import query.abstraction.AbstractBooleanExpr;
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
		String nodeId = contextIMP.getProcessingNode().getNodeIdentifier();
		if (sensorData.getType() == Boolean.class) {
			Boolean resB = (Boolean) sensorData.getValue();
			if (Boolean.TRUE.equals(resB)) {
				contextIMP.addPositiveSN(nodeId);
			}
			return resB;
		}
		return false; // sensorData.getType() != Boolean.class
	}

	@Override
	public Object eval(IParamContext context) {
		return context.get(sensorId); // test is using Double for get, not using SBExp in test
	}
}
