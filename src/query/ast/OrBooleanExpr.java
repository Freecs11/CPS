package query.ast;

import abstractQuery.AbstractBooleanExpr;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.interfaces.IParamContext;

public class OrBooleanExpr extends AbstractBooleanExpr {
	private final AbstractBooleanExpr expr1;
	private final AbstractBooleanExpr expr2;

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

	@Override
	public Boolean eval(ExecutionStateI context) {
		return (boolean) expr1.eval(context) || (boolean) expr2.eval(context);
	}

	@Override
	public Object eval(IParamContext context) {
		return (boolean) expr1.eval(context) || (boolean) expr2.eval(context);
	}

}
