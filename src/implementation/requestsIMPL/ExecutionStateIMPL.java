package implementation.requestsIMPL;

import java.util.HashSet;
import java.util.Set;

import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;
import implementation.QueryResultIMPL;

public class ExecutionStateIMPL implements ExecutionStateI {

	private ProcessingNodeI processingNode;
	private QueryResultIMPL queryResult;
	private Boolean isDirectional = null;
	private Set<Direction> directions;
	private Integer maxHops = null;
	private int hops = 0;
	private Boolean isFlooding = null;
	private Double maxDistance = null;
	private Set<String> positiveSNG;

	public ExecutionStateIMPL(ProcessingNodeI processingNode) {
		this.positiveSNG = new HashSet<>();
		this.queryResult = new QueryResultIMPL();
		this.processingNode = processingNode;
	}

	public ExecutionStateIMPL() {
		this.positiveSNG = new HashSet<>();
		this.queryResult = new QueryResultIMPL();
	}

	public Set<String> getPositiveSNG() {
		return positiveSNG;
	}

	public void setPositiveSNG(Set<String> positiveSNG) {
		this.positiveSNG = positiveSNG;
	}

	public void setMaxHops(int hops) {
		if (this.maxHops == null) {
			this.maxHops = hops;
		}
	}

	public void setDirectional(boolean isDirectional) {
		if (this.isDirectional == null) {
			this.isDirectional = isDirectional;
		}
	}

	public void setIsFlooding(boolean isFlooding) {
		if (this.isFlooding == null) {
			this.isFlooding = isFlooding;
		}
	}

	public void setMaxDistance(Double maxDistance) {
		if (this.maxDistance == null) {
			this.maxDistance = maxDistance;
		}
	}

	public Double getMaxDistance() {
		return this.maxDistance;
	}

	public void updateMaxDistance(Double maxDistance) {
		if (this.maxDistance != null) {
			this.maxDistance = maxDistance;
		}
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
			queryResult.update(result);
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
		}
		return null;
	}

	@Override
	public boolean noMoreHops() {
		if (this.isDirectional()) {
			return hops >= maxHops;
		}
		return false;
	}

	@Override
	public void incrementHops() {
		if (this.isDirectional()) {
			this.hops++;
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
		}
		return false;
	}

	public void addPositiveSN(String sn) {
		this.positiveSNG.add(sn);
	}

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
				", positiveSNG=" + positiveSNG +
				'}';
	}

}
