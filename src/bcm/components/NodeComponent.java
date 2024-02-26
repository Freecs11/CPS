package bcm.components;

import java.util.HashSet;
import java.util.Set;

import bcm.connector.RegistryConnector;
import bcm.interfaces.ports.NodeComponentInboundPort;
import bcm.interfaces.ports.NodeComponentOutboundPort;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI;
import implementation.NodeInfoIMPL;
import implementation.PositionIMPL;
import implementation.SensorNodeIMPL;

@OfferedInterfaces(offered = { RequestingCI.class })
@RequiredInterfaces(required = { RegistrationCI.class })
public class NodeComponent extends AbstractComponent {
    protected Set<NodeInfoI> neighbours;
    protected final SensorNodeIMPL sensorNode;
    protected final NodeInfoIMPL nodeInfo;
    protected final NodeComponentInboundPort inboundPort;
    protected final NodeComponentOutboundPort outboundPort;

    protected NodeComponent(String uri,
            String sensorNodeInboundPortURI, String registryInbountPortURI) throws Exception {
        // only one thread to ensure the serialised execution of services
        // inside the component.
        super(uri, 1, 0);
        assert sensorNodeInboundPortURI != null;
        assert registryInbountPortURI != null;
        this.neighbours = new HashSet<>();
        this.nodeInfo = new NodeInfoIMPL("node1", new PositionIMPL(20.20, 10.25), null, null, 25.0);
        this.sensorNode = new SensorNodeIMPL(nodeInfo);
        this.inboundPort = new NodeComponentInboundPort(sensorNodeInboundPortURI,
                this);
        this.outboundPort = new NodeComponentOutboundPort(registryInbountPortURI, this);
        this.inboundPort.publishPort();
        this.outboundPort.publishPort();

        this.getTracer().setTitle("Node Component");
        this.getTracer().setRelativePosition(1, 1);

        AbstractComponent.checkImplementationInvariant(this);
        AbstractComponent.checkInvariant(this);
    }

    @Override
    public synchronized void start() throws ComponentStartException {
        super.start();
        try {
            this.doPortConnection(this.outboundPort.getPortURI(), "registry-inbound-port",
                    RegistryConnector.class.getCanonicalName());
        } catch (Exception e) {
            throw new ComponentStartException(e);
        }

        try {
            this.neighbours = outboundPort.register(nodeInfo);
        } catch (Exception e) {
            throw new ComponentStartException(e);
        }
        this.logMessage("starting NodeComponent component.");
    }

    @Override
    public synchronized void finalise() throws Exception {
        this.logMessage("stopping provider component.");
        this.printExecutionLogOnFile("provider");
        super.finalise();
        // System.out.println("NodeComponent finalise");
    }

    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        // the shutdown is a good place to unpublish inbound ports.
        try {
            this.inboundPort.unpublishPort();
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
        // System.out.println("NodeComponent shutdown");
    }

    @Override
    public synchronized void shutdownNow() throws ComponentShutdownException {
        // the shutdown is a good place to unpublish inbound ports.
        try {
            this.inboundPort.unpublishPort();
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdownNow();
        // System.out.println("NodeComponent shutdownNow");
    }

    public QueryResultI returnQueryResult(RequestI request) throws Exception {
        return this.sensorNode.execute(request);
    }
}
