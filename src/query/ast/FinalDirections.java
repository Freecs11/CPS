package query.ast;

import java.util.HashSet;
import java.util.Set;

import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import implementation.requestsIMPL.ExecutionStateIMPL;
import query.abstraction.AbstractDirections;
import query.interfaces.IParamContext;

public class FinalDirections extends AbstractDirections {

	private Direction direction;

	public FinalDirections(Direction direction) {
		this.direction = direction;
	}

	@Override
	public Set<Direction> eval(ExecutionStateI context) {
		Direction dir = this.direction;
		Set<Direction> dirs = new HashSet<Direction>();
		dirs.add(dir);
		((ExecutionStateIMPL) context).setCurrentDirection(dir);
		return dirs;
	}

	@Override
	public Set<Direction> eval(IParamContext context) {
		Direction dir = this.direction;
		Set<Direction> dirs = new HashSet<Direction>();
		dirs.add(dir);
		return dirs;
	}
}
