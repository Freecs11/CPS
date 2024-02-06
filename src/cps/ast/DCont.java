package cps.ast;

import java.util.Set;

import abstractClass.ABSCont;
import abstractClass.ABSDirs;
import cps.interfaces.IParamContext;
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import itfIMP.requestsItfIMP.ExecutionState;

public class DCont extends ABSCont {
	private int jumps;
	private ABSDirs direction;

	public DCont(int jumps, ABSDirs direction) {
		super();
		this.jumps = jumps;
		this.direction = direction;
	}

	public int getJumps() {
		return jumps;
	}

	public void setJumps(int jumps) {
		this.jumps = jumps;
	}

	public ABSDirs getDirection() {
		return direction;
	}

	public void setDirection(ABSDirs direction) {
		this.direction = direction;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object eval(ExecutionStateI context) {
		Set<Direction> directions = (Set<Direction>) direction.eval(context);
		ExecutionState contextCC = (ExecutionState) context; 
		contextCC.setMaxHops(jumps);
		contextCC.setDirectional(true);
		contextCC.setDirections(directions);
		return null;
	}

	@Override
	public Object eval(IParamContext context) {
		// TODO Auto-generated method stub
		return null;
	}
}
