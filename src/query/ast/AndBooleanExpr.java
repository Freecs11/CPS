package query.ast;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.abstraction.AbstractBooleanExpr;

public class AndBooleanExpr extends AbstractBooleanExpr {
	private final AbstractBooleanExpr expr1;
	private final AbstractBooleanExpr expr2;

	public AndBooleanExpr(AbstractBooleanExpr expr1, AbstractBooleanExpr expr2) {
		super();
		this.expr1 = expr1;
		this.expr2 = expr2;
	}

	public AbstractBooleanExpr getExpr1() {
		return expr1;
	}

	public AbstractBooleanExpr getExpr2() {
		return expr2;
	}

	@Override
	public Boolean eval(ExecutionStateI context) {
		return expr1.eval(context) && expr2.eval(context);
	}

}
