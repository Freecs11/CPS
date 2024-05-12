package implementation;

import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>RequestIMPL</code> acts as the implementation of the
 * <code>RequestI</code> interface. It is used to define a request.
 * </p>
 * 
 */
public class RequestIMPL implements RequestI {

	private String URI;
	private QueryI queryI;
	private Boolean isAsync;
	private ConnectionInfoI clientConnectionInfo;

	private static final long serialVersionUID = 2824371013284123271L;
	
	/**
	 * Constructor of the RequestIMPL 
	 * 
	 * @param URI                 the URI of the request 
	 * @param queryI              the query of the request
	 * @param isAsync             the request is asynchronous
	 * @param clientConnectionInfo the connection information of the client
	 */
	public RequestIMPL(String URI, QueryI queryI, Boolean isAsync, ConnectionInfoI clientConnectionInfo) {
		super();
		this.URI = URI;
		this.queryI = queryI;
		this.isAsync = isAsync;
		this.clientConnectionInfo = clientConnectionInfo;
	}

	/**
	 * see {@link fr.sorbonne_u.cps.sensor_network.interfaces.RequestI#requestURI()}
	 */
	@Override
	public String requestURI() {
		return this.URI;
	}

	/**
	 * see {@link fr.sorbonne_u.cps.sensor_network.interfaces.RequestI#getQueryCode()}
	 */
	@Override
	public QueryI getQueryCode() {
		return this.queryI;
	}

	/**
	 * see {@link fr.sorbonne_u.cps.sensor_network.interfaces.RequestI#isAsynchronous()}
	 */
	@Override
	public boolean isAsynchronous() {
		return this.isAsync;
	}

	/**
	 * see {@link fr.sorbonne_u.cps.sensor_network.interfaces.RequestI#clientConnectionInfo()}
	 */
	@Override
	public ConnectionInfoI clientConnectionInfo() {
		return this.clientConnectionInfo;
	}

}
