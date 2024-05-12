package query.ast;

import java.util.HashSet;
import java.util.Set;

import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.abstraction.AbstractDirections;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>FinalDirections</code> represents the final directions in the
 * AST.
 * </p>
 * information.
 */
public class FinalDirections extends AbstractDirections {

	private Direction direction;

	/**
	 * Constructor of the FinalDirections
	 * 
	 * @param direction the direction of the final directions
	 */
	public FinalDirections(Direction direction) {
		this.direction = direction;
	}

	/**
	 * see {@link FinalDirections#eval(ExecutionStateI)}
	 */
	@Override
	public Set<Direction> eval(ExecutionStateI context) {
		Direction dir = this.direction;
		Set<Direction> dirs = new HashSet<Direction>();
		dirs.add(dir);
		return dirs;
	}

}
