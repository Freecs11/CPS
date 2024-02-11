package query.ast;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

import abstractQuery.AbstractContinuation;
import abstractQuery.AbstractGather;
import abstractQuery.AbstractQuery;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import implementation.QueryResultIMPL;
import implementation.SensorDataIMPL;
import query.interfaces.IParamContext;

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

	@SuppressWarnings("unchecked")
	@Override
	public QueryResultI eval(ExecutionStateI context) {
		Map<String, Object> res = (Map<String, Object>) gather.eval(context);
		// Object cont = continuation.eval(context); // Not used yet
		QueryResultIMPL currRes = (QueryResultIMPL) context.getCurrentResult();
		currRes.setGR(true);
		for (Map.Entry<String, Object> entry : res.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			Class<? extends Serializable> type = null;
			if (value instanceof Double) {
				type = Double.class;
			} else if (value instanceof Boolean) {
				type = Boolean.class;
			}
			SensorDataIMPL sensorData = new SensorDataIMPL(context.getProcessingNode().getNodeIdentifier(),
					key, (Serializable) value, Instant.now(), type);
			currRes.addToGatheredSensors(sensorData);
		}
		return currRes;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Double> eval(IParamContext context) {

		Map<String, Double> res = (Map<String, Double>) gather.eval(context);
		Object cont = continuation.eval(context);
		if (cont != null) {
			// toDo
			return res;
		} else {
			System.out.println(res);
			return res;
		}
	}
}
