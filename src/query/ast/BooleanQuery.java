package query.ast;

import java.util.ArrayList;

import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;
import implementation.QueryResultIMPL;
import implementation.request.ExecutionStateIMPL;
import query.abstraction.AbstractBooleanExpr;
import query.abstraction.AbstractContinuation;
import query.abstraction.AbstractQuery;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>BooleanQuery</code> represents a boolean query in the AST.
 * </p>
 * information.
 */
public class BooleanQuery extends AbstractQuery {
	private final AbstractBooleanExpr expr;
	private final AbstractContinuation continuation;

	/**
	 * Constructor of the BooleanQuery
	 * 
	 * @param expr         the expression of the query
	 * @param continuation the continuation of the query
	 */
	public BooleanQuery(AbstractBooleanExpr expr, AbstractContinuation continuation) {
		super();
		this.expr = expr;
		this.continuation = continuation;
	}

	/**
	 * Getter of the expression of the query
	 * 
	 * @return the expression of the query
	 */
	public AbstractBooleanExpr getExpression() {
		return expr;
	}

	/**
	 * Getter of the continuation of the query
	 * 
	 * @return the continuation of the query
	 */
	public AbstractContinuation getContinuation() {
		return continuation;
	}

	/**
	 * see {@link query.interfaces.IEval#eval(ExecutionStateI)}
	 */
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
		((QueryResultIMPL) contextIMP.getCurrentResult()).setBR(true);
		contextIMP.addToCurrentResult(res);
		return contextIMP.getCurrentResult();
	}

}
