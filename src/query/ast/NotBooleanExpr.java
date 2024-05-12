package query.ast;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.abstraction.AbstractBooleanExpr;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>NotBooleanExpr</code> represents a not boolean expression in
 * the AST.
 * </p>
 */
public class NotBooleanExpr extends AbstractBooleanExpr {
	private final AbstractBooleanExpr expr;

	/**
	 * Constructor of the NotBooleanExpr
	 * 
	 * @param expr
	 */
	public NotBooleanExpr(AbstractBooleanExpr expr) {
		super();
		this.expr = expr;
	}

	public AbstractBooleanExpr getExpr1() {
		return expr;
	}

	/**
	 * see {@link query.interfaces.IEval#eval(ExecutionStateI) }
	 */
	@Override
	public Boolean eval(ExecutionStateI context) {
		return !expr.eval(context);
	}

}
