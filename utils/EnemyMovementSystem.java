package utils;

import entities.Enemy;
import java.util.List;

public class EnemyMovementSystem {
    
    public static void updateEnemy(Enemy enemy, double deltaTime, List<Vector2D> path) {
        if (enemy.hasReachedEnd(path)) {
            return;
        }
        
        int currentIndex = getCurrentPathIndex(enemy, path);
        if (currentIndex >= path.size() - 1) {
            // Reached end
            return;
        }
        
        Vector2D currentPos = enemy.getPosition();
        Vector2D targetPos = path.get(currentIndex + 1);
        
        // Calculate direction
        double dx = targetPos.getX() - currentPos.getX();
        double dy = targetPos.getY() - currentPos.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance < 1.0) {
            // Reached waypoint
            enemy.setPosition(targetPos);
        } else {
            // Move towards waypoint
            double moveDistance = enemy.getSpeed() * deltaTime * 60.0;
            if (moveDistance > distance) {
                moveDistance = distance;
            }
            
            double newX = currentPos.getX() + (dx / distance) * moveDistance;
            double newY = currentPos.getY() + (dy / distance) * moveDistance;
            
            enemy.setPosition(new Vector2D(newX, newY));
        }
    }
    
    private static int getCurrentPathIndex(Enemy enemy, List<Vector2D> path) {
        // Simple implementation - always target next waypoint
        // In a real game, you'd have enemy track which waypoint it's heading toward
        return 0; // Simplified
    }
}