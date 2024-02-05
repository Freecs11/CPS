package cps.ast;

import abstractClass.ABSBExp;
import cps.interfaces.IParamContext;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

public class NotBExp extends ABSBExp {
	private final ABSBExp expr;

	public NotBExp(ABSBExp expr) {
		super();
		this.expr = expr;
	}

	public ABSBExp getExpr1() {
		return expr;
	}

	@Override
	public Boolean eval(ExecutionStateI context) {
		return !(boolean) expr.eval(context);
	}

	@Override
	public Object eval(IParamContext context) {
		return !(boolean) expr.eval(context);
	}

}
