package query.abstraction;

import java.util.ArrayList;

import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.interfaces.IEval;

public abstract class AbstractGather implements IEval {
    @Override
    public abstract ArrayList<SensorDataI> eval(ExecutionStateI context);
}
