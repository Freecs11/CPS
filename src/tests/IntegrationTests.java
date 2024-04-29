package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

// Cette classe permet d'effectuer des tests d'intégration avec JUnit
public class IntegrationTests {

        private Map<String, Map<String, Map<String, Double>>> clientComponentResult = new HashMap<>();
        private Map<String, Map<String, Double>> sensorData = new HashMap<>();

        @Before
        public void setUp() throws Exception {
                clientComponentResult = ParserRequest.parseFile("logRegistry.log");
                initializeSensorData();
        }

        private void initializeSensorData() {
                // urinode8
                Map<String, Double> urinode8 = new HashMap<>();
                urinode8.put("temperature", 91.0);
                urinode8.put("humidity", 92.0);
                sensorData.put("urinode8", urinode8);

                // urinode10
                Map<String, Double> urinode10 = new HashMap<>();
                urinode10.put("temperature", 61.0);
                urinode10.put("humidity", 62.0);
                sensorData.put("urinode10", urinode10);

                // urinode7
                Map<String, Double> urinode7 = new HashMap<>();
                urinode7.put("temperature", 41.0);
                urinode7.put("humidity", 42.0);
                sensorData.put("urinode7", urinode7);

                // urinode4
                Map<String, Double> urinode4 = new HashMap<>();
                urinode4.put("temperature", 21.0);
                urinode4.put("humidity", 22.0);
                sensorData.put("urinode4", urinode4);

                // urinode5
                Map<String, Double> urinode5 = new HashMap<>();
                urinode5.put("temperature", 71.0);
                urinode5.put("humidity", 72.0);
                sensorData.put("urinode5", urinode5);

                // urinode2
                Map<String, Double> urinode2 = new HashMap<>();
                urinode2.put("temperature", 51.0);
                urinode2.put("humidity", 52.0);
                sensorData.put("urinode2", urinode2);

                // urinode13
                Map<String, Double> urinode13 = new HashMap<>();
                urinode13.put("temperature", 81.0);
                urinode13.put("humidity", 82.0);
                sensorData.put("urinode13", urinode13);

                // urinode3
                Map<String, Double> urinode3 = new HashMap<>();
                urinode3.put("temperature", 101.0);
                urinode3.put("humidity", 102.0);
                sensorData.put("urinode3", urinode3);

                // urinode12
                Map<String, Double> urinode12 = new HashMap<>();
                urinode12.put("temperature", 31.0);
                urinode12.put("humidity", 32.0);
                sensorData.put("urinode12", urinode12);

                // urinode9
                Map<String, Double> urinode9 = new HashMap<>();
                urinode9.put("temperature", 11.0);
                urinode9.put("humidity", 12.0);
                sensorData.put("urinode9", urinode9);

                // urinode11
                Map<String, Double> urinode11 = new HashMap<>();
                urinode11.put("temperature", 111.0);
                urinode11.put("humidity", 112.0);
                sensorData.put("urinode11", urinode11);

                // urinode6
                Map<String, Double> urinode6 = new HashMap<>();
                urinode6.put("temperature", 121.0);
                urinode6.put("humidity", 122.0);
                sensorData.put("urinode6", urinode6);

                // urinode1
                Map<String, Double> urinode1 = new HashMap<>();
                urinode1.put("temperature", 1.0);
                urinode1.put("humidity", 2.0);
                sensorData.put("urinode1", urinode1);
        }

        @Test
        public void testNbRequests() {
                assertEquals("The number of requests should be 6", 6, clientComponentResult.size());
        }

        @Test
        public void testReq1() {
                Map<String, Map<String, Double>> req1 = clientComponentResult.get("req1");
                assertNotNull("Request 'req1' should not be null", req1);
                assertEquals("Number of nodes in 'req1' should be 13", 13, req1.size());

                testNode(req1, "urinode1", sensorData.get("urinode1").get("temperature"),
                                sensorData.get("urinode1").get("humidity"));
                testNode(req1, "urinode2", sensorData.get("urinode2").get("temperature"),
                                sensorData.get("urinode2").get("humidity"));
                testNode(req1, "urinode3", sensorData.get("urinode3").get("temperature"),
                                sensorData.get("urinode3").get("humidity"));
                testNode(req1, "urinode4", sensorData.get("urinode4").get("temperature"),
                                sensorData.get("urinode4").get("humidity"));
                testNode(req1, "urinode5", sensorData.get("urinode5").get("temperature"),
                                sensorData.get("urinode5").get("humidity"));
                testNode(req1, "urinode6", sensorData.get("urinode6").get("temperature"),
                                sensorData.get("urinode6").get("humidity"));
                testNode(req1, "urinode7", sensorData.get("urinode7").get("temperature"),
                                sensorData.get("urinode7").get("humidity"));
                testNode(req1, "urinode8", sensorData.get("urinode8").get("temperature"),
                                sensorData.get("urinode8").get("humidity"));
                testNode(req1, "urinode9", sensorData.get("urinode9").get("temperature"),
                                sensorData.get("urinode9").get("humidity"));
                testNode(req1, "urinode10", sensorData.get("urinode10").get("temperature"),
                                sensorData.get("urinode10").get("humidity"));
                testNode(req1, "urinode11", sensorData.get("urinode11").get("temperature"),
                                sensorData.get("urinode11").get("humidity"));
                testNode(req1, "urinode12", sensorData.get("urinode12").get("temperature"),
                                sensorData.get("urinode12").get("humidity"));
                testNode(req1, "urinode13", sensorData.get("urinode13").get("temperature"),
                                sensorData.get("urinode13").get("humidity"));

        }

