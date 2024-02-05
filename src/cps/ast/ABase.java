package cps.ast;

import abstractClass.ABSBase;
import cps.interfaces.IParamContext;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

public class ABase extends ABSBase {
	private final PositionI position;

	public ABase(PositionI position) {
		this.position = position;
	}

	@Override
	public PositionI eval(ExecutionStateI context) {
		return this.position;
	}

	@Override
	public Object eval(IParamContext context) {
		return position;
	}

}
