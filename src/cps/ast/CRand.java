package cps.ast;

import abstractClass.ABSRand;
import cps.interfaces.IParamContext;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

public class CRand extends ABSRand {
	private double randConst;

	public CRand(double randConst) {
		super();
		this.randConst = randConst;
	}

	public double getRandConst() {
		return randConst;
	}

	public void setRandConst(double randConst) {
		this.randConst = randConst;
	}

	@Override
	public Double eval(ExecutionStateI context) {
		return this.randConst;
	}

	@Override
	public Object eval(IParamContext context) {
		return this.randConst;
	}

}
