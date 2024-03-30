package query.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.abstraction.AbstractGather;

public class FinalGather extends AbstractGather {
	private final String sensorId;

	public FinalGather(String sensorId) {
		this.sensorId = sensorId;
	}

	public String getSensorId() {
		return sensorId;
	}

	// On comprend pas la remarque , puisqu'on voulait retourner les sensorData avec
	// leurs valeurs
	@Override
	public ArrayList<SensorDataI> eval(ExecutionStateI context) {
		ArrayList<SensorDataI> sensorDataList = new ArrayList<SensorDataI>();
		SensorDataI sensorData = context.getProcessingNode().getSensorData(sensorId);
		sensorDataList.add(sensorData);
		return sensorDataList;
	}

}
