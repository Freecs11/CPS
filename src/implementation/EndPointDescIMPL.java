package implementation;

import fr.sorbonne_u.cps.sensor_network.interfaces.EndPointDescriptorI;

public class EndPointDescIMPL implements EndPointDescriptorI {
    private final String URI;

    public EndPointDescIMPL(String address) {
        this.URI = address;
    }

    public String getURI() {
        return this.URI;
    }

}