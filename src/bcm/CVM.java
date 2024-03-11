package bcm;

import java.lang.reflect.Array;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import bcm.components.ClientComponent;
import bcm.components.NodeComponent;
import bcm.components.RegistryComponent;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.helpers.CVMDebugModes;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import javafx.scene.Node;
import utils.NodeComponentInfo;

public class CVM extends AbstractCVM {
        public static final String CLOCK_URI = "CLOCK-SERVER";
        protected static final long TIME_TO_START = 3000L;
        protected static final long unixEpochStartTimeInNanos = TimeUnit.MILLISECONDS.toNanos(
                        System.currentTimeMillis() + TIME_TO_START);
        public static final Instant CLOCK_START_INSTANT = Instant.parse("2024-01-31T09:00:00.00Z");
        protected static final double accelerationFactor = 1.0;

        /** URI of the provider component (convenience). */
        protected static final String NODE_COMPONENT_URI = "node-URI";
        protected static final String NODE2_COMPONENT_URI = "node2-URI";
        protected static final String NODE3_COMPONENT_URI = "node3-URI";
        /** URI of the consumer component (convenience). */
        protected static final String CLIENT_COMPONENT_URI = "client-URI";
        protected static final String REGISTER_COMPONENT_URI = "register-URI";
        // /** URI of the provider outbound port (simplifies the connection). */
        // protected static final String URIGetterOutboundPortURI = "oport";
        // /** URI of the consumer inbound port (simplifies the connection). */
        // protected static final String URIProviderInboundPortURI = "iport";

        protected static final String REGISTER_IN_BOUND_PORT_URI = "register-inbound-port";
        protected static final String LOOKUP_IN_BOUND_PORT_URI = "lookup_inbound-port";
        protected static final String CLIENTS_OUT_BOUND_PORT_URI = "client-outbound-port";
        protected static final String NODE_TO_REG_OUT_BOUND_PORT_URI = "node2reg-outbound-port";
        protected static final String NODE_IN_BOUND_PORT_URI = "node-inbound-port";
        protected static final String NODE2_TO_REG_OUT_BOUND_PORT_URI = "node2_2reg-outbound-port";
        protected static final String NODE2_IN_BOUND_PORT_URI = "node2-inbound-port";

        protected static final String NODE3_TO_REG_OUT_BOUND_PORT_URI = "node3_reg-outbound-port";
        protected static final String NODE3_IN_BOUND_PORT_URI = "node3-inbound-port";

        public CVM() throws Exception {
                super();
        }

        protected String clockURI;
        /**
         * Reference to the provider component to share between deploy
         * and shutdown.
         */
        // protected String uriNodeURI;
        // protected String uriNode2URI;
        // protected String uriNode3URI;
        // protected String uriNode4URI;
        // protected String uriNode5URI;
        // protected String uriNode6URI;
        /**
         * Reference to the consumer component to share between deploy
         * and shutdown.
         */
        protected String uriClientURI;

        protected String uriRegisterURI;

        public static Set<NodeComponentInfo> buildMap (int nbNode){
                Set<NodeComponentInfo> result = new HashSet<NodeComponentInfo>();
                Stack<NodeComponentInfo> stack = new Stack<NodeComponentInfo>();

                double x = nbNode,y = nbNode;
                int i = 0;
                NodeComponentInfo firstNode = new NodeComponentInfo("node"+i, x, y, 40.0);
                stack.add(firstNode);
                result.add(firstNode);
                nbNode --;
                i++;

                while ( nbNode > 0) {
                        NodeComponentInfo nodeInit = stack.pop();
                        NodeComponentInfo node =new  NodeComponentInfo("node"+i,nodeInit.getX()+1, nodeInit.getY()-1, 2.0);
                        NodeComponentInfo node2 =new  NodeComponentInfo("node"+(i+1),nodeInit.getX()-1, nodeInit.getY()-1, 2.0);
                        NodeComponentInfo node3 =new  NodeComponentInfo("node"+(i+2),nodeInit.getX()+1, nodeInit.getY()+1, 2.0);
                        NodeComponentInfo node4 =new  NodeComponentInfo("node"+(i+3),nodeInit.getX()-1, nodeInit.getY()+1, 2.0);
                        int nodeSuccess = 0;
                        
                        if (!result.contains(node)){
                                nbNode --;
                                node.setName("node"+(i+nodeSuccess));
                                result.add(node);
                                stack.add(node);
                                nodeSuccess++;
                        }
                        if (!result.contains(node2)){
                                nbNode --;  
                                node2.setName("node"+(i+nodeSuccess));
                                result.add(node2);
                                stack.add(node2);
                                nodeSuccess++;
                        }
                        if (!result.contains(node3)){
                                nbNode --;   
                                node3.setName("node"+(i+nodeSuccess));
                                result.add(node3);
                                stack.add(node3);
                                nodeSuccess++;
                        }
                        if (!result.contains(node4)){
                                nbNode --; 
                                node4.setName("node"+(i+nodeSuccess));
                                result.add(node4);
                                stack.add(node4);
                                nodeSuccess++;
                        }
                        i+=nodeSuccess;
                }


                return result;
        }

