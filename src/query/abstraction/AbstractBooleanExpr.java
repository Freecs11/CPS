package query.abstraction;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.interfaces.IEval;

public abstract class AbstractBooleanExpr implements IEval {
    @Override
    public abstract Boolean eval(ExecutionStateI context);

}
