package query.ast;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.abstraction.AbstractConditionalExpr;
import query.abstraction.AbstractRand;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>EqualConditionalExpr</code> represents an equal conditional
 * expression in the AST. It is used to compare two values.
 * </p>
 * information.
 */
public class EqualConditionalExpr extends AbstractConditionalExpr {
	private AbstractRand rand1;
	private AbstractRand rand2;

	/**
	 * Constructor of the EqualConditionalExpr
	 * 
	 * @param rand1 the first value to compare
	 * @param rand2 the second value to compare
	 */
	public EqualConditionalExpr(AbstractRand rand1, AbstractRand rand2) {

		this.rand1 = rand1;
		this.rand2 = rand2;
	}

	/**
	 * Getter of the first value to compare
	 * 
	 * @return the first value to compare
	 */
	public AbstractRand getRand1() {
		return rand1;
	}

	/**
	 * Setter of the first value to compare
	 * 
	 * 
	 */
	public void setRand1(AbstractRand rand1) {
		this.rand1 = rand1;
	}

	/**
	 * getter of the second value to compare
	 * 
	 * @return the second value to compare
	 */
	public AbstractRand getRand2() {
		return rand2;
	}

	/**
	 * Setter of the second value to compare
	 * 
	 * @param rand2 the second value to compare
	 */
	public void setRand2(AbstractRand rand2) {
		this.rand2 = rand2;
	}

	/**
	 * see {@link query.interfaces.IEval#eval(ExecutionStateI) }
	 */
	@Override
	public Boolean eval(ExecutionStateI context) {
		return this.rand1.eval(context).equals(this.rand2.eval(context));
	}

}
