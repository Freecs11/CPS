package bcm.plugin;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import bcm.connectors.LookUpRegistryConnector;
import bcm.connectors.RequestingConnector;
import bcm.ports.LookupOutboundPort;
import bcm.ports.RequestResultInboundPort;
import bcm.ports.RequestingOutboundPort;
import fr.sorbonne_u.components.AbstractComponent.AbstractTask;
import fr.sorbonne_u.components.AbstractPlugin;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestResultCI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.QueryI;
import implementation.ConnectionInfoImpl;
import implementation.EndPointDescIMPL;
import implementation.QueryResultIMPL;
import implementation.RequestIMPL;

public class ClientPlugin
                extends AbstractPlugin {

        protected RequestResultInboundPort clientRequestResultInboundPort;

        protected LookupOutboundPort LookupOutboundPort;
        protected String registryInboundPortURI;

        public final String clientIdentifer;

        private ConcurrentHashMap<String, List<QueryResultI>> resultsMap;

        public ClientPlugin(String registryInboundPortURI, String clientIdentifer) {
                this.registryInboundPortURI = registryInboundPortURI;
                this.clientIdentifer = clientIdentifer;
                this.resultsMap = new ConcurrentHashMap<>();
        }

        @Override
        public void installOn(ComponentI owner) throws Exception {
                super.installOn(owner);
                this.addRequiredInterface(LookupCI.class);
                this.addRequiredInterface(RequestingCI.class);
                this.addOfferedInterface(RequestResultCI.class);
        }

        @Override
        public void initialise() throws Exception {
                // ---------Init the ports---------
                this.LookupOutboundPort = new LookupOutboundPort(
                                AbstractOutboundPort.generatePortURI(),
                                this.getOwner());
                this.LookupOutboundPort.publishPort();

                this.getOwner().doPortConnection(
                                this.LookupOutboundPort.getPortURI(),
                                this.registryInboundPortURI,
                                LookUpRegistryConnector.class.getCanonicalName());

                this.clientRequestResultInboundPort = new RequestResultInboundPort(
                                AbstractOutboundPort.generatePortURI(),
                                this.getOwner());
                this.clientRequestResultInboundPort.publishPort();

                super.initialise();
        }

        @Override
        public void finalise() throws Exception {
                if (this.LookupOutboundPort.connected())
                        this.getOwner().doPortDisconnection(this.LookupOutboundPort.getPortURI());
                if (this.LookupOutboundPort.isPublished())
                        this.LookupOutboundPort.unpublishPort();
                super.finalise();
        }

        @Override
        public void uninstall() throws Exception {
                if (this.clientRequestResultInboundPort.isPublished())
                        this.clientRequestResultInboundPort.unpublishPort();

                super.uninstall();
        }

        // --------- Methodes du plugin ---------

        /**
         * Execute a sync request to a node with a certain delay to launch the request
         * 
         * @param request the request to be sent
         * @param nodeId  the id of the node to send the request to
         * @param delay   the delay to wait before sending the request
         */
        public void executeSyncRequest(String requestURI, QueryI query, String nodeId, long delay) {
                this.getOwner().scheduleTask(
                                new AbstractTask() {
                                        @Override
                                        public void run() {
                                                try {
                                                        ConnectionInfoI nodeInfo = ClientPlugin.this.LookupOutboundPort
                                                                        .findByIdentifier(nodeId);
                                                        RequestingOutboundPort clientRequestingOutboundPort = new RequestingOutboundPort(
                                                                        AbstractOutboundPort.generatePortURI(),
                                                                        ClientPlugin.this.getOwner());
                                                        clientRequestingOutboundPort.publishPort();

                                                        RequestI request = new RequestIMPL(requestURI, query, false,
                                                                        new ConnectionInfoImpl(
                                                                                        ClientPlugin.this.clientIdentifer,
                                                                                        new EndPointDescIMPL(
                                                                                                        clientRequestingOutboundPort
                                                                                                                        .getPortURI(),
                                                                                                        RequestingCI.class)));

                                                        ClientPlugin.this.getOwner().doPortConnection(
                                                                        clientRequestingOutboundPort.getPortURI(),
                                                                        ((EndPointDescIMPL) nodeInfo.endPointInfo())
                                                                                        .getInboundPortURI(),
                                                                        RequestingConnector.class.getCanonicalName());
                                                        QueryResultI res = clientRequestingOutboundPort
                                                                        .execute(request);
                                                        if (res.isGatherRequest()) {
                                                                ClientPlugin.this.getOwner()
                                                                                .logMessage("Gathered size : " + res
                                                                                                .gatheredSensorsValues()
                                                                                                .size()
                                                                                                + " for request with URI "
                                                                                                + request.requestURI());

                                                        } else if (res.isBooleanRequest()) {
                                                                ClientPlugin.this.getOwner()
                                                                                .logMessage("Floading size : " + res
                                                                                                .positiveSensorNodes()
                                                                                                .size());
                                                        }
                                                        ClientPlugin.this.getOwner()
                                                                        .logMessage("SYNC Query result, sent at : "
                                                                                        + Instant.now()
                                                                                        + " , URI : "
                                                                                        + request.requestURI()
                                                                                        + " : "
                                                                                        + res.toString());
                                                        ClientPlugin.this.getOwner().doPortDisconnection(
                                                                        clientRequestingOutboundPort.getPortURI());
                                                        clientRequestingOutboundPort.unpublishPort();
                                                        clientRequestingOutboundPort.destroyPort();
                                                } catch (Exception e) {
                                                        e.printStackTrace();
                                                }
                                        }
                                }, delay, TimeUnit.NANOSECONDS);
        }

        /**
         * Execute an async request to a node with a certain delay to launch the request
         * and a certain timeout to wait for the results to be gathered and combined
         * 
         * @param request the request to be sent
         * @param nodeId  the id of the node to send the request to
         * @param delay   the delay to wait before sending the request
         * @throws Exception
         */
        public void executeAsyncRequest(String requestURI, QueryI query, String nodeId, long delay, long asyncTimeout)
                        throws Exception {

                this.getOwner().scheduleTask(
                                new AbstractTask() {
                                        @Override
                                        public void run() {
                                                try {
                                                        // implementation of
                                                        // connectionInfo
                                                        // System.err.println("NodeInfo: " + nodeInfo.nodeIdentifier());

                                                        ConnectionInfoI nodeInfo = ClientPlugin.this.LookupOutboundPort
                                                                        .findByIdentifier(nodeId);
                                                        RequestingOutboundPort clientRequestingOutboundPort = new RequestingOutboundPort(
                                                                        AbstractOutboundPort.generatePortURI(),
                                                                        ClientPlugin.this.getOwner());
                                                        clientRequestingOutboundPort.publishPort();

                                                        RequestI request = new RequestIMPL(requestURI, query, true,
                                                                        new ConnectionInfoImpl(
                                                                                        ClientPlugin.this.clientIdentifer,
                                                                                        new EndPointDescIMPL(
                                                                                                        ClientPlugin.this.clientRequestResultInboundPort
                                                                                                                        .getPortURI(),
                                                                                                        RequestResultCI.class)));

                                                        ClientPlugin.this.getOwner().doPortConnection(
                                                                        clientRequestingOutboundPort.getPortURI(),
                                                                        ((EndPointDescIMPL) nodeInfo.endPointInfo())
                                                                                        .getInboundPortURI(),
                                                                        RequestingConnector.class.getCanonicalName());

                                                        ClientPlugin.this.resultsMap.put(request.requestURI(),
                                                                        new ArrayList<>());
                                                        clientRequestingOutboundPort.executeAsync(request);
                                                        ClientPlugin.this.getOwner()
                                                                        .logMessage("Async request sent to node "
                                                                                        + nodeId + " with URI "
                                                                                        + request.requestURI()
                                                                                        + " at " + Instant.now());

                                                        ClientPlugin.this.getOwner().doPortDisconnection(
                                                                        clientRequestingOutboundPort.getPortURI());
                                                        clientRequestingOutboundPort.unpublishPort();
                                                        clientRequestingOutboundPort.destroyPort();
                                                } catch (Exception e) {
                                                        e.printStackTrace();
                                                }
                                        }
                                }, delay, TimeUnit.NANOSECONDS);

                // after a certain delay the client component will combine the results he got
                // from the nodes and print them and then delete the query and don't wait for
                // more results
                this.getOwner().scheduleTask(
                                new AbstractTask() {
                                        @Override
                                        public void run() {
                                                try {
                                                        List<QueryResultI> results = ClientPlugin.this.resultsMap
                                                                        .get(requestURI);
                                                        if (results == null || results.isEmpty()) {
                                                                return;
                                                        }
                                                        QueryResultI result = results.get(0);
                                                        results.remove(0);
                                                        if (!results.isEmpty()) {
                                                                for (QueryResultI res : results) {
                                                                        ((QueryResultIMPL) result).update(res);
                                                                }
                                                                if (result.isGatherRequest()) {
                                                                        ClientPlugin.this.getOwner()
                                                                                        .logMessage("Gathered size : "
                                                                                                        + result.gatheredSensorsValues()
                                                                                                                        .size());
                                                                } else if (result.isBooleanRequest()) {
                                                                        ClientPlugin.this.getOwner()
                                                                                        .logMessage("Floading size : "
                                                                                                        + result.positiveSensorNodes()
                                                                                                                        .size());
                                                                }
                                                                ClientPlugin.this.getOwner().logMessage(
                                                                                "ASYNC Final Query result , received at : "
                                                                                                + Instant.now()
                                                                                                + " , URI : "
                                                                                                + requestURI
                                                                                                + " : "
                                                                                                + result.toString());
                                                                ClientPlugin.this.resultsMap
                                                                                .remove(requestURI);
                                                        }
                                                } catch (Exception e) {
                                                        e.printStackTrace();
                                                }
                                        }
                                }, delay + asyncTimeout, TimeUnit.NANOSECONDS);
        }

        public void acceptRequestResult(String requestURI, QueryResultI result) throws Exception {
                this.getOwner().logMessage(
                                "Received result for request with URI " + requestURI + " at " + Instant.now());
                if (this.resultsMap.containsKey(requestURI)) {
                        // we add the result to the list of results for this request URI
                        this.resultsMap.get(requestURI).add(result);
                } else {
                        System.out.println("No request with URI " + requestURI + " found.");
                }
        }

}
