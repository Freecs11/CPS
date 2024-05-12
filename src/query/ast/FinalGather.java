package query.ast;

import java.util.ArrayList;

import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.abstraction.AbstractGather;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>FinalGather</code> represents the final gather in the AST.
 * </p>
 * information.
 */
public class FinalGather extends AbstractGather {
	private final String sensorId;

	/**
	 * Constructor of the FinalGather
	 * 
	 * @param sensorId the id of the sensor
	 */
	public FinalGather(String sensorId) {
		this.sensorId = sensorId;
	}

	/**
	 * Getter of the sensor id
	 * 
	 * @return the sensor id
	 */
	public String getSensorId() {
		return sensorId;
	}

	/**
	 * see {@link query.interfaces.IEval#eval(ExecutionStateI) }
	 */
	@Override
	public ArrayList<SensorDataI> eval(ExecutionStateI context) {
		ArrayList<SensorDataI> sensorDataList = new ArrayList<SensorDataI>();
		SensorDataI sensorData = context.getProcessingNode().getSensorData(sensorId);
		sensorDataList.add(sensorData);
		return sensorDataList;
	}

}
