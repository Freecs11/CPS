package utils;

import java.util.ArrayList;
import java.util.List;

import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import implementation.SensorDataIMPL;

public class SensorDataGenerator {

    public static List<List<SensorDataI>> generateSensorData(int numberOfNodes) {
        List<String> sensorDataList = new ArrayList<>();
        sensorDataList.add("Temperature");
        sensorDataList.add("Humidity");
        sensorDataList.add("Pressure");
        sensorDataList.add("WindSpeed");
        List<List<SensorDataI>> sensorData = new ArrayList<>();
        for (int i = 0; i < numberOfNodes; i++) {
            List<SensorDataI> row = new ArrayList<>();
            int sensorDataListStart = (int) Math.floor((Math.random() * sensorDataList.size()));
            for (int j = sensorDataListStart; j < sensorDataList.size(); j++) {
                String sensorIdentifier = sensorDataList.get(j);
                SensorDataI sensorDataIMPL = new SensorDataIMPL(sensorIdentifier, Math.random() * 100);
                row.add(sensorDataIMPL);
            }
            sensorData.add(row);
        }
        return sensorData;
    }

    // Testing the SensorDataGenerator
    public static void main(String[] args) {
        List<List<SensorDataI>> sensorData = generateSensorData(5);
        for (List<SensorDataI> row : sensorData) {
            System.out.println("-----------------------------");
            for (SensorDataI sensorDataI : row) {
                System.out.println(sensorDataI.getSensorIdentifier() + " " + sensorDataI.getValue());
            }
        }
    }

}
