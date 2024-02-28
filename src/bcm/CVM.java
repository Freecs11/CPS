package bcm;

import bcm.components.ClientComponent;
import bcm.components.NodeComponent;
import bcm.components.RegistryComponent;
import bcm.connector.LookUpRegistryConnector;
import bcm.connector.NodeConnector;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.helpers.CVMDebugModes;
import implementation.NodeInfoIMPL;
import implementation.PositionIMPL;

public class CVM extends AbstractCVM {

    /** URI of the provider component (convenience). */
    protected static final String NODE_COMPONENT_URI = "node-URI";
    protected static final String NODE2_COMPONENT_URI = "node2-URI";
    /** URI of the consumer component (convenience). */
    protected static final String CLIENT_COMPONENT_URI = "client-URI";
    protected static final String REGISTER_COMPONENT_URI = "register-URI";

    // /** URI of the provider outbound port (simplifies the connection). */
    // protected static final String URIGetterOutboundPortURI = "oport";
    // /** URI of the consumer inbound port (simplifies the connection). */
    // protected static final String URIProviderInboundPortURI = "iport";

    protected static final String REGISTER_IN_BOUND_PORT_URI = "register-inbound-port";
    protected static final String LOOKUP_IN_BOUND_PORT_URI = "lookup_inbound-port";
    protected static final String CLIENTS_OUT_BOUND_PORT_URI = "client-outbound-port";
    protected static final String NODE_TO_REG_OUT_BOUND_PORT_URI = "node2reg-outbound-port";
    protected static final String NODE_IN_BOUND_PORT_URI = "node-inbound-port";
    protected static final String NODE2_TO_REG_OUT_BOUND_PORT_URI = "node2_2reg-outbound-port";
    protected static final String NODE2_IN_BOUND_PORT_URI = "node2-inbound-port";

    public CVM() throws Exception {
        super();
    }

    /**
     * Reference to the provider component to share between deploy
     * and shutdown.
     */
    protected String uriNodeURI;
    protected String uriNode2URI;
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
        this.uriRegisterURI = AbstractComponent.createComponent(RegistryComponent.class.getCanonicalName(),
                new Object[] {
                        REGISTER_COMPONENT_URI,
                        1, 0,
                        LOOKUP_IN_BOUND_PORT_URI,
                        REGISTER_IN_BOUND_PORT_URI });
        // create the node component

        this.uriNode2URI = AbstractComponent.createComponent(NodeComponent.class.getCanonicalName(),
                new Object[] {
                        NODE2_COMPONENT_URI,
                        NODE2_IN_BOUND_PORT_URI,
                        NODE2_TO_REG_OUT_BOUND_PORT_URI,
                        "node2",
                        10.0, 20.0, 20.0,
                        REGISTER_IN_BOUND_PORT_URI });
        this.uriNodeURI = AbstractComponent.createComponent(NodeComponent.class.getCanonicalName(),
                new Object[] {
                        NODE_COMPONENT_URI,
                        NODE_IN_BOUND_PORT_URI,
                        NODE_TO_REG_OUT_BOUND_PORT_URI,
                        "node1",
                        20.0, 20.0, 45.0,
                        REGISTER_IN_BOUND_PORT_URI });

        // create the client component
        this.uriClientURI = AbstractComponent.createComponent(ClientComponent.class.getCanonicalName(),
                new Object[] {
                        CLIENT_COMPONENT_URI,
                        CLIENTS_OUT_BOUND_PORT_URI }); // to be changed
        assert this.isDeployedComponent(this.uriNodeURI);
        this.toggleTracing(this.uriNodeURI);
        this.toggleLogging(this.uriNodeURI);

        assert this.isDeployedComponent(this.uriNode2URI);
        this.toggleTracing(this.uriNode2URI);
        this.toggleLogging(this.uriNode2URI);

        assert this.isDeployedComponent(this.uriClientURI);
        this.toggleTracing(this.uriClientURI);
        this.toggleLogging(this.uriClientURI);

        assert this.isDeployedComponent(this.uriRegisterURI);
        this.toggleTracing(this.uriRegisterURI);
        this.toggleLogging(this.uriRegisterURI);

        // ---------------------------------------------------------------------
        // Connection phase
        // ---------------------------------------------------------------------

        // do the connection
        // Connection directe Client<->Node
        // this.doPortConnection(this.uriClientURI, URIGetterOutboundPortURI,
        // URIProviderInboundPortURI,
        // NodeConnector.class.getCanonicalName());

        this.doPortConnection(this.uriClientURI,
                CLIENTS_OUT_BOUND_PORT_URI,
                LOOKUP_IN_BOUND_PORT_URI,
                LookUpRegistryConnector.class.getCanonicalName());
        // ---------------------------------------------------------------------
        // Deployment done
        // ---------------------------------------------------------------------
        super.deploy();
    }

    @Override
    public void finalise() throws Exception {
        // Port disconnection
        this.doPortDisconnection(this.uriClientURI, CLIENTS_OUT_BOUND_PORT_URI);
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
