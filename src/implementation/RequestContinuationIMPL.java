package implementation;

import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;

public class RequestContinuationIMPL implements RequestContinuationI {
    private RequestI request;
    private ExecutionStateI executionState;

    public RequestContinuationIMPL(RequestI request, ExecutionStateI executionState) {
        this.request = request;
        this.executionState = executionState;
    }

    @Override
    public String requestURI() {
        return this.request.requestURI();
    }

    @Override
    public QueryI getQueryCode() {
        return this.request.getQueryCode();
    }

    @Override
    public boolean isAsynchronous() {
        return this.request.isAsynchronous();
    }

    @Override
    public ConnectionInfoI clientConnectionInfo() {
        return this.request.clientConnectionInfo();
    }

    @Override
    public ExecutionStateI getExecutionState() {
        return this.executionState;
    }

    public RequestI getRequest() {
        return request;
    }

}
