package entities;

import utils.Vector2D;
import java.io.Serializable;
import java.awt.Graphics2D;
import java.awt.Color;
import java.util.List;

public abstract class Enemy implements Serializable {
    private static final long serialVersionUID = 1L;
    
    protected Vector2D position;
    protected int maxHealth;
    protected int currentHealth;
    protected int reward;
    protected double speed;
    protected int pathIndex;
    
    public Enemy(Vector2D startPosition, int health, double speed, int reward) {
        this.position = startPosition;
        this.maxHealth = health;
        this.currentHealth = health;
        this.speed = speed;
        this.reward = reward;
        this.pathIndex = 0;
    }
    
    public void update(double deltaTime, List<Vector2D> path) {
        if (pathIndex < path.size()) {
            Vector2D target = path.get(pathIndex);
            Vector2D direction = new Vector2D(
                target.getX() - position.getX(),
                target.getY() - position.getY()
            ).normalize();
            
            // Move towards target
            double moveDistance = speed * deltaTime * 60.0; // Adjusted for frame rate
            
            position = new Vector2D(
                position.getX() + direction.getX() * moveDistance,
                position.getY() + direction.getY() * moveDistance
            );
            
            // Check if reached current target
            if (position.distanceTo(target) < 2.0) {
                pathIndex++;
            }
        }
    }
    
    public abstract void render(Graphics2D g2d);
    
    // Add this method to Enemy class
    protected void drawHealthBar(Graphics2D g2d) {
        int barWidth = 30;
        int barHeight = 5;
        int barX = (int)position.getX() - barWidth/2;
        int barY = (int)position.getY() - 15;
        
        // Background (black)
        g2d.setColor(Color.BLACK);
        g2d.fillRect(barX, barY, barWidth, barHeight);
        
        // Health (green to red based on health percentage)
        double healthPercent = (double)currentHealth / maxHealth;
        int healthWidth = (int)(barWidth * healthPercent);
        
        if (healthPercent > 0.6) {
            g2d.setColor(Color.GREEN);
        } else if (healthPercent > 0.3) {
            g2d.setColor(Color.YELLOW);
        } else {
            g2d.setColor(Color.RED);
        }
        
        g2d.fillRect(barX, barY, healthWidth, barHeight);
        
        // Outline
        g2d.setColor(Color.BLACK);
        g2d.drawRect(barX, barY, barWidth, barHeight);
    }
    
    public void takeDamage(int damage) {
        currentHealth -= damage;
        if (currentHealth < 0) currentHealth = 0;
    }
    
    public boolean isDead() {
        return currentHealth <= 0;
    }
    
    public boolean hasReachedEnd(List<Vector2D> path) {
        return pathIndex >= path.size();
    }
    
    // Getters
    public Vector2D getPosition() { return position; }
    public int getHealth() { return currentHealth; }
    public int getMaxHealth() { return maxHealth; }
    public int getReward() { return reward; }
    public double getSpeed() { return speed; }
    
    // Setters
    public void setPosition(Vector2D position) { this.position = position; }
}