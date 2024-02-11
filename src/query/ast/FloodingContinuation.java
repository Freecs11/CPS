package query.ast;

import abstractQuery.AbstractBase;
import abstractQuery.AbstractContinuation;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import implementation.requestsIMPL.ExecutionStateIMPL;
import implementation.requestsIMPL.ProcessingNodeIMPL;
import query.interfaces.IParamContext;

public class FloodingContinuation extends AbstractContinuation {
	private final AbstractBase base;
	private final double distance;

	public FloodingContinuation(AbstractBase base, double distance) {
		super();
		this.base = base;
		this.distance = distance;
	}

	public AbstractBase getBase() {
		return base;
	}

	public double getDistance() {
		return distance;
	}

	@Override
	public Object eval(ExecutionStateI context) {
		// context.getNeighbours()
		// context.propagateRequest(null, null)
		PositionI pos = (PositionI) this.base.eval(context);
		ProcessingNodeIMPL procNode = (ProcessingNodeIMPL) context.getProcessingNode();
		procNode.setPostion(pos);
		ExecutionStateIMPL contextCC = (ExecutionStateIMPL) context;
		contextCC.setIsFlooding(true);
		contextCC.setMaxDistance(this.distance);
		contextCC.updateProcessingNode(procNode);
		return null;
	}

	@Override
	public Object eval(IParamContext context) {
		// TODO Auto-generated method stub
		return null;
	}

}