        @Override
        public void deploy() throws Exception {
                assert !this.deploymentDone();

                // ---------------------------------------------------------------------
                // Configuration phase
                // ---------------------------------------------------------------------

                // debugging mode configuration; comment and uncomment the line to see
                // the difference
                AbstractCVM.DEBUG_MODE.add(CVMDebugModes.LIFE_CYCLE);
                AbstractCVM.DEBUG_MODE.add(CVMDebugModes.INTERFACES);
                AbstractCVM.DEBUG_MODE.add(CVMDebugModes.PORTS);
                AbstractCVM.DEBUG_MODE.add(CVMDebugModes.CONNECTING);
                AbstractCVM.DEBUG_MODE.add(CVMDebugModes.CALLING);
                AbstractCVM.DEBUG_MODE.add(CVMDebugModes.EXECUTOR_SERVICES);
                this.clockURI = AbstractComponent.createComponent(ClocksServer.class.getCanonicalName(),
                                new Object[] {
                                                CLOCK_URI,
                                                unixEpochStartTimeInNanos,
                                                CLOCK_START_INSTANT,
                                                accelerationFactor
                                });

                // ---------------------------------------------------------------------
                // Creation phase
                // ---------------------------------------------------------------------
                this.uriRegisterURI = AbstractComponent.createComponent(RegistryComponent.class.getCanonicalName(),
                                new Object[] {
                                                REGISTER_COMPONENT_URI,
                                                1, 1,
                                                LOOKUP_IN_BOUND_PORT_URI,
                                                REGISTER_IN_BOUND_PORT_URI });
                // create the node component

               

                

                Map<String,List<Double>> data = new HashMap<String , List<Double>>();
                data.put("node1", new ArrayList<>(Arrays.asList(1.0, 5.0, 45.0)));
                data.put("node2", new ArrayList<>(Arrays.asList(2.0, 4.0, 45.0)));
                data.put("node3", new ArrayList<>(Arrays.asList(3.0, 3.0, 40.0)));
                data.put("node4", new ArrayList<>(Arrays.asList(1.0, 3.0, 40.0)));
                data.put("node5", new ArrayList<>(Arrays.asList(1.0, 1.0, 40.0)));
                data.put("node6", new ArrayList<>(Arrays.asList(4.0, 4.0, 40.0)));


                Random random = new Random();
                        // Définir l'intervalle (par exemple, entre 1 et 10)
                        int borneInferieure = 1;
                        int borneSuperieure = 11; // Exclusif, donc la plage sera de 1 à 10 inclus

                for (Map.Entry<String, List<Double>> entry : data.entrySet()) {
                        String key = entry.getKey();
                        List<Double> value = entry.getValue();
                        // Génération d'un entier aléatoire dans l'intervalle spécifié
                        int nombreAleatoire = random.nextInt(borneSuperieure - borneInferieure) + borneInferieure;
     
                         String uri = AbstractComponent.createComponent(NodeComponent.class.getCanonicalName(),
                                        new Object[] { "uri"+key, key, value.get(0), value.get(1), value.get(2),
                                                        REGISTER_IN_BOUND_PORT_URI,
                                                        RegistryComponent.REG_START_INSTANT.plusSeconds(nombreAleatoire)});
                        assert this.isDeployedComponent(uri);
                        this.toggleTracing(uri);
                        this.toggleLogging(uri);
                        
                }
                
              

                // create the client component
                this.uriClientURI = AbstractComponent.createComponent(ClientComponent.class.getCanonicalName(),
                                new Object[] {
                                                CLIENT_COMPONENT_URI,
                                                LOOKUP_IN_BOUND_PORT_URI,
                                                RegistryComponent.REG_START_INSTANT.plusSeconds(10)

                                }); // to be changed
                // ---------------------------------------------------------------------
                // Deployment phase
                // ---------------------------------------------------------------------
                assert this.isDeployedComponent(this.uriClientURI);
                this.toggleTracing(this.uriClientURI);
                this.toggleLogging(this.uriClientURI);

                assert this.isDeployedComponent(this.uriRegisterURI);
                this.toggleTracing(this.uriRegisterURI);
                this.toggleLogging(this.uriRegisterURI);

                // ---------------------------------------------------------------------
                // Deployment done
                // ---------------------------------------------------------------------
                super.deploy();
        }

        @Override
        public void finalise() throws Exception {
                super.finalise();
                // System.out.println("CVM finalise");
        }

        @Override
        public void shutdown() throws Exception {
                assert this.allFinalised();

                super.shutdown();
                // System.out.println("CVM shutdown");
        }

        public static void main(String[] args) {
                // try {
                //         CVM cvm = new CVM();
                //         cvm.startStandardLifeCycle(1500000L);
                //         Thread.sleep(10000L);
                //         System.exit(0);
                // } catch (Exception e) {
                //         throw new RuntimeException(e);
                // }
                Set<NodeComponentInfo> result = CVM.buildMap(55);
                System.out.println(result.size());
                System.out.println(result);
        }

}
