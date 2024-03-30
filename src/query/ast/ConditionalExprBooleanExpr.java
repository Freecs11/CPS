package query.ast;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.abstraction.AbstractBooleanExpr;
import query.abstraction.AbstractConditionalExpr;

public class ConditionalExprBooleanExpr extends AbstractBooleanExpr {
	private final AbstractConditionalExpr cexpr;

	public ConditionalExprBooleanExpr(AbstractConditionalExpr expr) {
		this.cexpr = expr;
	}

	public AbstractConditionalExpr getExpr1() {
		return cexpr;
	}

	@Override
	public Boolean eval(ExecutionStateI context) {
		return cexpr.eval(context);
	}

}
