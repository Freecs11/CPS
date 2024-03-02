package query.ast;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.abstraction.AbstractRand;
import query.interfaces.IParamContext;

public class ConstantRand extends AbstractRand {
	private double randConst;

	public ConstantRand(double randConst) {
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
