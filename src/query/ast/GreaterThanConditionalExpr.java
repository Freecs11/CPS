package query.ast;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import implementation.request.ExecutionStateIMPL;
import query.abstraction.AbstractConditionalExpr;
import query.abstraction.AbstractRand;

public class GreaterThanConditionalExpr extends AbstractConditionalExpr {

	private final AbstractRand rand1;
	private final AbstractRand rand2;

	public GreaterThanConditionalExpr(AbstractRand rand1, AbstractRand rand2) {
		this.rand1 = rand1;
		this.rand2 = rand2;
	}

	public AbstractRand getRand1() {
		return rand1;
	}

	public AbstractRand getRand2() {
		return rand2;
	}

	@Override
	public Boolean eval(ExecutionStateI context) {
		return ((Double) this.rand1.eval(context)) > ((Double) this.rand2.eval(context));
	}

}
