package implementation;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.cps.sensor_network.interfaces.BCM4JavaEndPointDescriptorI;
import fr.sorbonne_u.cps.sensor_network.interfaces.EndPointDescriptorI;

public class EndPointDescIMP implements EndPointDescriptorI {
    private final String URI;

    public EndPointDescIMP(String address) {
        this.URI = address;
    }

    public String getURI() {
        return this.URI;
    }

}