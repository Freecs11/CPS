package cps.ast;

import java.util.HashSet;
import java.util.Set;

import abstractClass.ABSDirs;
import cps.interfaces.IParamContext;
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

public class RDirs extends ABSDirs {
	private Direction direction;
	private ABSDirs directions;

	public RDirs(Direction direction, ABSDirs directions) {
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

	public ABSDirs getDirections() {
		return directions;
	}

	public void setDirections(ABSDirs directions) {
		this.directions = directions;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<Direction> eval(ExecutionStateI context) {
		Direction dirEval = this.direction;
		HashSet<Direction> listDirs = (HashSet<Direction>) this.directions.eval(context);
		listDirs.add(dirEval);
		return listDirs;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<Direction> eval(IParamContext context) {
		Direction dirEval = this.direction;

		HashSet<Direction> listDirs = (HashSet<Direction>) this.directions.eval(context);
		listDirs.add(dirEval);
		return listDirs;
	}

}
