package query.ast;

import java.util.Set;

import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.abstraction.AbstractDirections;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>RecursiveDirections</code> represents a recursive direction
 * in the AST.
 * </p>
 */
public class RecursiveDirections extends AbstractDirections {
	private Direction direction;
	private AbstractDirections directions;

	/**
	 * Constructor of the RecursiveDirections
	 * 
	 * @param direction  the direction
	 * @param directions the directions
	 */
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

	/**
	 * see {@link query.interfaces.IEval#eval(ExecutionStateI) }
	 */
	@Override
	public Set<Direction> eval(ExecutionStateI context) {
		Direction dirEval = this.direction;
		Set<Direction> listDirs = this.directions.eval(context);
		listDirs.add(dirEval);
		return listDirs;
	}

}
