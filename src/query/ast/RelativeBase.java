package query.ast;

import abstractQuery.AbstractBase;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.interfaces.IParamContext;

public class RelativeBase extends AbstractBase {
	public RelativeBase() {
	}

	@Override
	public PositionI eval(ExecutionStateI context) {
		return context.getProcessingNode().getPosition();
	}

	@Override
	public Object eval(IParamContext context) {
		return context.getPosition();
	}
}
