package cps.ast;

import abstractClass.ABSBExp;
import abstractClass.ABSCExp;
import cps.interfaces.IParamContext;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

public class CExpBExp extends ABSBExp {
	private final ABSCExp cexpr;

	public CExpBExp(ABSCExp expr) {
		this.cexpr = expr;
	}

	public ABSCExp getExpr1() {
		return cexpr;
	}

	@Override
	public Object eval(ExecutionStateI context) {
		return cexpr.eval(context);
	}

	@Override
	public Object eval(IParamContext context) {
		return cexpr.eval(context);
	}
}
