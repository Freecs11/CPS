package bcm.components;

import bcm.connector.NodeConnector;
import bcm.interfaces.ports.ClientComponentOutboundPort;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
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

@RequiredInterfaces(required = { RequestingCI.class })
public class ClientComponent extends AbstractComponent {
    protected String nodeComponentInboundPortURI;
    protected ClientComponentOutboundPort outboundPort;
    protected RequestI request;

    protected ClientComponent(String uri,
            String outboundPortURI,
            String nodeoutPort) throws Exception {
        super(outboundPortURI, 1, 0);
        System.out.println("was in here for a breif moment" + uri);
        System.out.println("and the other is : " + outboundPortURI);
        // assert outboundPortURI != null;
        this.nodeComponentInboundPortURI = nodeoutPort;
        this.outboundPort = new ClientComponentOutboundPort(uri, this);
        this.outboundPort.localPublishPort();
        this.getTracer().setTitle("Client Component");
        this.getTracer().setRelativePosition(1, 1);
        System.out.println("nodeInboundPortURI is set to : " + this.nodeComponentInboundPortURI);
        System.out.println("the OutboundPortURI is : " + this.outboundPort.getPortURI());
        AbstractComponent.checkImplementationInvariant(this);
    }

    @Override
    public void start() throws ComponentStartException {
        this.logMessage("starting client component.");
        super.start();
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
        // GQuery query = new GQuery(
        // new RGather("temperature", new FGather("humidity")), new ECont());
        AndBooleanExpr res = new AndBooleanExpr(
                new ConditionalExprBooleanExpr(
                        new EqualConditionalExpr(new SensorRand("humidity"), new ConstantRand(15.0))),
                new ConditionalExprBooleanExpr(
                        new EqualConditionalExpr(new SensorRand("temperature"), new ConstantRand(15.0))));
        BooleanQuery query = new BooleanQuery(res, new EmptyContinuation());
        this.request = new RequestIMPL("req1", query, false, null);
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
    public void finalise() throws Exception {
        this.doPortDisconnection(this.outboundPort.getPortURI());
        this.outboundPort.unpublishPort();
        super.finalise();
    }

}
