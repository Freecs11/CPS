package query.abstraction;

import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.interfaces.IEval;

/**
 * <p> Description </p>
 * <p> The class <code>AbstractBase</code> acts as the as an implementation of the <code>IEval</code> interface. </p>
 */
public abstract class AbstractBase implements IEval {

    @Override
    public abstract PositionI eval(ExecutionStateI context);
}
