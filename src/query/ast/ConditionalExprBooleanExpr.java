package query.ast;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.abstraction.AbstractBooleanExpr;
import query.abstraction.AbstractConditionalExpr;

/**
 * <p>
 * Description
 * </p>
 * <p>
 * The class <code>ConditionalExprBooleanExpr</code> acts as the as an extension
 * of the <code>AbstractBooleanExpr</code> class.
 * </p>
 * <p>
 * It is used to define a conditional expression of a boolean expression.
 * </p>
 * 
 */
public class ConditionalExprBooleanExpr extends AbstractBooleanExpr {
	private final AbstractConditionalExpr cexpr;

	/**
	 * Constructor of the the class ConditionalExprBooleanExpr
	 * 
	 * @param expr the conditional expression
	 */
	public ConditionalExprBooleanExpr(AbstractConditionalExpr expr) {
		this.cexpr = expr;
	}

	/**
	 * Getter of the expression of the expression
	 * 
	 * @return the expression of the expression
	 */
	public AbstractConditionalExpr getExpr1() {
		return cexpr;
	}

	/**
	 * see {@link query.interfaces.IEval#eval(ExecutionStateI) }
	 */
	@Override
	public Boolean eval(ExecutionStateI context) {
		return cexpr.eval(context);
	}

}
