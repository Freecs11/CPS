package itfIMP;

import java.util.ArrayList;

import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;

public class queryResultIMP implements QueryResultI {

	private static final long serialVersionUID = 1L;
	private Object result;

	private boolean isBR;

	private boolean isGR;
	private ArrayList<String> positiveSN;
	private ArrayList<SensorDataI> gatheredSensors;

	public queryResultIMP(boolean isBR, boolean isGR, ArrayList<String> positiveSN,
			ArrayList<SensorDataI> gatheredSensors) {
		this.isBR = isBR;
		this.isGR = isGR;
		this.positiveSN = positiveSN;
		this.gatheredSensors = gatheredSensors;
	}

	@Override
	public boolean isBooleanRequest() {
		// TODO Auto-generated method stub
		return isBR;
	}

	@Override
	public ArrayList<String> positiveSensorNodes() {
		if (this.isBooleanRequest()) {
			return positiveSN;
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
			return gatheredSensors;
		}
		return new ArrayList<>();
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public boolean isBR() {
		return isBR;
	}

	public void setBR(boolean isBR) {
		this.isBR = isBR;
	}

	public boolean isGR() {
		return isGR;
	}

	public void setGR(boolean isGR) {
		this.isGR = isGR;
	}

	public ArrayList<String> getPositiveSN() {
		return positiveSN;
	}

	public void setPositiveSN(ArrayList<String> positiveSN) {
		this.positiveSN = positiveSN;
	}

	public ArrayList<SensorDataI> getGatheredSensors() {
		return gatheredSensors;
	}

	public void setGatheredSensors(ArrayList<SensorDataI> gatheredSensors) {
		if (this.gatheredSensors == null) {
			this.gatheredSensors = new ArrayList<>();
		} else {
			this.gatheredSensors.addAll(gatheredSensors);
		}
	}

	public void addToGatheredSensors(SensorDataI sensorData) {
		if (this.gatheredSensors == null) {
			this.gatheredSensors = new ArrayList<>();
		}
		this.gatheredSensors.add(sensorData);
	}

	public void update(QueryResultI query) {
		if (!query.isBooleanRequest() && query.isGatherRequest()) {
			gatheredSensors.addAll(query.gatheredSensorsValues());
		} else if (query.isBooleanRequest() && !query.isGatherRequest()) {
			positiveSN.addAll(query.positiveSensorNodes());
		}
	}

	public void addPositiveSN(String sensorId) {
		if (this.positiveSN == null) {
			this.positiveSN = new ArrayList<>();
		}
		this.positiveSN.add(sensorId);
	}
}
