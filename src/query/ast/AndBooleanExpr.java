package query.ast;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.abstraction.AbstractBooleanExpr;
import query.interfaces.IParamContext;

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
		return (Boolean) expr1.eval(context) && (Boolean) expr2.eval(context);
	}

	@Override
	public Object eval(IParamContext context) {
		return (Boolean) expr1.eval(context) && (Boolean) expr2.eval(context);
	}
}
