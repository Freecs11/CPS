package bcm.components;

import bcm.connector.LookUpRegistryConnector;
import bcm.connector.NodeConnector;
import bcm.interfaces.ports.ClientComponentOutboundPort;
import bcm.interfaces.ports.Client2RegisterOutboundPort;
import bcm.interfaces.ports.LookupInboundPort;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI;
import implementation.EndPointDescIMP;
import implementation.RequestContinuationIMPL;
import implementation.RequestIMPL;
import implementation.requestsIMPL.ExecutionStateIMPL;
import query.ast.AndBooleanExpr;
import query.ast.BooleanQuery;
import query.ast.ConditionalExprBooleanExpr;
import query.ast.ConstantRand;
import query.ast.DirectionContinuation;
import query.ast.EmptyContinuation;
import query.ast.EqualConditionalExpr;
import query.ast.FinalDirections;
import query.ast.FinalGather;
import query.ast.FloodingContinuation;
import query.ast.GatherQuery;
import query.ast.OrBooleanExpr;
import query.ast.RecursiveDirections;
import query.ast.RecursiveGather;
import query.ast.RelativeBase;
import query.ast.SensorRand;

@RequiredInterfaces(required = { RequestingCI.class, LookupCI.class })
public class ClientComponent extends AbstractComponent {
    protected ClientComponentOutboundPort outboundPort;
    protected Client2RegisterOutboundPort client2RegisterOutboundPort;
    protected String registryInboundPortURI;
    protected RequestI request;

    protected ClientComponent(String uri, String registryInboundPortURI) throws Exception {
        super(uri, 1, 0);
        this.client2RegisterOutboundPort = new Client2RegisterOutboundPort(AbstractOutboundPort.generatePortURI(),
                this);
        this.client2RegisterOutboundPort.publishPort();
        this.registryInboundPortURI = registryInboundPortURI;
        this.getTracer().setTitle("Client Component");
        this.getTracer().setRelativePosition(1, 1);
        AbstractComponent.checkImplementationInvariant(this);
        // TODO: recheck this check
        AbstractComponent.checkInvariant(this);
    }

    @Override
    public synchronized void start() throws ComponentStartException {
        this.logMessage("starting client component.");
        super.start();
        // ---------Connection to the registry component---------
        try {
            this.doPortConnection(this.client2RegisterOutboundPort.getPortURI(),
                    registryInboundPortURI,
                    LookUpRegistryConnector.class.getCanonicalName());
        } catch (Exception e) {
            throw new ComponentStartException(e);
        }
    }

    @Override
    public void execute() throws Exception {
        super.execute();

        // Finding node with identifier "node1"
        String nodeIdentifier = "node1";
        this.runTask(new AbstractTask() {
            @Override
            public void run() {
                try {
                    ConnectionInfoI nodeInfo = ClientComponent.this.client2RegisterOutboundPort
                            .findByIdentifier(nodeIdentifier); // modify to return an implementation of connectionInfo
                    // System.err.println("NodeInfo: " + nodeInfo.nodeIdentifier());
                    ClientComponent.this.outboundPort = new ClientComponentOutboundPort(nodeInfo.nodeIdentifier(),
                            ClientComponent.this);
                    ClientComponent.this.outboundPort.publishPort();
                    ClientComponent.this.doPortConnection(
                            ClientComponent.this.outboundPort.getPortURI(),
                            ((EndPointDescIMP) nodeInfo.endPointInfo()).getURI(),
                            NodeConnector.class.getCanonicalName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // -------------------Gather Query Test-------------------
        // GatherQuery query = new GatherQuery(
        // new RecursiveGather("temperature",
        // new FinalGather("humidity")),
        // // new FloodingContinuation(new RelativeBase(), 15.0));
        // // new DirectionContinuation(3, new FinalDirections(Direction.SE)));
        // new DirectionContinuation(3, new RecursiveDirections(Direction.SE, new
        // FinalDirections(Direction.NE))));

        // -------------Boolean Query Test---------------
        OrBooleanExpr res = new OrBooleanExpr(
                new ConditionalExprBooleanExpr(
                        new EqualConditionalExpr(new SensorRand("humidity"), new ConstantRand(20.0))),
                new ConditionalExprBooleanExpr(
                        new EqualConditionalExpr(new SensorRand("temperature"), new ConstantRand(20.0))));

        BooleanQuery query2 = new BooleanQuery(res,
                new DirectionContinuation(3, new RecursiveDirections(Direction.SE, new FinalDirections(Direction.NE))));

        // ------Actual Request to look up-------
        this.request = new RequestIMPL("req1",
                // query,
                query2,
                false,
                null); // change later

        this.runTask(new AbstractTask() {
            @Override
            public void run() {
                try {
                    QueryResultI res = ClientComponent.this.outboundPort.execute(request);
                    ClientComponent.this.logMessage("Query result: " + res.toString());
                    ClientComponent.this.doPortDisconnection(ClientComponent.this.outboundPort.getPortURI());
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
        if (this.client2RegisterOutboundPort.connected()) {
            this.doPortDisconnection(this.client2RegisterOutboundPort.getPortURI());
        }
        this.client2RegisterOutboundPort.unpublishPort();
        super.finalise();
        // System.out.println("finalise ClientComponent");
    }

}
