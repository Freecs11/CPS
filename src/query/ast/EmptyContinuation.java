package query.ast;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.abstraction.AbstractContinuation;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>EmptyContinuation</code> represents an empty continuation in
 * the AST. it used to represent the end of a query.
 * </p>
 * information.
 */
public class EmptyContinuation extends AbstractContinuation {

	/**
	 * Constructor of the EmptyContinuation
	 */
	public EmptyContinuation() {
	}

	/**
	 * see {@link query.interfaces.IEval#eval(ExecutionStateI) }
	 */
	@Override
	public Object eval(ExecutionStateI context) {

		return null;
	}

}
