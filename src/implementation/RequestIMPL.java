package implementation;

import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;

public class RequestIMPL implements RequestI {

	private String URI;
	private QueryI queryI;
	private Boolean isAsync;
	private ConnectionInfoI clientConnectionInfo;

	/**
	 * 
	 */
	private static final long serialVersionUID = 2824371013284123271L;

	public RequestIMPL(String URI, QueryI queryI, Boolean isAsync, ConnectionInfoI clientConnectionInfo) {
		super();
		this.URI = URI;
		this.queryI = queryI;
		this.isAsync = isAsync;
		this.clientConnectionInfo = clientConnectionInfo;
	}

	@Override
	public String requestURI() {
		return this.URI;
	}

	@Override
	public QueryI getQueryCode() {
		return this.queryI;
	}

	@Override
	public boolean isAsynchronous() {
		return this.isAsync;
	}

	@Override
	public ConnectionInfoI clientConnectionInfo() {
		return this.clientConnectionInfo;
	}

}
