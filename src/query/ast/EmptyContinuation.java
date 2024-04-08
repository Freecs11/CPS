package query.ast;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.abstraction.AbstractContinuation;

public class EmptyContinuation extends AbstractContinuation {

	public EmptyContinuation() {
	}

	@Override
	public Object eval(ExecutionStateI context) {

		return null;
	}

}
