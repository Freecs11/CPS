package cps.interfaces;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

public interface IEval {
	public Object eval(ExecutionStateI context);

	public Object eval(IParamContext context);
}
