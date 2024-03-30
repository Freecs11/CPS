package query.ast;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;

import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import implementation.QueryResultIMPL;
import implementation.SensorDataIMPL;
import query.abstraction.AbstractContinuation;
import query.abstraction.AbstractGather;
import query.abstraction.AbstractQuery;

public class GatherQuery extends AbstractQuery {
	private final AbstractGather gather;
	private final AbstractContinuation continuation;

	public GatherQuery(AbstractGather gather, AbstractContinuation continuation) {
		super();
		this.gather = gather;
		this.continuation = continuation;
	}

	public AbstractGather getGather() {
		return gather;
	}

	public AbstractContinuation getContinuation() {
		return continuation;
	}

	@Override
	public QueryResultI eval(ExecutionStateI context) {
		ArrayList<SensorDataI> GatheredSensorDataList = gather.eval(context);
		continuation.eval(context);
		QueryResultIMPL newRes = new QueryResultIMPL(false, true, null, GatheredSensorDataList);
		((QueryResultIMPL) context.getCurrentResult()).setGR(true);
		context.addToCurrentResult(newRes);
		return context.getCurrentResult();
	}

}
