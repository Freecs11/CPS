package query.abstraction;

import java.util.Set;

import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.interfaces.IEval;

public abstract class AbstractDirections implements IEval {
    @Override
    public abstract Set<Direction> eval(ExecutionStateI context);

}
