package query.ast;

import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.abstraction.AbstractBase;
import query.interfaces.IParamContext;

public class AbsoluteBase extends AbstractBase {
	private final PositionI position;

	public AbsoluteBase(PositionI position) {
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
