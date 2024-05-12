package query.ast;

import java.util.Set;

import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import implementation.request.ExecutionStateIMPL;
import query.abstraction.AbstractContinuation;
import query.abstraction.AbstractDirections;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>DirectionContinuation</code> represents a direction
 * continuation in the AST.
 * </p>
 * information.
 */
public class DirectionContinuation extends AbstractContinuation {
	private int jumps;
	private AbstractDirections direction;

	/**
	 * Constructor of the DirectionContinuation
	 * 
	 * @param jumps     the number of possible Jumps
	 * @param direction the direction of the continuation
	 */
	public DirectionContinuation(int jumps, AbstractDirections direction) {
		super();
		this.jumps = jumps;
		this.direction = direction;
	}

	/**
	 * Getter of the number of jumps
	 * 
	 * @return the number of jumps
	 */
	public int getJumps() {
		return jumps;
	}

	/**
	 * Setter of the number of jumps
	 * 
	 * @param jumps the number of jumps
	 */
	public void setJumps(int jumps) {
		this.jumps = jumps;
	}

	/**
	 * Getter of the direction of the continuation
	 * 
	 * @return the direction of the continuation
	 */
	public AbstractDirections getDirection() {
		return direction;
	}

	/**
	 * Setter of the direction of the continuation
	 * 
	 * @param direction the direction of the continuation to set
	 */
	public void setDirection(AbstractDirections direction) {
		this.direction = direction;
	}

	/**
	 * see {@link query.interfaces.IEval#eval(ExecutionStateI) }
	 */
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
