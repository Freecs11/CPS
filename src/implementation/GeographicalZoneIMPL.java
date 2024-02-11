package implementation;

import fr.sorbonne_u.cps.sensor_network.interfaces.GeographicalZoneI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;

public class GeographicalZoneIMPL implements GeographicalZoneI {
	private PositionI position;
	private Double maxRange;
	
	public GeographicalZoneIMPL(PositionI position,Double maxRange) {
		this.position=position;
		this.maxRange=maxRange;
	}

	@Override
	public boolean in(PositionI p) {
		return this.position.distance(p) <=this.maxRange;
	}

}
