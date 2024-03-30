package implementation.request;

import java.util.Set;

import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;
import implementation.QueryResultIMPL;

public class ExecutionStateIMPL implements ExecutionStateI {

	private ProcessingNodeI processingNode;
	private QueryResultI queryResult;
	private boolean isDirectional;
	private Set<Direction> directions;
	private int maxHops = -1;
	private int hops = 0;
	private boolean isFlooding;
	private double maxDistance = -1;

	public ExecutionStateIMPL(ProcessingNodeI processingNode) {
		this.queryResult = new QueryResultIMPL();
		this.processingNode = processingNode;
	}

	public ExecutionStateIMPL() {
		this.queryResult = new QueryResultIMPL();
	}

	public void setMaxHops(int hops) {
		if (this.maxHops == -1) {
			this.maxHops = hops;
		}
	}

	public void setDirectional(boolean isDirectional) {
		if (isContinuationSet() == false) {
			this.isDirectional = isDirectional;
		}
	}

	public void setIsFlooding(boolean isFlooding) {
		if (isContinuationSet() == false) {
			this.isFlooding = isFlooding;
		} else {
			throw new IllegalStateException("Cannot set flooding or directional if continuation is set");
		}
	}

	public void setMaxDistance(Double maxDistance) {
		if (this.maxDistance == -1) {
			this.maxDistance = maxDistance;
		}
	}

	public double getMaxDistance() {
		return this.maxDistance;
	}

	public void updateMaxDistance(Double maxDistance) {
		this.maxDistance = maxDistance;
	}

	public void setDirections(Set<Direction> directions) {
		if (this.directions == null) {
			this.directions = directions;
		} else {
			this.directions.addAll(directions);
		}
	}

	@Override
	public ProcessingNodeI getProcessingNode() {
		return processingNode;
	}

	@Override
	public void updateProcessingNode(ProcessingNodeI pn) {
		this.processingNode = pn;
	}

	@Override
	public QueryResultI getCurrentResult() {
		return queryResult;
	}

	@Override
	public void addToCurrentResult(QueryResultI result) {
		if (getCurrentResult().isBooleanRequest() == result.isBooleanRequest()
				&& getCurrentResult().isGatherRequest() == result.isGatherRequest()) {
			((QueryResultIMPL) queryResult).update(result);
		} else {
			throw new IllegalStateException("Cannot add result of different type");
		}
	}

	@Override
	public boolean isDirectional() {
		return this.isDirectional;
	}

	@Override
	public Set<Direction> getDirections() {
		if (this.isDirectional()) {
			return this.directions;
		} else {
			throw new IllegalStateException("Cannot get directions if not directional");
		}
	}

	@Override
	public boolean noMoreHops() {
		if (this.isDirectional()) {
			return hops >= maxHops;
		} else {
			throw new IllegalStateException("Cannot check for hops if not directional");
		}
	}

	@Override
	public void incrementHops() {
		if (this.isDirectional()) {
			this.hops++;
		} else {
			throw new IllegalStateException("Cannot increment hops if not directional");
		}
	}

	@Override
	public boolean isFlooding() {
		return this.isFlooding;
	}

	@Override
	public boolean withinMaximalDistance(PositionI p) {
		if (this.isFlooding() && p != null) {
			PositionI currentPos = processingNode.getPosition();
			return maxDistance >= p.distance(currentPos);
		} else {
			throw new IllegalStateException("Cannot check for distance if not flooding");
		}
	}

	@Override
	public boolean isContinuationSet() {
		return this.isFlooding() || this.isDirectional();
	}

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

}
