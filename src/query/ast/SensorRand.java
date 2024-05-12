package query.ast;

import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.abstraction.AbstractRand;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>SensorRand</code> represents a sensor random value in the
 * AST.
 * </p>
 */
public class SensorRand extends AbstractRand {
	private final String sensorId;

	/**
	 * Constructor of the SensorRand
	 * 
	 * @param sensorId
	 */
	public SensorRand(String sensorId) {
		this.sensorId = sensorId;
	}

	public String getSensorId() {
		return sensorId;
	}

	/**
	 * see {@link query.interfaces.IEval#eval(ExecutionStateI) }
	 */
	@Override
	public Object eval(ExecutionStateI context) {
		SensorDataI sensorData = context.getProcessingNode().getSensorData(sensorId);
		return sensorData.getValue();
	}

}
