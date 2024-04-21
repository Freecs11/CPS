package implementation;

import java.util.ArrayList;
import java.util.List;

import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;

public class QueryResultIMPL implements QueryResultI {
	private boolean isBR;
	private boolean isGR;
	private List<String> positiveSN;
	private List<SensorDataI> gatheredSensors;

	public QueryResultIMPL() {
		this.isBR = false;
		this.isGR = false;
		this.positiveSN = new ArrayList<>();
		this.gatheredSensors = new ArrayList<>();
	}

	public QueryResultIMPL(boolean isBR, boolean isGR, List<String> positiveSN,
			List<SensorDataI> gatheredSensors) {
		this.isBR = isBR;
		this.isGR = isGR;
		this.positiveSN = positiveSN;
		this.gatheredSensors = gatheredSensors;
	}

	public QueryResultIMPL(QueryResultI query) {
		this.isBR = query.isBooleanRequest();
		this.isGR = query.isGatherRequest();
		this.positiveSN = new ArrayList<>(query.positiveSensorNodes());
		this.gatheredSensors = new ArrayList<>(query.gatheredSensorsValues());
	}

	@Override
	public boolean isBooleanRequest() {
		return isBR;
	}

	@Override
	public ArrayList<String> positiveSensorNodes() {
		if (this.isBooleanRequest()) {
			return (ArrayList<String>) positiveSN;
		}
		return new ArrayList<>();
	}

	@Override
	public boolean isGatherRequest() {
		return isGR;
	}

	@Override
	public ArrayList<SensorDataI> gatheredSensorsValues() {
		if (this.isGatherRequest()) {
			return (ArrayList<SensorDataI>) gatheredSensors;
		}
		return new ArrayList<>();
	}

	public void setBR(boolean isBR) {
		this.isBR = isBR;
	}

	public void setGR(boolean isGR) {
		this.isGR = isGR;
	}

	public List<String> getPositiveSN() {
		return positiveSN;
	}

	public void setPositiveSN(List<String> positiveSN) {
		this.positiveSN = positiveSN;
	}

	public List<SensorDataI> getGatheredSensors() {
		return gatheredSensors;
	}

	public void setGatheredSensors(List<SensorDataI> gatheredSensors) {
		this.gatheredSensors = gatheredSensors;
	}

	public void addToGatheredSensors(SensorDataI sensorData) {
		if (this.gatheredSensors == null) {
			this.gatheredSensors = new ArrayList<>();
		}
		if (!isSensorDataPresent(sensorData)) {
			this.gatheredSensors.add(sensorData);
		}
	}

	private boolean isSensorDataPresent(SensorDataI sensorData) {
		for (SensorDataI sensor : this.gatheredSensors) {
			if (sensor.getNodeIdentifier().equals(sensorData.getNodeIdentifier())
					&& sensor.getSensorIdentifier().equals(sensorData.getSensorIdentifier())) {
				return true;
			}
		}
		return false;
	}

	public void update(QueryResultI query) {
		if (!query.isBooleanRequest() && query.isGatherRequest()) {
			processGatherRequest(query);
		} else if (query.isBooleanRequest() && !query.isGatherRequest()) {
			processBooleanRequest(query);
		}
	}

	private void processGatherRequest(QueryResultI query) {
		ArrayList<SensorDataI> gathered = query.gatheredSensorsValues();
		for (SensorDataI sensor : gathered) {
			this.addToGatheredSensors(sensor);
		}
	}

	private void processBooleanRequest(QueryResultI query) {
		ArrayList<String> positive = query.positiveSensorNodes();
		if (positive != null) {
			for (String sn : positive) {
				this.addPositiveSN(sn);
			}
		}
	}

	public void addPositiveSN(String sensorId) {
		if (this.positiveSN == null) {
			this.positiveSN = new ArrayList<>();
		}
		if (!this.positiveSN.contains(sensorId)) {
			this.positiveSN.add(sensorId);
		}
	}

	@Override
	public String toString() {
		// print the result of the query in a readable format
		if (this.isBooleanRequest()) {
			if (this.positiveSN.isEmpty()) {
				return "No positive nodes to the query";
			}
			StringBuilder bld = new StringBuilder();
			bld.append("\nPositive Nodes:--->| \u2193 \n");
			for (String sn : this.positiveSN) {
				bld.append("  |--- " + sn + "\n");
			}
			return bld.toString();
		} else if (this.isGatherRequest()) {
			StringBuilder bld = new StringBuilder();
			bld.append("\nGathered Sensors:--->| \u2193 \n");
			for (SensorDataI sensor : this.gatheredSensors) {
				bld.append("  |--- " + sensor.toString() + "\n");
			}
			return bld.toString();
		}
		return "No result" + "isGR: " + this.isGR + " isBR: " + this.isBR;
	}
}
