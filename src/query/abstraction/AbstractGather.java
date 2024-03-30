package query.abstraction;

import java.util.ArrayList;
import java.util.Map;

import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import query.interfaces.IEval;

public abstract class AbstractGather implements IEval {
    @Override
    public abstract ArrayList<SensorDataI> eval(ExecutionStateI context);
}
