package cps.ast;

import java.util.HashSet;

import abstractClass.ABSBExp;
import abstractClass.ABSCont;
import abstractClass.ABSQuery;
import cps.interfaces.IParamContext;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import itfIMP.QueryResultIMP;
import itfIMP.requestsItfIMP.ExecutionState;

public class BQuery extends ABSQuery {
	private final ABSBExp expr;
	private final ABSCont continuation;

	public BQuery(ABSBExp expr, ABSCont continuation) {
		super();
		this.expr = expr;
		this.continuation = continuation;
	}

	public ABSBExp getExpression() {
		return expr;
	}

	public ABSCont getContinuation() {
		return continuation;
	}

	@Override
	public QueryResultI eval(ExecutionStateI context) {
		ExecutionState contextIMP = (ExecutionState) context;
		Boolean res = (Boolean) expr.eval(contextIMP);
		Object cont = continuation.eval(contextIMP);
		QueryResultIMP currRes = (QueryResultIMP) contextIMP.getCurrentResult();
		HashSet<String> sensors = contextIMP.getPositiveSNG();

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
