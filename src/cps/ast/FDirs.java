package cps.ast;

import java.util.HashSet;
import java.util.Set;

import abstractClass.ABSDirs;
import cps.interfaces.IParamContext;
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

public class FDirs extends ABSDirs {

	private Direction direction;

	public FDirs(Direction direction) {
		this.direction = direction;
	}

	@Override
	public Set<Direction> eval(ExecutionStateI context) {
		Direction dir = this.direction;
		Set<Direction> dirs = new HashSet<Direction>();
		dirs.add(dir);
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
