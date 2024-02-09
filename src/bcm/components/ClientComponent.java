package bcm.components;

import abstractClass.ABSQuery;
import bcm.interfaces.ports.ClientComponentOutboundPort;
import cps.ast.ECont;
import cps.ast.FGather;
import cps.ast.GQuery;
import cps.ast.RGather;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cvm.utils.CyclicBarrierProtocol.RequestI;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.pre.controlflowhelpers.AbstractLocalComposedContinuation;
import fr.sorbonne_u.components.pre.controlflowhelpers.AbstractContinuation;
import fr.sorbonne_u.components.pre.controlflowhelpers.AbstractLocalContinuation;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import fr.sorbonne_u.exceptions.InvariantException;
import itfIMP.RequestIMP;

@RequiredInterfaces(required = { RequestingCI.class })
public class ClientComponent extends AbstractComponent {
    protected String nodeComponentInboundPortURI;
    protected ClientComponentOutboundPort outboundPort;
    protected RequestI request;

    protected ClientComponent(String nodeComponentInboundPortURI) throws Exception {

        super(1, 0);
        assert nodeComponentInboundPortURI != null;
        this.nodeComponentInboundPortURI = nodeComponentInboundPortURI;
        this.outboundPort = new ClientComponentOutboundPort(this);
        this.outboundPort.publishPort();
        this.getTracer().setTitle("Client Component");
        this.getTracer().setRelativePosition(1, 1);
        AbstractComponent.checkImplementationInvariant(this);
        AbstractComponent.checkInvariant(this);
    }

    protected ClientComponent(String reflectionInboundPortURI,
            String valueProvidingInboundPortURI) throws Exception {
        super(reflectionInboundPortURI, 1, 0);
        assert valueProvidingInboundPortURI != null;
        this.nodeComponentInboundPortURI = valueProvidingInboundPortURI;
        this.outboundPort = new ClientComponentOutboundPort(this);
        this.outboundPort.publishPort();
        this.getTracer().setTitle("Client Component");
        this.getTracer().setRelativePosition(1, 1);
    }

    @Override
    public void start() throws ComponentStartException {
        super.start();
        try {
            this.doPortConnection(this.outboundPort.getPortURI(), this.nodeComponentInboundPortURI,
                    RequestingCI.class.getCanonicalName());
        } catch (Exception e) {
            throw new ComponentStartException(e);
        }
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        GQuery query = new GQuery(
                        new RGather("temperature", new FGather("humidity")), new ECont());
        this.request = new RequestIMP(
            "request1",
            query,
            false,
            
        )

    }

}
