package implementation;

import fr.sorbonne_u.cps.sensor_network.interfaces.GeographicalZoneI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;

/**
 * <p>
 * <strong>Description</strong>
 * </p>
 * <p>
 * The class <code>GeographicalZoneIMPL</code>
 * acts as the implementation of the <code>GeographicalZoneI</code> interface.
 * It is used to define a geographical zone.
 * </p>
 */
public class GeographicalZoneIMPL implements GeographicalZoneI {
	private PositionI position;
	private Double maxRange;

	/**
	 * Constructor of the GeographicalZoneIMPL
	 * 
	 * @param position the position of the zone center
	 * @param maxRange the maximum range of the zone
	 */
	public GeographicalZoneIMPL(PositionI position, Double maxRange) {
		this.position = position;
		this.maxRange = maxRange;
	}

	/**
	 * see
	 * {@link GeographicalZoneI#getPosition()}
	 */
	@Override
	public boolean in(PositionI p) {
		return this.position.distance(p) <= this.maxRange;
	}

}
