package bcm.components;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import bcm.interfaces.ports.LookupInboundPort;
import bcm.interfaces.ports.RegistryInboundPort;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.GeographicalZoneI;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI;
import implementation.LookUpIMPL;
import implementation.RegistrationIMPL;

@OfferedInterfaces(offered = {
        LookupCI.class, RegistrationCI.class })
public class RegistryComponent extends AbstractComponent {
    protected RegistrationCI registry;
    protected LookupInboundPort lookUpInboundPort;
    protected RegistryInboundPort registryInboundPort;
    protected Map<String, NodeInfoI> nodesMap;

    protected RegistryComponent(String reflectionInboundPortURI,
            int nbThreads, int nbSchedulableThreads,
            String lookupInboundPortURI,
            String registerInboundPortURI) {
        super(reflectionInboundPortURI, nbThreads, nbSchedulableThreads);
        this.nodesMap = new HashMap<>();
        // this.lookup = new LookUpIMPL(nodesMap);
        this.registry = new RegistrationIMPL(nodesMap);
        try {
            this.lookUpInboundPort = new LookupInboundPort(lookupInboundPortURI, this);
            this.registryInboundPort = new RegistryInboundPort(registerInboundPortURI, this);
            this.lookUpInboundPort.publishPort();
            this.registryInboundPort.publishPort();
            System.err.println("RegistryComponent constructor done");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void start() throws ComponentStartException {
        super.start();
        this.logMessage("starting Registry component.");
    }

    public void addNodeToMap(NodeInfoI nodeInfo) {
        this.nodesMap.put(nodeInfo.nodeIdentifier(), nodeInfo);
    }

    public ConnectionInfoI findByIdentifier(String sensorNodeId) throws Exception {
        System.err.println("print was here");
        return this.nodesMap.get(sensorNodeId);
        // return this.lookup.findByIdentifier(sensorNodeId);
    }

    public Set<ConnectionInfoI> findByZone(GeographicalZoneI z) throws Exception {
        // return this.lookup.findByZone(z);
        assert z != null;
        Set<ConnectionInfoI> result = new HashSet<>();
        for (NodeInfoI nodeInfo : nodesMap.values()) {
            if (z.in(nodeInfo.nodePosition())) {
                result.add(nodeInfo);
            }
        }
        return result;
    }

    public boolean registered(String nodeIdentifier) throws Exception {
        return this.registry.registered(nodeIdentifier);
    }

    public Set<NodeInfoI> register(NodeInfoI nodeInfo) throws Exception {
        return this.registry.register(nodeInfo);
    }

    public NodeInfoI findNewNeighbour(NodeInfoI nodeInfo, Direction d) throws Exception {
        return this.registry.findNewNeighbour(nodeInfo, d);
    }

    public void unregister(String nodeIdentifier) throws Exception {
        this.registry.unregister(nodeIdentifier);
    }

    @Override
    public synchronized void finalise() throws Exception {
        this.logMessage("stopping provider component.");
        this.printExecutionLogOnFile("provider");
        super.finalise();
    }

    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        // the shutdown is a good place to unpublish inbound ports.
        try {
            this.lookUpInboundPort.unpublishPort();
            this.registryInboundPort.unpublishPort();
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
    }

    @Override
    public synchronized void shutdownNow() throws ComponentShutdownException {
        // the shutdown is a good place to unpublish inbound ports.
        try {
            this.lookUpInboundPort.unpublishPort();
            this.registryInboundPort.unpublishPort();
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdownNow();
    }

}
