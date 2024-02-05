package cps.interfaces;

import cps.ast.Position;

public interface IParamContext {

	public Position getPosition();

	public void setPosition(Position pos);

	public String getNodeId();

	public void setNodeId(String nodeId);

	public Double get(String key);

}
