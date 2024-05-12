package query.ast;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.abstraction.AbstractRand;

/**
 * <p>
 * Description
 * </p>
 * <p>
 * The class <code>ConstantRand</code> acts as the as an extension of the
 * <code>AbstractRand</code> class.
 * </p>
 * <p>
 * It is used to define a constant random value.
 * </p>
 */
public class ConstantRand extends AbstractRand {
	private double randConst;

	/**
	 * Constructor of the ConstantRand
	 * 
	 * @param randConst the constant random value
	 */
	public ConstantRand(double randConst) {
		super();
		this.randConst = randConst;
	}

	/**
	 * Getter of the constant random value
	 * 
	 * @return the constant random value
	 */
	public double getRandConst() {
		return randConst;
	}

	/**
	 * Setter of the constant random value of the expression
	 * 
	 * @param randConst the constant random value to set
	 * 
	 */
	public void setRandConst(double randConst) {
		this.randConst = randConst;
	}

	/**
	 * see {@link query.interfaces.IEval#eval(ExecutionStateI) }
	 */
	@Override
	public Double eval(ExecutionStateI context) {
		return this.randConst;
	}

}
