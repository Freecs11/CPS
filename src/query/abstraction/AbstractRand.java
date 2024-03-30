package query.abstraction;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.interfaces.IEval;

public abstract class AbstractRand implements IEval {

    public abstract Object eval(ExecutionStateI context);

}
