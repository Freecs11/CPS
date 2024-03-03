package query.ast;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import implementation.request.ExecutionStateIMPL;
import query.abstraction.AbstractContinuation;
import query.interfaces.IParamContext;

public class EmptyContinuation extends AbstractContinuation {

	public EmptyContinuation() {
	}

	@Override
	public Object eval(ExecutionStateI context) {
		((ExecutionStateIMPL) context).setDirectional(false);
		((ExecutionStateIMPL) context).setIsFlooding(false);
		return null;
	}

	@Override
	public Object eval(IParamContext context) {
		return null;
	}
}
