package query.ast;

import java.util.ArrayList;
import java.util.Map;

import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.abstraction.AbstractGather;

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
	public ArrayList<SensorDataI> eval(ExecutionStateI context) {
		ArrayList<SensorDataI> identsCont = gather.eval(context);
		SensorDataI sensorData = context.getProcessingNode().getSensorData(sensorId);
		identsCont.add(sensorData);
		return identsCont;
	}

}
