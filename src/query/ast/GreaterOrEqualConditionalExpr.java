package query.ast;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.abstraction.AbstractConditionalExpr;
import query.abstraction.AbstractRand;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>GreaterOrEqualConditionalExpr</code> represents a greater or
 * equal conditional expression in the AST.
 * </p>
 * 
 */
public class GreaterOrEqualConditionalExpr extends AbstractConditionalExpr {

	private final AbstractRand rand1;
	private final AbstractRand rand2;

	/**
	 * Constructor of the GreaterOrEqualConditionalExpr
	 * 
	 * @param rand1 the first random variable
	 * @param rand2 the second random variable
	 */
	public GreaterOrEqualConditionalExpr(AbstractRand rand1, AbstractRand rand2) {
		super();
		this.rand1 = rand1;
		this.rand2 = rand2;
	}

	public AbstractRand getRand1() {
		return rand1;
	}

	public AbstractRand getRand2() {
		return rand2;
	}

	/**
	 * see {@link query.interfaces.IEval#eval(ExecutionStateI) }
	 */
	@Override
	public Boolean eval(ExecutionStateI context) {
		return ((Double) this.rand1.eval(context)) >= ((Double) this.rand2.eval(context));
	}

}
