package cps.ast;

import abstractClass.ABSBExp;
import cps.interfaces.IParamContext;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

public class AndBExp extends ABSBExp {
	private final ABSBExp expr1;
	private final ABSBExp expr2;

	public AndBExp(ABSBExp expr1, ABSBExp expr2) {
		super();
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
		return (Boolean) expr1.eval(context) && (Boolean) expr2.eval(context);
	}

	@Override
	public Object eval(IParamContext context) {
		return (Boolean) expr1.eval(context) && (Boolean) expr2.eval(context);
	}
}
