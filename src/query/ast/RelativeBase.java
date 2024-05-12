package query.ast;

import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.abstraction.AbstractBase;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>RelativeBase</code> represents a relative base in the AST.
 * </p>
 * 
 */
public class RelativeBase extends AbstractBase {
	public RelativeBase() {
	}

	/**
	 * see {@link query.interfaces.IEval#eval(ExecutionStateI) }
	 */
	@Override
	public PositionI eval(ExecutionStateI context) {
		return context.getProcessingNode().getPosition();
	}

}
