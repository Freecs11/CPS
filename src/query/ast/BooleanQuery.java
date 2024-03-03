package query.ast;

import java.util.HashSet;

import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import implementation.QueryResultIMPL;
import implementation.requestsIMPL.ExecutionStateIMPL;
import query.abstraction.AbstractBooleanExpr;
import query.abstraction.AbstractContinuation;
import query.abstraction.AbstractQuery;
import query.interfaces.IParamContext;

public class BooleanQuery extends AbstractQuery {
	private final AbstractBooleanExpr expr;
	private final AbstractContinuation continuation;

	public BooleanQuery(AbstractBooleanExpr expr, AbstractContinuation continuation) {
		super();
		this.expr = expr;
		this.continuation = continuation;
	}

	public AbstractBooleanExpr getExpression() {
		return expr;
	}

	public AbstractContinuation getContinuation() {
		return continuation;
	}

	@Override
	public QueryResultI eval(ExecutionStateI context) {
		ExecutionStateIMPL contextIMP = (ExecutionStateIMPL) context;
		expr.eval(contextIMP); // we need to evaluate the expression to get the positive sensors
		continuation.eval(context);
		QueryResultIMPL currRes = (QueryResultIMPL) contextIMP.getCurrentResult();
		HashSet<String> sensors = (HashSet<String>) contextIMP.getPositiveSNG();
		currRes.setBR(true);
		for (String sensor : sensors) {
			currRes.addPositiveSN(sensor);
		}

		return currRes;
	}

	@Override
	public Object eval(IParamContext context) {
		Object res = expr.eval(context);
		Object cont = continuation.eval(context);
		System.out.println(res);
		System.out.println(cont);
		if (cont != null) {
			// toDo
			return res;
		} else {
			Boolean b = (Boolean) res;
			System.out.println(b);
			return res;
		}
	}

}
