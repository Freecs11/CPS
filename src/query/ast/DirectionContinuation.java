package query.ast;

import java.util.Set;

import abstractQuery.AbstractContinuation;
import abstractQuery.AbstractDirections;
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import implementation.requestsIMPL.ExecutionStateIMPL;
import query.interfaces.IParamContext;

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

	@SuppressWarnings("unchecked")
	@Override
	public Object eval(ExecutionStateI context) {
		Set<Direction> directions = (Set<Direction>) direction.eval(context);
		ExecutionStateIMPL contextCC = (ExecutionStateIMPL) context;
		contextCC.setMaxHops(jumps);
		contextCC.setDirectional(true);
		contextCC.setIsFlooding(false);
		contextCC.setDirections(directions);
		return null;
	}

	@Override
	public Object eval(IParamContext context) {
		// TODO Auto-generated method stub
		return null;
	}
}
