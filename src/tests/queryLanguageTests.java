package tests;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.text.Position;

import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;
import implementation.PositionIMPL;
import implementation.SensorDataIMPL;
import implementation.request.ExecutionStateIMPL;
import implementation.request.ProcessingNodeIMPL;

/**
 * Tests for the query language used in the project.
 */
public class queryLanguageTests {

    private ExecutionStateI executionStateTests1;
    private ExecutionStateI executionStateTests2;

    private PositionI position1;
    private PositionI position2;

    private void initialisation() throws Exception {
        ProcessingNodeIMPL pn1 = new ProcessingNodeIMPL();
        String nodeId1 = "node1";
        pn1.setNodeId(nodeId1);
        position1 = new PositionIMPL(1.0, 1.0);
        pn1.setPostion(position1);
        Map<String, SensorDataI> sensorDataMap = new HashMap<String, SensorDataI>();
        pn1.setSensorDataMap(sensorDataMap);
        pn1.addSensorData("capteur_fumee", 12.5);
        pn1.addSensorData("capteur_temperature", 20.0);
        pn1.addSensorData("capteur_humidite", 50.0);
        pn1.addSensorData("capteur_luminosite", 100.0);
        pn1.addSensorData("capteur_presence", 1);

        executionStateTests1 = new ExecutionStateIMPL(pn1);

        ProcessingNodeIMPL pn2 = new ProcessingNodeIMPL();
        String nodeId2 = "node2";
        pn2.setNodeId(nodeId2);
        position2 = new PositionIMPL(2.0, 2.0);
        pn2.setPostion(position2);
        Map<String, SensorDataI> sensorDataMap2 = new HashMap<String, SensorDataI>();
        pn2.setSensorDataMap(sensorDataMap2);
        pn2.addSensorData("capteur_fumee", 12.5);
        pn2.addSensorData("capteur_temperature", 20.0);
        pn2.addSensorData("capteur_humidite", 50.0);
        pn2.addSensorData("capteur_luminosite", 100.0);
        pn2.addSensorData("capteur_presence", 1);

        executionStateTests2 = new ExecutionStateIMPL(pn2);
    }

    /// tests every query class
}
