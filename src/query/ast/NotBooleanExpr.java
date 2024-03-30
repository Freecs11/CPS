package query.ast;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.abstraction.AbstractBooleanExpr;

public class NotBooleanExpr extends AbstractBooleanExpr {
	private final AbstractBooleanExpr expr;

	public NotBooleanExpr(AbstractBooleanExpr expr) {
		super();
		this.expr = expr;
	}

	public AbstractBooleanExpr getExpr1() {
		return expr;
	}

	@Override
	public Boolean eval(ExecutionStateI context) {
		return !(boolean) expr.eval(context);
	}

}
