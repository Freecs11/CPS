package implementation;

import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;

public class RequestIMPL implements RequestI {

	private String URI ;
	private QueryI queryI;
	private Boolean isAsync;
	private ConnectionInfoI clientConnectionInfo;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2824371013284123271L;


	public RequestIMPL(String uRI, QueryI queryI, Boolean isAsync, ConnectionInfoI clientConnectionInfo) {
		super();
		URI = uRI;
		this.queryI = queryI;
		this.isAsync = isAsync;
		this.clientConnectionInfo = clientConnectionInfo;
	}

	@Override
	public String requestURI() {
		// TODO Auto-generated method stub
		return this.URI;
	}

	@Override
	public QueryI getQueryCode() {
		// TODO Auto-generated method stub
		return this.queryI;
	}

	@Override
	public boolean isAsynchronous() {
		// TODO Auto-generated method stub
		return this.isAsync;
	}

	@Override
	public ConnectionInfoI clientConnectionInfo() {
		// TODO Auto-generated method stub
		return this.clientConnectionInfo;
	}

}
