package query.ast;

import abstractQuery.AbstractContinuation;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.interfaces.IParamContext;

public class EmptyContinuation extends AbstractContinuation {

	public EmptyContinuation() {
	}

	@Override
	public Object eval(ExecutionStateI context) {
		return null;
	}

	@Override
	public Object eval(IParamContext context) {
		return null;
	}
}
