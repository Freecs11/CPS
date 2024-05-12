package query.ast;

import java.util.ArrayList;

import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import implementation.QueryResultIMPL;
import query.abstraction.AbstractContinuation;
import query.abstraction.AbstractGather;
import query.abstraction.AbstractQuery;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>GatherQuery</code> represents a gather query in the AST.
 * </p>
 * information.
 */
public class GatherQuery extends AbstractQuery {
	private final AbstractGather gather;
	private final AbstractContinuation continuation;

	/**
	 * Constructor of the GatherQuery
	 * 
	 * @param gather
	 * @param continuation
	 */
	public GatherQuery(AbstractGather gather, AbstractContinuation continuation) {
		super();
		this.gather = gather;
		this.continuation = continuation;
	}

	/**
	 * Getter of the gather
	 * 
	 * @return the gather
	 */
	public AbstractGather getGather() {
		return gather;
	}

	/**
	 * Getter of the continuation
	 * 
	 * @return the continuation
	 */
	public AbstractContinuation getContinuation() {
		return continuation;
	}

	/**
	 * @see query.abstraction.AbstractQuery#eval(ExecutionStateI)
	 * @param context the execution state
	 */
	@Override
	public QueryResultI eval(ExecutionStateI context) {
		ArrayList<SensorDataI> GatheredSensorDataList = gather.eval(context);
		continuation.eval(context);
		QueryResultIMPL newRes = new QueryResultIMPL(false, true, new ArrayList<>(), GatheredSensorDataList);
		((QueryResultIMPL) context.getCurrentResult()).setGR(true);
		context.addToCurrentResult(newRes);
		return context.getCurrentResult();
	}

}
