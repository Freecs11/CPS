package query.ast;

import java.util.Set;

import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.abstraction.AbstractDirections;

public class RecursiveDirections extends AbstractDirections {
	private Direction direction;
	private AbstractDirections directions;

	public RecursiveDirections(Direction direction, AbstractDirections directions) {
		super();
		this.direction = direction;
		this.directions = directions;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public AbstractDirections getDirections() {
		return directions;
	}

	public void setDirections(AbstractDirections directions) {
		this.directions = directions;
	}

	@Override
	public Set<Direction> eval(ExecutionStateI context) {
		Direction dirEval = this.direction;
		Set<Direction> listDirs = this.directions.eval(context);
		listDirs.add(dirEval);
		return listDirs;
	}

}
