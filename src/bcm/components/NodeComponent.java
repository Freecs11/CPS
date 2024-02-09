package bcm.components;

import bcm.interfaces.ports.NodeComponentInboundPort;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import itfIMP.PositionIMP;
import itfIMP.SensorNode;

@OfferedInterfaces(offered = { RequestingCI.class })
public class NodeComponent extends AbstractComponent {
    protected final SensorNode sensorNode;
    protected final NodeComponentInboundPort inboundPort;

    protected NodeComponent(
            String sensorNodeInboundPortURI) throws Exception {
        // only one thread to ensure the serialised execution of services
        // inside the component.
        super(1, 0);
        assert sensorNodeInboundPortURI != null;

        this.sensorNode = new SensorNode("node1", null, new PositionIMP(20.2, 40.2), 0, null);
        this.inboundPort = new NodeComponentInboundPort(sensorNodeInboundPortURI,
                this);
        this.inboundPort.publishPort();

        this.getTracer().setTitle("Node Component");
        this.getTracer().setRelativePosition(1, 1);

        AbstractComponent.checkImplementationInvariant(this);
        AbstractComponent.checkInvariant(this);
    }

    @Override
    public void shutdown() throws ComponentShutdownException {
        // the shutdown is a good place to unpublish inbound ports.
        try {
            this.inboundPort.unpublishPort();
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
    }

    @Override
    public void shutdownNow() throws ComponentShutdownException {
        // the shutdown is a good place to unpublish inbound ports.
        try {
            this.inboundPort.unpublishPort();
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdownNow();
    }

    public QueryResultI returnQueryResult(RequestI request) throws Exception {
        return this.sensorNode.execute(request);
    }
}
