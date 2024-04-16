package bcm;

import java.util.HashMap;
import java.util.Map;

public class NodeMapGenerator {
    
    public static Map<String, int[]> generateNodeCoordinates(int gridSize) {
        // Calculer les dimensions de la grille en fonction du nombre de nœuds approximatif
        
        int nodeId = 1;
        Map<String, int[]> nodeMap = new HashMap<>();
        
        
        for (int y = 1; y <= gridSize; y++) {
            // Itération pour les lignes impaires de la grille
            if (y % 2 != 0) {
                for (int x = 1; x <= gridSize; x += 2) {
                    nodeMap.put("n" + nodeId, new int[]{x, y});
                    nodeId++;
                }
            }else {
                // Itération pour les lignes paires de la grille
                    for (int x = 2; x <= gridSize; x += 2) {
                        nodeMap.put("n" + nodeId, new int[]{x, y });
                        nodeId++;
                    }
                }
            
            
        }
        
        return nodeMap;
    }

    public static void main(String[] args) {
        int n = 6; // Nombre approximatif de nœuds à créer
        Map<String, int[]> nodeCoordinates = generateNodeCoordinates(n);
        
        // Affichage des nœuds et de leurs coordonnées
        for (Map.Entry<String, int[]> entry : nodeCoordinates.entrySet()) {
            System.out.println(entry.getKey() + " -> (" + entry.getValue()[0] + ", " + entry.getValue()[1] + ")");
        }
    }
}
