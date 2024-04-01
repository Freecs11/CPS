package implementation;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.cps.sensor_network.interfaces.BCM4JavaEndPointDescriptorI;
import fr.sorbonne_u.cps.sensor_network.interfaces.EndPointDescriptorI;

public class EndPointDescIMPL implements BCM4JavaEndPointDescriptorI {
    /**
     * The URI of the inbound port.
     */
    private String inbounPortURI;
    /**
     * The offered interface class.
     */
    private Class<? extends OfferedCI> offertI;

    /**
     * Constructs a <code>BCM4JavaEndPointDescriptor</code> with the specified
     * inbound port URI and offered interface.
     *
     * @param inbounPortURI The URI of the inbound port associated with the
     *                      endpoint.
     * @param offertI       The class representing the offered interface of the
     *                      endpoint.
     */
    public EndPointDescIMPL(
            String inbounPortURI, Class<? extends OfferedCI> offertI) {
        this.inbounPortURI = inbounPortURI;
        this.offertI = offertI;
    }

    /**
     * Retrieves the URI of the inbound port associated with this endpoint
     * descriptor.
     *
     * @return The URI of the inbound port.
     */
    @Override
    public String getInboundPortURI() {
        return inbounPortURI;
    }

    /**
     * Checks if the specified offered interface is supported by this endpoint
     * descriptor.
     *
     * @param inter The class representing the offered interface to check.
     * @return True if the interface is supported, false otherwise.
     */
    @Override
    public boolean isOfferedInterface(Class<? extends OfferedCI> inter) {
        return inter.isAssignableFrom(offertI);
    }

}