package query.ast;

import java.util.Set;

import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import implementation.request.ExecutionStateIMPL;
import query.abstraction.AbstractContinuation;
import query.abstraction.AbstractDirections;

public class DirectionContinuation extends AbstractContinuation {
	private int jumps;
	private AbstractDirections direction;

	public DirectionContinuation(int jumps, AbstractDirections direction) {
		super();
		this.jumps = jumps;
		this.direction = direction;
	}

	public int getJumps() {
		return jumps;
	}

	public void setJumps(int jumps) {
		this.jumps = jumps;
	}

	public AbstractDirections getDirection() {
		return direction;
	}

	public void setDirection(AbstractDirections direction) {
		this.direction = direction;
	}

	@Override
	public Object eval(ExecutionStateI context) {
		Set<Direction> directions = direction.eval(context);
		ExecutionStateIMPL contextCC = (ExecutionStateIMPL) context;
		if (jumps > 0) {
			contextCC.setMaxHops(jumps);
		}
		contextCC.setDirectional(true);
		contextCC.setDirections(directions);
		return null;
	}

}
