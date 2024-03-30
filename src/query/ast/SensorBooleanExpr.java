package query.ast;

import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import implementation.request.ExecutionStateIMPL;
import query.abstraction.AbstractBooleanExpr;

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
			return (Boolean) sensorData.getValue();
		} else {
			throw new RuntimeException("Sensor data is not of type Boolean");
		}
	}

}
