package cps.ast;

import abstractClass.ABSBase;
import abstractClass.ABSCont;
import cps.interfaces.IParamContext;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import itfIMP.ExecutionState;
import itfIMP.ProcessingNodeImp;

public class FCont extends ABSCont {
	private final ABSBase base;
	private final double distance;

	public FCont(ABSBase base, double distance) {
		super();
		this.base = base;
		this.distance = distance;
	}

	public ABSBase getBase() {
		return base;
	}

	public double getDistance() {
		return distance;
	}

	@Override
	public Object eval(ExecutionStateI context) {
		// TODO Auto-generated method stub
		// context.getNeighbours()
		// context.propagateRequest(null, null)
		PositionI pos = (PositionI) this.base.eval(context);
		ProcessingNodeImp procNode = (ProcessingNodeImp) context.getProcessingNode();
		procNode.setPostion(pos);
		ExecutionState contextCC = (ExecutionState) context; 
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
