package query.ast;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.abstraction.AbstractBooleanExpr;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>OrBooleanExpr</code> represents a or boolean expression in
 * the AST.
 * </p>
 */
public class OrBooleanExpr extends AbstractBooleanExpr {
	private final AbstractBooleanExpr expr1;
	private final AbstractBooleanExpr expr2;

	/**
	 * Constructor of the OrBooleanExpr
	 * 
	 * @param expr1
	 * @param expr2
	 */
	public OrBooleanExpr(AbstractBooleanExpr expr1, AbstractBooleanExpr expr2) {
		this.expr1 = expr1;
		this.expr2 = expr2;
	}

	public AbstractBooleanExpr getExpr1() {
		return expr1;
	}

	public AbstractBooleanExpr getExpr2() {
		return expr2;
	}

	/**
	 * see {@link query.interfaces.IEval#eval(ExecutionStateI) }
	 */
	@Override
	public Boolean eval(ExecutionStateI context) {
		return expr1.eval(context) || expr2.eval(context);
	}

}
