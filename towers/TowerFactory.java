package towers;

import entities.Tower;
import utils.Vector2D;

public class TowerFactory {
    public static Tower createTower(String type, Vector2D position) {
        if (type == null) {
            return new BasicTower(position);
        }
        
        switch (type) {
            case "Basic Tower":
                return new BasicTower(position);
            case "Sniper Tower":
                return new SniperTower(position);
            case "AOE Tower":
                return new AoeTower(position);
            default:
                // Default to basic tower if type is unknown
                System.err.println("Unknown tower type: " + type + ", creating Basic Tower");
                return new BasicTower(position);
        }
    }
}