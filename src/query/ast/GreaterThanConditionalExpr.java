package query.ast;

import abstractQuery.AbstractConditionalExpr;
import abstractQuery.AbstractRand;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import implementation.requestsIMPL.ExecutionStateIMPL;
import query.interfaces.IParamContext;

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
		ExecutionStateIMPL contextIMP = (ExecutionStateIMPL) context;
		Double rand1Eval = (Double) this.rand1.eval(contextIMP);
		Double rand2Eval = (Double) this.rand2.eval(contextIMP);
		boolean resB = Double.compare(rand1Eval, rand2Eval) > 0;

		String nodeInfo = contextIMP.getProcessingNode().getNodeIdentifier();
		if (Boolean.TRUE.equals(resB)) {
			contextIMP.addPositiveSN(nodeInfo);
		}
		return resB;
	}

	@Override
	public Boolean eval(IParamContext context) {
		Double rand1Eval = (Double) this.rand1.eval(context);
		Double rand2Eval = (Double) this.rand2.eval(context);
		return Double.compare(rand1Eval, rand2Eval) > 0;
	}
}
