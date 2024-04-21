package implementation;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;

public class QueryResultIMPL implements QueryResultI {
	private boolean isBR;
	private boolean isGR;
	private CopyOnWriteArrayList<String> positiveSN;
	private CopyOnWriteArrayList<SensorDataI> gatheredSensors;

	public QueryResultIMPL() {
		this.isBR = false;
		this.isGR = false;
		this.positiveSN = new CopyOnWriteArrayList<>();
		this.gatheredSensors = new CopyOnWriteArrayList<>();
	}

	public QueryResultIMPL(boolean isBR, boolean isGR, ArrayList<String> positiveSN,
			ArrayList<SensorDataI> gatheredSensors) {
		this.isBR = isBR;
		this.isGR = isGR;
		this.positiveSN = new CopyOnWriteArrayList<>(positiveSN);
		this.gatheredSensors = new CopyOnWriteArrayList<>(gatheredSensors);
	}

	@Override
	public boolean isBooleanRequest() {
		return isBR;
	}

	@Override
	public ArrayList<String> positiveSensorNodes() {
		if (this.isBooleanRequest()) {
			return new ArrayList<>(positiveSN);
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
			return new ArrayList<>(gatheredSensors);
		}
		return new ArrayList<>();
	}

	public void setBR(boolean isBR) {
		this.isBR = isBR;
	}

	public void setGR(boolean isGR) {
		this.isGR = isGR;
	}

	public ArrayList<String> getPositiveSN() {
		return new ArrayList<>(positiveSN);
	}

	public void setPositiveSN(ArrayList<String> positiveSN) {
		this.positiveSN = new CopyOnWriteArrayList<>(positiveSN);
	}

	public ArrayList<SensorDataI> getGatheredSensors() {
		return new ArrayList<>(gatheredSensors);
	}

	public void setGatheredSensors(ArrayList<SensorDataI> gatheredSensors) {
		this.gatheredSensors = new CopyOnWriteArrayList<>(gatheredSensors);
	}

	public void addToGatheredSensors(SensorDataI sensorData) {
		if (this.gatheredSensors == null) {
			this.gatheredSensors = new CopyOnWriteArrayList<>();
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
			// System.err.println("gathered sensors: " + this.gatheredSensors.toString());
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
			this.positiveSN = new CopyOnWriteArrayList<>();
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
