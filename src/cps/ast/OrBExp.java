package cps.ast;

import abstractClass.ABSBExp;
import cps.interfaces.IParamContext;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

public class OrBExp extends ABSBExp {
	private final ABSBExp expr1;
	private final ABSBExp expr2;

	public OrBExp(ABSBExp expr1, ABSBExp expr2) {
		this.expr1 = expr1;
		this.expr2 = expr2;
	}

	public ABSBExp getExpr1() {
		return expr1;
	}

	public ABSBExp getExpr2() {
		return expr2;
	}

	@Override
	public Boolean eval(ExecutionStateI context) {
		return (boolean) expr1.eval(context) || (boolean) expr2.eval(context);
	}

	@Override
	public Object eval(IParamContext context) {
		return (boolean) expr1.eval(context) || (boolean) expr2.eval(context);
	}

}
