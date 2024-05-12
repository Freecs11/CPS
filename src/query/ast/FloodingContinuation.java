package query.ast;

import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import implementation.request.ExecutionStateIMPL;
import implementation.request.ProcessingNodeIMPL;
import query.abstraction.AbstractBase;
import query.abstraction.AbstractContinuation;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>FloodingContinuation</code> represents a flooding
 * continuation in the AST.
 * </p>
 * information.
 */
public class FloodingContinuation extends AbstractContinuation {
	private final AbstractBase base;
	private final double distance;

	/**
	 * Constructor of the FloodingContinuation
	 * 
	 * @param base     the base of the flooding continuation
	 * @param distance the distance of the flooding continuation
	 */
	public FloodingContinuation(AbstractBase base, double distance) {
		super();
		this.base = base;
		this.distance = distance;
	}

	/**
	 * Getter of the base of the flooding continuation
	 * 
	 * @return the base of the flooding continuation
	 */
	public AbstractBase getBase() {
		return base;
	}

	/**
	 * Getter of the distance of the flooding continuation
	 * 
	 * @return the distance of the flooding continuation
	 */
	public double getDistance() {
		return distance;
	}

	/**
	 * see {@link query.interfaces.IEval#eval(ExecutionStateI) }
	 */
	@Override
	public Object eval(ExecutionStateI context) {
		ExecutionStateIMPL contextCC = (ExecutionStateIMPL) context;
		PositionI pos = this.base.eval(contextCC);
		((ProcessingNodeIMPL) contextCC.getProcessingNode()).setPostion(pos);
		if (distance > 0) {
			contextCC.setMaxDistance(distance);
		}
		contextCC.setIsFlooding(true);
		return null;
	}
}
