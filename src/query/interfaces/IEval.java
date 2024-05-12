package query.interfaces;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

/**
 * <p>
 * Description
 * </p>
 * <p>
 * The interface <code>IEval</code> is used to define the eval method which
 * evaluates expressions in the AST.
 * </p>
 */
public interface IEval {

	/**
	 * The eval method is used to evaluate expressions in the AST.
	 * 
	 * @param context the context of the request to evaluate
	 */
	public Object eval(ExecutionStateI context);
}
