package query.abstraction;

import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;
import query.interfaces.IEval;

public abstract class AbstractQuery implements QueryI, IEval {
    @Override
    public abstract QueryResultI eval(ExecutionStateI context);
}
