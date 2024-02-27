package bcm.connector;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;

public class NodeConnector extends AbstractConnector
        implements RequestingCI, RequestContinuationI {

    @Override
    public QueryResultI execute(RequestI request) throws Exception {
        return ((RequestingCI) this.offering).execute(request);
    }

    @Override
    public void executeAsync(RequestI request) throws Exception {
        ((RequestingCI) this.offering).executeAsync(request);
    }

    @Override
    public String requestURI() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'requestURI'");
    }

    @Override
    public QueryI getQueryCode() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getQueryCode'");
    }

    @Override
    public boolean isAsynchronous() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isAsynchronous'");
    }

    @Override
    public ConnectionInfoI clientConnectionInfo() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'clientConnectionInfo'");
    }

    @Override
    public ExecutionStateI getExecutionState() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getExecutionState'");
    }

}
