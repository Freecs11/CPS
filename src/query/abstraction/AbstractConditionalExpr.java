package query.abstraction;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.interfaces.IEval;

public abstract class AbstractConditionalExpr implements IEval {
    @Override
    public abstract Object eval(ExecutionStateI context);

}
