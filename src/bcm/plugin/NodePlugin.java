package bcm.plugin;

import bcm.ports.RegistrationOutboundPort;
import bcm.ports.RequestingInboundPort;
import bcm.ports.SensorNodeP2PInboundPort;
import fr.sorbonne_u.components.AbstractPlugin;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI;

public class NodePlugin
        extends AbstractPlugin {
    private static final long serialVersionUID = 1L;

    protected RequestingInboundPort requestingInboundPort;
    protected SensorNodeP2PInboundPort sensorNodeP2PInboundPort;
    protected RegistrationOutboundPort RegistrationOutboundPort;

    @Override
    public void installOn(ComponentI owner) throws Exception {
        super.installOn(owner);
        this.addRequiredInterface(RegistrationCI.class);
        this.addRequiredInterface(SensorNodeP2PCI.class);
        this.addRequiredInterface(RequestingCI.class);
    }

    public void initialise() throws Exception {
        super.initialise();

        this.requestingInboundPort = new RequestingInboundPort(this.owner);
    }

}
