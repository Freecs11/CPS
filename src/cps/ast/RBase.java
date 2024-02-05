package cps.ast;

import abstractClass.ABSBase;
import cps.interfaces.IParamContext;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

public class RBase extends ABSBase {
	public RBase() {
		// TODO Auto-•generated constructor stub
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
