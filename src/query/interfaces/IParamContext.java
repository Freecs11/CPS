package query.interfaces;

import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;

public interface IParamContext {

	public PositionI getPosition();

	public void setPosition(PositionI pos);

	public String getNodeId();

	public void setNodeId(String nodeId);

	public Double get(String key);

}
