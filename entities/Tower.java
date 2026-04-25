package entities;

import utils.Vector2D;
import java.io.Serializable;
import java.awt.Graphics2D;
import java.util.List;

public abstract class Tower implements Serializable {
    private static final long serialVersionUID = 1L;
    
    protected Vector2D position;
    protected int level;
    protected int damage;
    protected double range;
    protected double fireRate; // shots per second
    protected double lastShotTime; // in seconds
    protected int cost;
    protected int upgradeCost;
    
    public Tower(Vector2D position, int cost) {
        this.position = position;
        this.level = 1;
        this.cost = cost;
        this.upgradeCost = cost / 2;
        this.lastShotTime = 0.0;
    }
    
    public abstract void update(double deltaTime, List<Enemy> enemies);
    public abstract void render(Graphics2D g2d);
    public abstract void upgrade();
    
    // Add this method - it was missing!
    public boolean canShoot(double currentTime) {
        return (currentTime - lastShotTime) >= (1.0 / fireRate);
    }
    
    // Add this method for finding targets
    protected Enemy findTarget(List<Enemy> enemies) {
        if (enemies == null || enemies.isEmpty()) {
            return null;
        }
        
        Enemy target = null;
        double closestDistance = Double.MAX_VALUE;
        
        // Find closest enemy within range
        for (Enemy enemy : enemies) {
            double distance = position.distanceTo(enemy.getPosition());
            if (distance <= range && distance < closestDistance) {
                closestDistance = distance;
                target = enemy;
            }
        }
        
        return target;
    }
    
    // Getters and setters
    public Vector2D getPosition() { return position; }
    public int getLevel() { return level; }
    public int getDamage() { return damage; }
    public double getRange() { return range; }
    public int getCost() { return cost; }
    public int getUpgradeCost() { return upgradeCost; }
    public double getFireRate() { return fireRate; }
    
    public void setLevel(int level) { this.level = level; }
    public void setDamage(int damage) { this.damage = damage; }
    public void setRange(double range) { this.range = range; }
    public void setFireRate(double fireRate) { this.fireRate = fireRate; }
    public void setLastShotTime(double lastShotTime) { this.lastShotTime = lastShotTime; }
}