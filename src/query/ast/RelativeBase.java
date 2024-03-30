package query.ast;

import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.abstraction.AbstractBase;

public class RelativeBase extends AbstractBase {
	public RelativeBase() {
	}

	@Override
	public PositionI eval(ExecutionStateI context) {
		return context.getProcessingNode().getPosition();
	}

}
