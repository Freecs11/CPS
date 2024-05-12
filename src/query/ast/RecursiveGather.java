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
 * The class <code>RecursiveGather</code> represents a recursive gather in the
 * AST.
 * </p>
 * 
 */
public class RecursiveGather extends AbstractGather {
	private final String sensorId;
	private final AbstractGather gather;

	/**
	 * Constructor of the RecursiveGather
	 * 
	 * @param sensorId the id of the sensor
	 * @param gather   the gather
	 */
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

	/**
	 * see {@link query.interfaces.IEval#eval(ExecutionStateI) }
	 */
	@Override
	public ArrayList<SensorDataI> eval(ExecutionStateI context) {
		ArrayList<SensorDataI> identsCont = gather.eval(context);
		SensorDataI sensorData = context.getProcessingNode().getSensorData(sensorId);
		identsCont.add(sensorData);
		return identsCont;
	}

}
