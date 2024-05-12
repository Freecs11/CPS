package query.ast;

import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.abstraction.AbstractBase;

/**
 * <p> Description </p>
 * <p> The class <code>AbsoluteBase</code> acts as the as an extension of the <code>AbstractBase</code> class. </p>
 * <p> It is used to define an absolute base. </p>
 */
public class AbsoluteBase extends AbstractBase {
	private final PositionI position;

	/**
	 * Constructor of the AbsoluteBase
	 * 
	 * @param position the position of the base
	 */
	public AbsoluteBase(PositionI position) {
		this.position = position;
	}

	/**
	 * see {@link query.interfaces.IEval#eval(ExecutionStateI)}
	 */
	@Override
	public PositionI eval(ExecutionStateI context) {
		return this.position;
	}

}
