package query.abstraction;

import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.interfaces.IEval;

public abstract class AbstractBase implements IEval {

    @Override
    public abstract PositionI eval(ExecutionStateI context);
}
