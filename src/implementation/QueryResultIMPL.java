package implementation;

import java.util.ArrayList;
import java.util.List;

import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p> The class <code>QueryResultIMPL</code> 
 * acts as the implementation of the <code>QueryResultI</code> interface. 
 * It is used to store the result of a query.
 * </p>
 */
public class QueryResultIMPL implements QueryResultI {
	private boolean isBR;
	private boolean isGR;
	private List<String> positiveSN;
	private List<SensorDataI> gatheredSensors;

	/**
	 * Constructor of the QueryResultIMPL
	 */
	public QueryResultIMPL() {
		this.isBR = false;
		this.isGR = false;
		this.positiveSN = new ArrayList<>();
		this.gatheredSensors = new ArrayList<>();
	}

	/**
	 * Constructor of the QueryResultIMPL
	 * 
	 * @param isBR           information if the query is the boolean query
	 * @param isGR           information if the query is the gather query
	 * @param positiveSN     list of positive sensor nodes that are valid for the query
	 * @param gatheredSensors list of gathered sensors that are concerned with the query
	 */
	public QueryResultIMPL(boolean isBR, boolean isGR, List<String> positiveSN,
			List<SensorDataI> gatheredSensors) {
		this.isBR = isBR;
		this.isGR = isGR;
		this.positiveSN = positiveSN;
		this.gatheredSensors = gatheredSensors;
	}

	/**
	 * Copy constructor of the QueryResultIMPL
	 * 
	 * @param query the query to copy
	 */
	public QueryResultIMPL(QueryResultI query) {
		this.isBR = query.isBooleanRequest();
		this.isGR = query.isGatherRequest();
		this.positiveSN = new ArrayList<>(query.positiveSensorNodes());
		this.gatheredSensors = new ArrayList<>(query.gatheredSensorsValues());
	}

	/**
	 * see {@link fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI#isBooleanRequest()}
	 */
	@Override
	public boolean isBooleanRequest() {
		return isBR;
	}

	/**
	 * see {@link fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI#positiveSensorNodes()}
	 */
	@Override
	public ArrayList<String> positiveSensorNodes() {
		if (this.isBooleanRequest()) {
			return (ArrayList<String>) positiveSN;
		}
		return new ArrayList<>();
	}

	/**
	 * see {@link fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI#isGatherRequest()}
	 */
	@Override
	public boolean isGatherRequest() {
		return isGR;
	}

	/**
	 * see {@link fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI#gatheredSensorsValues()}
	 */
	@Override
	public ArrayList<SensorDataI> gatheredSensorsValues() {
		if (this.isGatherRequest()) {
			return (ArrayList<SensorDataI>) gatheredSensors;
		}
		return new ArrayList<>();
	}

	/**
	 * Setter for the boolean type request
	 * @param isBR
	 */
	public void setBR(boolean isBR) {
		this.isBR = isBR;
	}

	/**
	 * Setter for the gather type request
	 * @param isGR
	 */
	public void setGR(boolean isGR) {
		this.isGR = isGR;
	}

	/**
	 * Getter for the positive sensor of the query
	 * @return the positive sensor who validated the query
	 */
	public List<String> getPositiveSN() {
		return positiveSN;
	}

	/**
	 * Setter for the positive sensor of the query
	 * @param positiveSN the new List of positive sensor 
	 */
	public void setPositiveSN(List<String> positiveSN) {
		this.positiveSN = positiveSN;
	}

	/**
	 * Getter for the gathered sensors of the query
	 * @return the gathered sensors of the query
	 */
	public List<SensorDataI> getGatheredSensors() {
		return gatheredSensors;
	}

	/**
	 * Setter for the gathered sensors of the query
	 * @param gatheredSensors the new List of gathered sensors
	 */
	public void setGatheredSensors(List<SensorDataI> gatheredSensors) {
		this.gatheredSensors = gatheredSensors;
	}

	/**
	 * Add a sensor to the gathered sensors list
	 * @param sensorData the sensor to add
	 */
	public void addToGatheredSensors(SensorDataI sensorData) {
		if (this.gatheredSensors == null) {
			this.gatheredSensors = new ArrayList<>();
		}
		if (!isSensorDataPresent(sensorData)) {
			this.gatheredSensors.add(sensorData);
		}
	}

	/**
	 * Check if the sensor is already present in the gathered sensors list
	 * 
	 * @param sensorData the sensor to check
	 * @return true if the sensor is present, false otherwise
	 */
	private boolean isSensorDataPresent(SensorDataI sensorData) {
		for (SensorDataI sensor : this.gatheredSensors) {
			if (sensor.getNodeIdentifier().equals(sensorData.getNodeIdentifier())
					&& sensor.getSensorIdentifier().equals(sensorData.getSensorIdentifier())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Update the query result with the new query
	 * 
	 * @param query the new query to update the result with
	 */
	public void update(QueryResultI query) {
		if (!query.isBooleanRequest() && query.isGatherRequest()) {
			processGatherRequest(query);
		} else if (query.isBooleanRequest() && !query.isGatherRequest()) {
			processBooleanRequest(query);
		}
	}

	/**
	 * Add the gathered sensors of Query with other queries
	 * 
	 * @param query the gather sonsors query to add
	 */
	private void processGatherRequest(QueryResultI query) {
		ArrayList<SensorDataI> gathered = query.gatheredSensorsValues();
		for (SensorDataI sensor : gathered) {
			this.addToGatheredSensors(sensor);
		}
	}

	/**
	 * Add the positive sensor of Query with other queries
	 * 
	 * @param query the positive sensor query to add
	 */
	private void processBooleanRequest(QueryResultI query) {
		ArrayList<String> positive = query.positiveSensorNodes();
		if (positive != null) {
			for (String sn : positive) {
				this.addPositiveSN(sn);
			}
		}
	}

	/**
	 * Add a positive sensor to the query
	 * 
	 * @param sensorId the sensor to add
	 */
	public void addPositiveSN(String sensorId) {
		if (this.positiveSN == null) {
			this.positiveSN = new ArrayList<>();
		}
		if (!this.positiveSN.contains(sensorId)) {
			this.positiveSN.add(sensorId);
		}
	}

	/**
	 * Descriptions of the query result 
	 */
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
