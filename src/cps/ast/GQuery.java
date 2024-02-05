package cps.ast;

import java.io.Serializable;
import java.sql.Time;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import abstractClass.ABSCont;
import abstractClass.ABSGather;
import abstractClass.ABSQuery;
import cps.interfaces.IParamContext;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import itfIMP.SensorDataIMP;
import itfIMP.queryResultIMP;

public class GQuery extends ABSQuery {
	private final ABSGather gather;
	private final ABSCont continuation;

	public GQuery(ABSGather gather, ABSCont continuation) {
		super();
		this.gather = gather;
		this.continuation = continuation;
	}

	public ABSGather getGather() {
		return gather;
	}

	public ABSCont getContinuation() {
		return continuation;
	}

	@SuppressWarnings("unchecked")
	@Override
	public QueryResultI eval(ExecutionStateI context) {
		Map<String, Object> res = (Map<String, Object>) gather.eval(context);
		Object cont = continuation.eval(context);
		queryResultIMP currRes = (queryResultIMP) context.getCurrentResult();
		currRes.setGR(true);
		for (Map.Entry<String, Object> entry : res.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			SensorDataIMP sensorData = new SensorDataIMP(context.getProcessingNode().getNodeIdentifier(),
					key, (Serializable) value, Instant.now());
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
