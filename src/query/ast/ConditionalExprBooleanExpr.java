package query.ast;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.abstraction.AbstractBooleanExpr;
import query.abstraction.AbstractConditionalExpr;
import query.interfaces.IParamContext;

public class ConditionalExprBooleanExpr extends AbstractBooleanExpr {
	private final AbstractConditionalExpr cexpr;

	public ConditionalExprBooleanExpr(AbstractConditionalExpr expr) {
		this.cexpr = expr;
	}

	public AbstractConditionalExpr getExpr1() {
		return cexpr;
	}

	@Override
	public Object eval(ExecutionStateI context) {
		return cexpr.eval(context);
	}

	@Override
	public Object eval(IParamContext context) {
		return cexpr.eval(context);
	}
}
