package query.ast;

import java.util.ArrayList;
import java.util.HashSet;

import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;
import implementation.QueryResultIMPL;
import implementation.request.ExecutionStateIMPL;
import query.abstraction.AbstractBooleanExpr;
import query.abstraction.AbstractContinuation;
import query.abstraction.AbstractQuery;

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
		ProcessingNodeI node = contextIMP.getProcessingNode();
		ArrayList<String> nodesEvaluated = new ArrayList<String>();
		boolean b = expr.eval(contextIMP); // we need to evaluate the expression to get the positive sensors
		if (b) {
			nodesEvaluated.add(node.getNodeIdentifier());
		}
		QueryResultI res = new QueryResultIMPL(true, false, nodesEvaluated, null);
		continuation.eval(contextIMP);
		contextIMP.addToCurrentResult(res);
		return contextIMP.getCurrentResult();
	}

}
