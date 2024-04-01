package implementation;

import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;

public class RequestContinuationIMPL extends RequestIMPL implements RequestContinuationI {

    private ExecutionStateI executionState;

    public RequestContinuationIMPL(String URI, QueryI queryI, Boolean isAsync, ConnectionInfoI clientConnectionInfo,
            ExecutionStateI executionState) {
        super(URI, queryI, isAsync, clientConnectionInfo);
        this.executionState = executionState;
    }

    @Override
    public ExecutionStateI getExecutionState() {
        return this.executionState;
    }

}
