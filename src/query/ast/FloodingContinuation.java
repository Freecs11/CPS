package query.ast;

import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import implementation.request.ExecutionStateIMPL;
import implementation.request.ProcessingNodeIMPL;
import query.abstraction.AbstractBase;
import query.abstraction.AbstractContinuation;

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
		ExecutionStateIMPL contextCC = (ExecutionStateIMPL) context;
		PositionI pos = this.base.eval(contextCC);
		ProcessingNodeIMPL procNode = (ProcessingNodeIMPL) contextCC.getProcessingNode();
		procNode.setPostion(pos);

		if (distance > 0) {
			contextCC.setMaxDistance(distance);
		}
		contextCC.setIsFlooding(true);
		contextCC.updateProcessingNode(procNode);
		return null;
	}

}
