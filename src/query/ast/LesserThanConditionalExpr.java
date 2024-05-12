package query.ast;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.abstraction.AbstractConditionalExpr;
import query.abstraction.AbstractRand;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>LesserThanConditionalExpr</code> represents a lesser than
 * conditional expression in the AST.
 * </p>
 * 
 * 
 */
public class LesserThanConditionalExpr extends AbstractConditionalExpr {

	private final AbstractRand rand1;
	private final AbstractRand rand2;

	/**
	 * Constructor of the LesserThanConditionalExpr
	 * 
	 * @param rand1
	 * @param rand2
	 */
	public LesserThanConditionalExpr(AbstractRand rand1, AbstractRand rand2) {
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
		return ((Double) this.rand1.eval(context)) < ((Double) this.rand2.eval(context));
	}

}
