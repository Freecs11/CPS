package query.ast;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.abstraction.AbstractConditionalExpr;
import query.abstraction.AbstractRand;

public class EqualConditionalExpr extends AbstractConditionalExpr {
	private AbstractRand rand1;
	private AbstractRand rand2;

	public EqualConditionalExpr(AbstractRand rand1, AbstractRand rand2) {

		this.rand1 = rand1;
		this.rand2 = rand2;
	}

	public AbstractRand getRand1() {
		return rand1;
	}

	public void setRand1(AbstractRand rand1) {
		this.rand1 = rand1;
	}

	public AbstractRand getRand2() {
		return rand2;
	}

	public void setRand2(AbstractRand rand2) {
		this.rand2 = rand2;
	}

	@Override
	public Boolean eval(ExecutionStateI context) {
		return this.rand1.eval(context).equals(this.rand2.eval(context));
	}

}