        @Test
        public void testReq2() {
                Map<String, Map<String, Double>> req2 = clientComponentResult.get("req2");
                assertNotNull("Request 'req2' should not be null", req2);

                testNode(req2, "urinode1", sensorData.get("urinode1").get("temperature"),
                                sensorData.get("urinode1").get("humidity"));
                testNode(req2, "urinode2", sensorData.get("urinode2").get("temperature"),
                                sensorData.get("urinode2").get("humidity"));
                testNode(req2, "urinode3", sensorData.get("urinode3").get("temperature"),
                                sensorData.get("urinode3").get("humidity"));
                testNode(req2, "urinode4", sensorData.get("urinode4").get("temperature"),
                                sensorData.get("urinode4").get("humidity"));
                testNode(req2, "urinode5", sensorData.get("urinode5").get("temperature"),
                                sensorData.get("urinode5").get("humidity"));
                testNode(req2, "urinode6", sensorData.get("urinode6").get("temperature"),
                                sensorData.get("urinode6").get("humidity"));
                testNode(req2, "urinode7", sensorData.get("urinode7").get("temperature"),
                                sensorData.get("urinode7").get("humidity"));
                testNode(req2, "urinode8", sensorData.get("urinode8").get("temperature"),
                                sensorData.get("urinode8").get("humidity"));
                testNode(req2, "urinode9", sensorData.get("urinode9").get("temperature"),
                                sensorData.get("urinode9").get("humidity"));
                testNode(req2, "urinode10", sensorData.get("urinode10").get("temperature"),
                                sensorData.get("urinode10").get("humidity"));
                testNode(req2, "urinode11", sensorData.get("urinode11").get("temperature"),
                                sensorData.get("urinode11").get("humidity"));
                testNode(req2, "urinode12", sensorData.get("urinode12").get("temperature"),
                                sensorData.get("urinode12").get("humidity"));
                testNode(req2, "urinode13", sensorData.get("urinode13").get("temperature"),
                                sensorData.get("urinode13").get("humidity"));
        }

        @Test
        public void testReq3() {
                Map<String, Map<String, Double>> req3 = clientComponentResult.get("req3");
                assertNotNull("Request 'req3' should not be null", req3);

                testNode(req3, "urinode2", sensorData.get("urinode2").get("temperature"),
                                sensorData.get("urinode2").get("humidity"));
                testNode(req3, "urinode3", sensorData.get("urinode3").get("temperature"),
                                sensorData.get("urinode3").get("humidity"));
                testNode(req3, "urinode4", sensorData.get("urinode4").get("temperature"),
                                sensorData.get("urinode4").get("humidity"));
                testNode(req3, "urinode5", sensorData.get("urinode5").get("temperature"),
                                sensorData.get("urinode5").get("humidity"));
                testNode(req3, "urinode7", sensorData.get("urinode7").get("temperature"),
                                sensorData.get("urinode7").get("humidity"));
                testNode(req3, "urinode8", sensorData.get("urinode8").get("temperature"),
                                sensorData.get("urinode8").get("humidity"));
                testNode(req3, "urinode10", sensorData.get("urinode10").get("temperature"),
                                sensorData.get("urinode10").get("humidity"));
                testNode(req3, "urinode13", sensorData.get("urinode13").get("temperature"),
                                sensorData.get("urinode13").get("humidity"));
        }

        @Test
        public void testReq4() {
                Map<String, Map<String, Double>> req4 = clientComponentResult.get("req4");
                assertNotNull("Request 'req4' should not be null", req4);

                testNode(req4, "urinode2", sensorData.get("urinode2").get("temperature"),
                                sensorData.get("urinode2").get("humidity"));
                testNode(req4, "urinode3", sensorData.get("urinode3").get("temperature"),
                                sensorData.get("urinode3").get("humidity"));
                testNode(req4, "urinode4", sensorData.get("urinode4").get("temperature"),
                                sensorData.get("urinode4").get("humidity"));
                testNode(req4, "urinode5", sensorData.get("urinode5").get("temperature"),
                                sensorData.get("urinode5").get("humidity"));
                testNode(req4, "urinode7", sensorData.get("urinode7").get("temperature"),
                                sensorData.get("urinode7").get("humidity"));
                testNode(req4, "urinode8", sensorData.get("urinode8").get("temperature"),
                                sensorData.get("urinode8").get("humidity"));
                testNode(req4, "urinode10", sensorData.get("urinode10").get("temperature"),
                                sensorData.get("urinode10").get("humidity"));
                testNode(req4, "urinode13", sensorData.get("urinode13").get("temperature"),
                                sensorData.get("urinode13").get("humidity"));
        }

        private void testNode(Map<String, Map<String, Double>> request, String node, double tempExp, double humidExp) {
                Map<String, Double> sensors = request.get(node);
                assertNotNull("Node " + node + " should not be null", sensors);
                assertEquals("Temperature for " + node + " should be " + tempExp, tempExp, sensors.get("temperature"),
                                0.01);
                assertEquals("Humidity for " + node + " should be " + humidExp, humidExp, sensors.get("humidity"),
                                0.01);
        }
}
