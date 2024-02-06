package itfIMP;

import fr.sorbonne_u.cps.sensor_network.interfaces.GeographicalZoneI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;

public class GeographicalZoneIMP implements GeographicalZoneI {
	private PositionI position;
	private Double maxRange;
	
	public GeographicalZoneIMP(PositionI position,Double maxRange) {
		this.position=position;
		this.maxRange=maxRange;
	}

	@Override
	public boolean in(PositionI p) {
		return this.position.distance(p) <=this.maxRange;
	}

}
