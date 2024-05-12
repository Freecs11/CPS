package implementation.request;

import java.util.Set;

import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;
import implementation.QueryResultIMPL;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>ExecutionStateIMPL</code> acts as the implementation of the
 * <code>ExecutionStateI</code> interface. It is used to store the state of the
 * execution of a request on a node.
 * </p>
 * See the interface
 * {@link fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI}
 */
public class ExecutionStateIMPL implements ExecutionStateI {

	private static final long serialVersionUID = -6858748451128986487L;
	private ProcessingNodeI processingNode;
	private QueryResultI queryResult;
	private boolean isDirectional;
	private Set<Direction> directions;
	private int maxHops = -1;
	private int hops = 0;
	private boolean isFlooding;
	private double maxDistance = -1;

	/**
	 * Constructor of the ExecutionStateIMPL
	 * 
	 * @param processingNode the processing node
	 */
	public ExecutionStateIMPL(ProcessingNodeI processingNode) {
		this.queryResult = new QueryResultIMPL();
		this.processingNode = processingNode;
	}

	/**
	 * Constructor of the ExecutionStateIMPL
	 */
	public ExecutionStateIMPL() {
		this.queryResult = new QueryResultIMPL();
	}

	/**
	 * Constructor of the ExecutionStateIMPL
	 * 
	 * @param state the state to copy
	 */
	public ExecutionStateIMPL(ExecutionStateI state) {
		this.queryResult = new QueryResultIMPL(state.getCurrentResult());
		this.processingNode = state.getProcessingNode();
		this.maxHops = ((ExecutionStateIMPL) state).getMaxHops();
		this.hops = ((ExecutionStateIMPL) state).getHops();
		this.isFlooding = ((ExecutionStateIMPL) state).isFlooding();
		this.maxDistance = ((ExecutionStateIMPL) state).getMaxDistance();
		this.isDirectional = ((ExecutionStateIMPL) state).isDirectional();
		if (state.isDirectional()) {
			this.directions = state.getDirections();
		}

	}

	/**
	 * Set the maximum number of hops
	 * 
	 * @param hops
	 */
	public void setMaxHops(int hops) {
		if (this.maxHops == -1) {
			this.maxHops = hops;
		}
	}

	/**
	 * Set the maximum number of hops
	 */
	public void setDirectional(boolean isDirectional) {
		if (isContinuationSet() == false) {
			this.isDirectional = isDirectional;
		}
	}

	/**
	 * Set the maximum number of hops
	 * 
	 * @param isFlooding
	 */
	public void setIsFlooding(boolean isFlooding) {
		if (isContinuationSet() == false) {
			this.isFlooding = isFlooding;
		}
	}

	/**
	 * Set the maximum number of hops
	 * 
	 * @param maxDistance
	 */
	public void setMaxDistance(Double maxDistance) {
		if (this.maxDistance == -1) {
			this.maxDistance = maxDistance;
		}
	}

	/**
	 * Get the maximum number of hops
	 * 
	 * @return maxDistance
	 * 
	 */
	public double getMaxDistance() {
		return this.maxDistance;
	}

	/**
	 * Get the maximum number of hops
	 */
	public int getMaxHops() {
		return this.maxHops;
	}

	/**
	 * Update the maximum number of hops
	 * 
	 * @params maxDistance
	 */
	public void updateMaxDistance(Double maxDistance) {
		this.maxDistance = maxDistance;
	}

	/**
	 * Update the maximum number of hops
	 * 
	 * @param directions
	 */
	public void setDirections(Set<Direction> directions) {
		if (this.directions == null) {
			this.directions = directions;
		} else {
			this.directions.addAll(directions);
		}
	}

	/**
	 * See
	 * {@link fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI#getProcessingNode()}
	 */
	@Override
	public ProcessingNodeI getProcessingNode() {
		return processingNode;
	}

	/**
	 * See
	 * {@link fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI#updateProcessingNode(ProcessingNodeI)}
	 * 
	 */
	@Override
	public void updateProcessingNode(ProcessingNodeI pn) {
		this.processingNode = pn;
	}

	/**
	 * See
	 * {@link fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI#getCurrentResult()}
	 * 
	 */
	@Override
	public QueryResultI getCurrentResult() {
		return queryResult;
	}

	/**
	 * See
	 * {@link fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI#addToCurrentResult(QueryResultI)}
	 * 
	 */
	@Override
	public void addToCurrentResult(QueryResultI result) {
		if (getCurrentResult().isBooleanRequest() == result.isBooleanRequest()
				&& getCurrentResult().isGatherRequest() == result.isGatherRequest()) {
			((QueryResultIMPL) queryResult).update(result);
		} else {
			throw new IllegalStateException("Cannot add result of different type");
		}
	}

	/**
	 * See
	 * {@link fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI#isDirectional()}
	 */
	@Override
	public boolean isDirectional() {
		return this.isDirectional;
	}

	/**
	 * See
	 * {@link fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI#getDirections()}
	 */
	@Override
	public Set<Direction> getDirections() {
		if (this.isDirectional()) {
			return this.directions;
		} else {
			throw new IllegalStateException("Cannot get directions if not directional");
		}
	}

	/**
	 * See
	 * {@link fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI#noMoreHops()}
	 * 
	 */
	@Override
	public boolean noMoreHops() {
		if (this.isDirectional()) {
			return hops >= maxHops;
		} else {
			throw new IllegalStateException("Cannot check for hops if not directional");
		}
	}

	/**
	 * See
	 * {@link fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI#incrementHops()}
	 * 
	 */
	@Override
	public void incrementHops() {
		if (this.isDirectional()) {
			this.hops++;
		} else {
			throw new IllegalStateException("Cannot increment hops if not directional");
		}
	}

	/**
	 * See
	 * {@link fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI#isFlooding()}
	 * 
	 */
	@Override
	public boolean isFlooding() {
		return this.isFlooding;
	}

	/**
	 * See
	 * {@link fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI#withinMaximalDistance(PositionI)}
	 * 
	 */
	@Override
	public boolean withinMaximalDistance(PositionI p) {
		if (this.isFlooding() && p != null) {
			PositionI currentPos = processingNode.getPosition();
			return maxDistance >= p.distance(currentPos);
		} else {
			throw new IllegalStateException("Cannot check for distance if not flooding");
		}
	}

	/**
	 * See
	 * {@link fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI#isContinuationSet()}
	 * 
	 */
	@Override
	public boolean isContinuationSet() {
		return this.isFlooding() || this.isDirectional();
	}

	/**
	 * See
	 * {@link fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI#toString()}
	 */
	@Override
	public String toString() {
		return "ExecutionStateIMPL{" +
				"processingNode=" + processingNode +
				", queryResult=" + queryResult +
				", isDirectional=" + isDirectional +
				", directions=" + directions +
				", maxHops=" + maxHops +
				", hops=" + hops +
				", isFlooding=" + isFlooding +
				", maxDistance=" + maxDistance +
				'}';
	}

	/**
	 * Get the number of hops
	 * 
	 * @return hops
	 */
	public int getHops() {
		return hops;
	}

}
