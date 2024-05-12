package implementation;

import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>RequestContinuationIMPL</code> acts as the implementation of
 * the <code>RequestContinuationI</code> interface. It is used to define a
 * request continuation.
 * </p>
 */
public class RequestContinuationIMPL extends RequestIMPL implements RequestContinuationI {

    private ExecutionStateI executionState;

    /**
     * Constructor of the RequestContinuationIMPL
     * 
     * @param URI                 the URI of the request
     * @param queryI              the query of the request
     * @param isAsync             the request is asynchronous
     * @param clientConnectionInfo the connection information of the client
     * @param executionState      the execution state of the request
     */
    public RequestContinuationIMPL(String URI, QueryI queryI, Boolean isAsync, ConnectionInfoI clientConnectionInfo,
            ExecutionStateI executionState) {
        super(URI, queryI, isAsync, clientConnectionInfo);
        this.executionState = executionState;
    }

    /**
     * see {@link fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI#getExecutionState()}
     */
    @Override
    public ExecutionStateI getExecutionState() {
        return this.executionState;
    }

}
