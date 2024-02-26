package bcm.components;

import bcm.connector.LookUpRegistryConnector;
import bcm.interfaces.ports.ClientComponentOutboundPort;
import bcm.interfaces.ports.ClientRegisterOutboundPort;
import bcm.interfaces.ports.LookupInboundPort;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI;
import implementation.RequestIMPL;
import query.ast.AndBooleanExpr;
import query.ast.BooleanQuery;
import query.ast.ConditionalExprBooleanExpr;
import query.ast.ConstantRand;
import query.ast.EmptyContinuation;
import query.ast.EqualConditionalExpr;
import query.ast.FinalGather;
import query.ast.GatherQuery;
import query.ast.RecursiveGather;
import query.ast.SensorRand;

@RequiredInterfaces(required = { RequestingCI.class, LookupCI.class })
public class ClientComponent extends AbstractComponent {
    protected ClientComponentOutboundPort outboundPort;
    protected ClientRegisterOutboundPort clientRegisterOutboundPort;
    protected RequestI request;

    protected ClientComponent(String uri,
            String outboundPortURI) throws Exception {
        super(uri, 1, 0);
        System.out.println("was in here for a breif moment" + uri);
        System.out.println("and the other is : " + outboundPortURI);
        // assert outboundPortURI != null;
        // this.outboundPort = new ClientComponentOutboundPort(uri, this);
        // this.outboundPort.localPublishPort();
        // this.lookupInboundPortURI = lookupInboundPortURI;
        System.err.println("tihs : " + this.isPortExisting(outboundPortURI));
        this.clientRegisterOutboundPort = new ClientRegisterOutboundPort(outboundPortURI, this);
        System.err.println("clientRegisterOutboundPortURI: " + this.clientRegisterOutboundPort.getPortURI());
        this.clientRegisterOutboundPort.publishPort();
        this.getTracer().setTitle("Client Component");
        this.getTracer().setRelativePosition(1, 1);
        // System.out.println("nodeInboundPortURI is set to : " +
        // this.nodeComponentInboundPortURI);
        // System.out.println("the OutboundPortURI is : " +
        // this.outboundPort.getPortURI());
        AbstractComponent.checkImplementationInvariant(this);
    }

    @Override
    public synchronized void start() throws ComponentStartException {
        this.logMessage("starting client component.");
        super.start();
        // try {
        // this.doPortConnection(this.lookupInboundPortURI,
        // this.clientRegisterOutboundPort.getPortURI(),
        // LookUpRegistryConnector.class.getCanonicalName());
        // } catch (Exception e) {
        // throw new ComponentStartException(e);
        // }

        // try {
        // this.doPortConnection(this.outboundPort.getPortURI(),
        // this.nodeComponentInboundPortURI,
        // nodeConnector.class.getCanonicalName());
        // } catch (Exception e) {
        // throw new ComponentStartException(e);
        // }
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        GatherQuery query = new GatherQuery(
                new RecursiveGather("temperature", new FinalGather("humidity")), new EmptyContinuation());
        String nodeIdentifier = "node1";
        // ConnectionInfoI nodeInfo = ClientComponent.this.clientRegisterOutboundPort
        // .findByIdentifier(nodeIdentifier);
        // System.err.println("NodeInfo: " + nodeInfo.toString());
        this.runTask(new AbstractTask() {
            @Override
            public void run() {
                try {
                    System.err.println("NodeInfo:  dkjfkj ");
                    ConnectionInfoI nodeInfo = ClientComponent.this.clientRegisterOutboundPort
                            .findByIdentifier(nodeIdentifier); // modify to return an implementation of connectionInfo
                    // Boolean res =
                    // ClientComponent.this.clientRegisterOutboundPort.registered(nodeIdentifier);
                    System.err.println("NodeInfo: " + nodeInfo.nodeIdentifier());
                    ClientComponent.this.outboundPort = new ClientComponentOutboundPort(nodeInfo.nodeIdentifier(),
                            ClientComponent.this);
                    ClientComponent.this.outboundPort.publishPort();
                    // ClientComponent.this.doPortConnection(ClientComponent.this.outboundPort.getPortURI(),
                    // nodeInfo.endPointInfo(), LookUpRegistryConnector.class.getCanonicalName());
                } catch (Exception e) {
                    System.err.println("NodeInfo:");

                    e.printStackTrace();
                }
            }
        });

        // AndBooleanExpr res = new AndBooleanExpr(
        // new ConditionalExprBooleanExpr(
        // new EqualConditionalExpr(new SensorRand("humidity"), new
        // ConstantRand(15.0))),
        // new ConditionalExprBooleanExpr(
        // new EqualConditionalExpr(new SensorRand("temperature"), new
        // ConstantRand(15.0))));
        // BooleanQuery query = new BooleanQuery(res, new EmptyContinuation());
        this.request = new RequestIMPL("req1", query, false, null); // change later
        RequestI re = this.request;
        this.runTask(new AbstractTask() {
            @Override
            public void run() {
                try {
                    QueryResultI res = ClientComponent.this.outboundPort.execute(re);
                    ClientComponent.this.logMessage("Query result: " + res.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public synchronized void finalise() throws Exception {
        if (this.outboundPort.connected()) {
            this.doPortDisconnection(this.outboundPort.getPortURI());
        }
        this.outboundPort.unpublishPort();
        super.finalise();
        // System.out.println("finalise ClientComponent");
    }

}
