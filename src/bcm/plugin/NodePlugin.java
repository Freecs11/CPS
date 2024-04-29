package bcm.plugin;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import bcm.connectors.RegistryConnector;
import bcm.connectors.SensorNodeConnector;
import bcm.ports.RegistrationOutboundPort;
import bcm.ports.RequestingInboundPort;
import bcm.ports.SensorNodeP2PInboundPort;
import bcm.ports.SensorNodeP2POutboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.BCM4JavaEndPointDescriptorI;
import fr.sorbonne_u.components.AbstractPlugin;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.RegistrationCI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import implementation.request.ProcessingNodeIMPL;

public class NodePlugin
        extends AbstractPlugin {
    private static final long serialVersionUID = 1L;

    protected Set<NodeInfoI> neighbours;

    private ProcessingNodeI processingNode;

    // Liste des données des capteurs,,
    private ArrayList<SensorDataI> sensorData;

    // Uris des requetes deja executees
    protected Set<String> requestURIs;

    protected NodeInfoI nodeInfo;

    protected ConcurrentHashMap<NodeInfoI, SensorNodeP2POutboundPort> nodeInfoToP2POutboundPortMap;

    protected RequestingInboundPort requestingInboundPort;
    protected SensorNodeP2PInboundPort sensorNodeP2PInboundPort;
    protected RegistrationOutboundPort RegistrationOutboundPort;

    protected String registerInboundPortURI;

    private final ReadWriteLock neigboursLock = new ReentrantReadWriteLock();

    public NodePlugin(String registerInboundPortURI, NodeInfoI nodeInfo, ProcessingNodeI processingNode) {
        this.registerInboundPortURI = registerInboundPortURI;
        this.nodeInfo = nodeInfo;
        this.processingNode = processingNode;
    }

    @Override
    public void installOn(ComponentI owner) throws Exception {
        super.installOn(owner);
        // this.addRequiredInterface(RegistrationCI.class);
        // this.addRequiredInterface(SensorNodeP2PCI.class);
        // this.addRequiredInterface(RequestingCI.class);
    }

    @Override
    public void initialise() throws Exception {

        this.requestingInboundPort = new RequestingInboundPort(this.getOwner());
        this.requestingInboundPort.publishPort();
        this.sensorNodeP2PInboundPort = new SensorNodeP2PInboundPort(this.getOwner());
        this.sensorNodeP2PInboundPort.publishPort();
        this.RegistrationOutboundPort = new RegistrationOutboundPort(this.getOwner());
        this.RegistrationOutboundPort.publishPort();

        this.neighbours = new HashSet<>();
        this.sensorData = new ArrayList<>();
        this.requestURIs = new HashSet<>();
        this.nodeInfoToP2POutboundPortMap = new ConcurrentHashMap<>();
        this.getOwner().doPortConnection(
                this.RegistrationOutboundPort.getPortURI(),
                this.registerInboundPortURI,
                RegistryConnector.class.getCanonicalName());
        super.initialise();
    }

    @Override
    public void finalise() throws Exception {
    	// MEC FAIS LE
//        if (this.requestingInboundPort.connected()) {
//            this.getOwner().doPortDisconnection(this.requestingInboundPort.getPortURI());
//        }
//        if (this.sensorNodeP2PInboundPort.connected()) {
//            this.getOwner().doPortDisconnection(this.sensorNodeP2PInboundPort.getPortURI());
//        }
//        if (this.RegistrationOutboundPort.connected()) {
//            this.getOwner().doPortDisconnection(this.RegistrationOutboundPort.getPortURI());
//        }
        super.finalise();
    }

    @Override
    public void uninstall() throws Exception {
        this.requestingInboundPort.unpublishPort();
        this.sensorNodeP2PInboundPort.unpublishPort();
        this.RegistrationOutboundPort.unpublishPort();
        super.uninstall();
    }

    public void ask4Connection(NodeInfoI newNeighbour)
            throws Exception {
        try {
            Direction direction = this.nodeInfo.nodePosition().directionFrom(newNeighbour.nodePosition()); // direction
            NodeInfoI neighbourInTheDirection = null;

            Iterator<NodeInfoI> it = new HashSet<>(neighbours).iterator(); // Copie pour éviter
                                                                           // ConcurrentModificationException
            while (it.hasNext()) {
                NodeInfoI neighbour = it.next();
                if (this.nodeInfo.nodePosition().directionFrom(neighbour.nodePosition()) == direction) {
                    neighbourInTheDirection = neighbour;
                    break;
                }
            }

            System.err.println("Neighbour in the direction: " + neighbourInTheDirection);

            if (neighbourInTheDirection == null) {
                SensorNodeP2POutboundPort newPort = new SensorNodeP2POutboundPort(
                        AbstractOutboundPort.generatePortURI(), this.getOwner());
                newPort.publishPort();
                this.nodeInfoToP2POutboundPortMap.put(newNeighbour, newPort);
                this.getOwner().doPortConnection(newPort.getPortURI(),
                        ((BCM4JavaEndPointDescriptorI) newNeighbour.p2pEndPointInfo()).getInboundPortURI(),
                        SensorNodeConnector.class.getCanonicalName());
                newPort.ask4Connection(this.nodeInfo);

                this.addNeighbour(newNeighbour);

                this.logMessage("ask4Connection: " + newNeighbour.nodeIdentifier() + " connected");
                this.printNeighbours();
            } else {
                // ------- compare the distance between the new neighbour and the neighbour in
                // the same direction
                double distanceNewNeighbour = this.nodeInfo.nodePosition().distance(newNeighbour.nodePosition());
                double distanceNeighbourInTheDirection = this.nodeInfo.nodePosition()
                        .distance(neighbourInTheDirection.nodePosition());
                if (distanceNewNeighbour < distanceNeighbourInTheDirection) {
                    SensorNodeP2POutboundPort newPort = new SensorNodeP2POutboundPort(
                            AbstractOutboundPort.generatePortURI(), this.getOwner());
                    newPort.publishPort();
                    this.nodeInfoToP2POutboundPortMap.put(newNeighbour, newPort);
                    this.getOwner().doPortConnection(newPort.getPortURI(),
                            ((BCM4JavaEndPointDescriptorI) newNeighbour.p2pEndPointInfo()).getInboundPortURI(),
                            SensorNodeConnector.class.getCanonicalName());
                    newPort.ask4Connection(this.nodeInfo);

                    this.addNeighbour(newNeighbour);

                    this.logMessage("ask4Connection: " + newNeighbour.nodeIdentifier() + " connected");
                    this.logMessage("Disconnecting neighbour in the same direction: "
                            + neighbourInTheDirection.nodeIdentifier());
                    this.ask4Disconnection(neighbourInTheDirection);
                    this.logMessage(
                            "Neighbours after disconnection: -->;" + neighbourInTheDirection.nodeIdentifier()
                                    + " disconnected");
                    this.printNeighbours();
                } else {
                    this.logMessage("ask4Connection: " + newNeighbour.nodeIdentifier() + " not connected");
                    this.logMessage("Distance between " + newNeighbour.nodeIdentifier() + " and "
                            + neighbourInTheDirection.nodeIdentifier() + " is less than the distance between "
                            + newNeighbour.nodeIdentifier() + " and " + neighbourInTheDirection.nodeIdentifier());
                }
            }
        } catch (Exception e) {
            throw new Exception("Error in ask4Connection" + e.getMessage());
        }
    }

    public void ask4Disconnection(NodeInfoI neighbour) {
        try {
            SensorNodeP2POutboundPort nodePort = this.nodeInfoToP2POutboundPortMap.get(neighbour);
            if (nodePort == null) {
                this.logMessage("ask4Disconnection: " + neighbour.nodeIdentifier() + " not connected");
                return;
            }

            Direction directionOfNeighbour = this.nodeInfo.nodePosition().directionFrom(neighbour.nodePosition());

            this.removeNeighbour(neighbour);

            this.nodeInfoToP2POutboundPortMap.remove(neighbour);
            this.getOwner().doPortDisconnection(nodePort.getPortURI());
            nodePort.unpublishPort();

            // ----- Find new in the same direction if possible -----
            NodeInfoI newNeighbour = this.RegistrationOutboundPort.findNewNeighbour(this.nodeInfo,
                    directionOfNeighbour);
            if (newNeighbour != null && newNeighbour.nodeIdentifier() != neighbour.nodeIdentifier()) {
                SensorNodeP2POutboundPort newPort = new SensorNodeP2POutboundPort(
                        AbstractOutboundPort.generatePortURI(), this.getOwner());
                newPort.publishPort();
                this.nodeInfoToP2POutboundPortMap.put(newNeighbour, newPort);
                this.getOwner().doPortConnection(newPort.getPortURI(),
                        ((BCM4JavaEndPointDescriptorI) newNeighbour.p2pEndPointInfo()).getInboundPortURI(),
                        SensorNodeConnector.class.getCanonicalName());
                newPort.ask4Connection(this.nodeInfo);

                this.addNeighbour(newNeighbour);

                this.logMessage("ask4Disconnection: " + neighbour.nodeIdentifier() + " disconnected");
                this.logMessage("Found new neighbour in direction " + directionOfNeighbour + " : "
                        + newNeighbour.nodeIdentifier());
            } else {
                this.logMessage("ask4Disconnection: " + neighbour.nodeIdentifier() + " disconnected");
                this.logMessage("No new neighbour found in direction " + directionOfNeighbour);
            }

            this.logMessage("Neighbours after disconnection: -->@;" + neighbour.nodeIdentifier() + " disconnected");
            this.logMessage("<Neighbours after disconnection: -->@;" + printNeighbours());

        } catch (Exception e) {
            System.err.println("Error in ask4Disconnection " + e.getMessage());
            e.printStackTrace();
        }
        // }
    }

    public void addNeighbour(NodeInfoI neighbour) {
        neigboursLock.writeLock().lock();
        try {
            neighbours.add(neighbour);
        } finally {
            neigboursLock.writeLock().unlock();
        }
    }

    public void removeNeighbour(NodeInfoI neighbour) {
        neigboursLock.writeLock().lock();
        try {
            neighbours.remove(neighbour);
        } finally {
            neigboursLock.writeLock().unlock();
        }
    }

    public String printNeighbours() {
        StringBuilder sb = new StringBuilder();
        sb.append("Neighbours of " + this.nodeInfo.nodeIdentifier() + " : ");
        for (NodeInfoI neighbour : neighbours) {
            sb.append(neighbour.nodeIdentifier() + ", ");
        }
        return sb.toString();
    }

    public void connect2Neighbours() throws ComponentStartException {
        try {
            Iterator<NodeInfoI> it = new HashSet<>(neighbours).iterator(); // Copie pour éviter
                                                                           // ConcurrentModificationException
            while (it.hasNext()) {
                NodeInfoI neighbour = it.next();
                SensorNodeP2POutboundPort p2poutboundP = new SensorNodeP2POutboundPort(
                        AbstractOutboundPort.generatePortURI(),
                        this.getOwner());
                p2poutboundP.publishPort();
                this.getOwner().doPortConnection(p2poutboundP.getPortURI(),
                        ((BCM4JavaEndPointDescriptorI) neighbour.p2pEndPointInfo()).getInboundPortURI(),
                        SensorNodeConnector.class.getCanonicalName());
                this.nodeInfoToP2POutboundPortMap.put(neighbour, p2poutboundP);
                System.err.println("Connecting to neighbour: " + neighbour.nodeIdentifier());
                logMessage("Connecting to neighbour: " + neighbour.nodeIdentifier());
                p2poutboundP.ask4Connection(this.nodeInfo);
            }
        } catch (

        Exception e) {
            throw new ComponentStartException(e);
        }
    }

    public void registerNode() throws Exception {
        try {
            // ----------------- REGISTRATION -----------------
            this.neighbours = RegistrationOutboundPort.register(nodeInfo);
            ((ProcessingNodeIMPL) this.processingNode).setNeighbors(neighbours);
            this.getOwner().logMessage(this.printNeighbours());
            this.getOwner().logMessage("Registration Success: "
                    + RegistrationOutboundPort.registered(nodeInfo.nodeIdentifier()) + "");
            this.getOwner().logMessage("Connecting to all the neighbours received from the registry at Time : "
                    + Instant.now() + " .....................");
            this.connect2Neighbours();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.getOwner().logMessage("Node Component successfully executed: " +
                this.nodeInfo.nodeIdentifier());
    }
}
