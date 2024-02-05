package cps.ast;

import abstractClass.ABSCExp;
import abstractClass.ABSRand;
import cps.interfaces.IParamContext;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import itfIMP.ExecutionState;

public class GeqCExp extends ABSCExp {

	private final ABSRand rand1;
	private final ABSRand rand2;

	public GeqCExp(ABSRand rand1, ABSRand rand2) {
		super();
		this.rand1 = rand1;
		this.rand2 = rand2;
	}

	public ABSRand getRand1() {
		return rand1;
	}

	public ABSRand getRand2() {
		return rand2;
	}

	@Override
	public Boolean eval(ExecutionStateI context) {
		ExecutionState contextIMP = (ExecutionState) context;
		Double rand1Eval = (Double) this.rand1.eval(contextIMP);
		Double rand2Eval = (Double) this.rand2.eval(contextIMP);
		Boolean resB = Double.compare(rand1Eval, rand2Eval) >= 0;

		if (this.rand1 instanceof SRand && resB) {
			SRand rand1IMP = (SRand) this.rand1;
			contextIMP.addPositiveSN(rand1IMP.getSensorId());
		}

		if (this.rand2 instanceof SRand && resB) {
			SRand rand2IMP = (SRand) this.rand2;
			contextIMP.addPositiveSN(rand2IMP.getSensorId());
		}
		return resB;
	}

	@Override
	public Boolean eval(IParamContext context) {
		Double rand1Eval = (Double) this.rand1.eval(context);
		Double rand2Eval = (Double) this.rand2.eval(context);
		return Double.compare(rand1Eval, rand2Eval) >= 0;
	}
}
