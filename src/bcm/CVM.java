package bcm;

import bcm.components.ClientComponent;
import bcm.components.NodeComponent;
import bcm.components.RegistryComponent;
import bcm.connector.NodeConnector;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.helpers.CVMDebugModes;

public class CVM extends AbstractCVM {

    /** URI of the provider component (convenience). */
    protected static final String NODE_COMPONENT_URI = "my-URI-provider";
    /** URI of the consumer component (convenience). */
    protected static final String CLIENT_COMPONENT_URI = "my-URI-consumer";

    /** URI of the provider outbound port (simplifies the connection). */
    protected static final String URIGetterOutboundPortURI = "oport";
    /** URI of the consumer inbound port (simplifies the connection). */
    protected static final String URIProviderInboundPortURI = "iport";

    protected static final String URINodeRegisterInboundPortURI = "register-inbound-port";
    protected static final String URINodeLookupInboundPortURI = "lookup-inbound-port";
    protected static final String URINodeRegisterOutboundURI = "register-outbound-port";

    public CVM() throws Exception {
        super();
    }

    /**
     * Reference to the provider component to share between deploy
     * and shutdown.
     */
    protected String uriNodeURI;
    /**
     * Reference to the consumer component to share between deploy
     * and shutdown.
     */
    protected String uriClientURI;

    protected String uriRegisterURI;

    @Override
    public void deploy() throws Exception {
        assert !this.deploymentDone();

        // ---------------------------------------------------------------------
        // Configuration phase
        // ---------------------------------------------------------------------

        // debugging mode configuration; comment and uncomment the line to see
        // the difference
        AbstractCVM.DEBUG_MODE.add(CVMDebugModes.LIFE_CYCLE);
        AbstractCVM.DEBUG_MODE.add(CVMDebugModes.INTERFACES);
        AbstractCVM.DEBUG_MODE.add(CVMDebugModes.PORTS);
        AbstractCVM.DEBUG_MODE.add(CVMDebugModes.CONNECTING);
        AbstractCVM.DEBUG_MODE.add(CVMDebugModes.CALLING);
        AbstractCVM.DEBUG_MODE.add(CVMDebugModes.EXECUTOR_SERVICES);

        // ---------------------------------------------------------------------
        // Creation phase
        // ---------------------------------------------------------------------

        // create the node component
        this.uriNodeURI = AbstractComponent.createComponent(NodeComponent.class.getCanonicalName(),
                new Object[] {
                        NODE_COMPONENT_URI,
                        URIProviderInboundPortURI,
                        URINodeRegisterInboundPortURI });
        this.uriRegisterURI = AbstractComponent.createComponent(RegistryComponent.class.getCanonicalName(),
                new Object[] {
                        URINodeRegisterInboundPortURI,
                        1, 0 });

        // create the client component
        this.uriClientURI = AbstractComponent.createComponent(ClientComponent.class.getCanonicalName(),
                new Object[] {
                        URIGetterOutboundPortURI,
                        CLIENT_COMPONENT_URI,
                        URIProviderInboundPortURI });
        assert this.isDeployedComponent(this.uriNodeURI);
        this.toggleTracing(this.uriNodeURI);
        this.toggleLogging(this.uriNodeURI);

        assert this.isDeployedComponent(this.uriClientURI);
        this.toggleTracing(this.uriClientURI);
        this.toggleLogging(this.uriClientURI);

        // ---------------------------------------------------------------------
        // Connection phase
        // ---------------------------------------------------------------------

        // do the connection
        this.doPortConnection(this.uriClientURI, URIGetterOutboundPortURI, URIProviderInboundPortURI,
                NodeConnector.class.getCanonicalName());
        this.doPortConnection(this.uriNodeURI, 

        // ---------------------------------------------------------------------
        // Deployment done
        // ---------------------------------------------------------------------
        super.deploy();
    }

    @Override
    public void finalise() throws Exception {
        // Port disconnection
        this.doPortDisconnection(this.uriClientURI, URIGetterOutboundPortURI);
        super.finalise();
        // System.out.println("CVM finalise");
    }

    @Override
    public void shutdown() throws Exception {
        assert this.allFinalised();

        super.shutdown();
        // System.out.println("CVM shutdown");
    }

    public static void main(String[] args) {
        try {
            CVM cvm = new CVM();
            cvm.startStandardLifeCycle(2000L);
            Thread.sleep(10000L);
            System.exit(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
