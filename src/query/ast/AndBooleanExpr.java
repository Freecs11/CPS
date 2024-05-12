package query.ast;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.abstraction.AbstractBooleanExpr;

/**
 * <p> Description </p>
 * <p> The class <code>AndBooleanExpr</code> is used to define the 
 * End operator of the boolean expression. </p>
 */
public class AndBooleanExpr extends AbstractBooleanExpr {
	private final AbstractBooleanExpr expr1;
	private final AbstractBooleanExpr expr2;

	/**
	 * Constructor of the AndBooleanExpr
	 * 
	 * @param expr1 the first expression to be checked
	 * @param expr2 the second expression to be checked
	 */
	public AndBooleanExpr(AbstractBooleanExpr expr1, AbstractBooleanExpr expr2) {
		super();
		this.expr1 = expr1;
		this.expr2 = expr2;
	}

	/**
	 * Getter of the first expression of the expression
	 * 
	 * @return	the first expression of the expression
	 */
	public AbstractBooleanExpr getExpr1() {
		return expr1;
	}

	/**
	 * Getter of the second expression of the expression
	 * 
	 * @return	the second expression of the expression
	 */
	public AbstractBooleanExpr getExpr2() {
		return expr2;
	}

	/**
	 * see {@link query.interfaces.IEval#eval(ExecutionStateI)}
	 */
	@Override
	public Boolean eval(ExecutionStateI context) {
		return expr1.eval(context) && expr2.eval(context);
	}

}
