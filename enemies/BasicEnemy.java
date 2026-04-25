package enemies;

import entities.Enemy;
import utils.Vector2D;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import javax.swing.ImageIcon;
import java.net.URL;

public class BasicEnemy extends Enemy {
    private Image enemyImage;
    private ImageIcon enemyGif;
    private static final int IMAGE_WIDTH = 70;
    private static final int IMAGE_HEIGHT = 60;
    
    public BasicEnemy(Vector2D startPosition) {
        super(startPosition, 50, 1.0, 25);
        loadGif("enemies/basic_enemy.gif");
    }
    
    private void loadGif(String gifPath) {
        try {
            // Method 1: Using ImageIcon (handles animation automatically)
            URL gifUrl = getClass().getClassLoader().getResource(gifPath);
            if (gifUrl != null) {
                enemyGif = new ImageIcon(gifUrl);
                enemyImage = enemyGif.getImage();
            } else {
                // Try loading from file system
                enemyGif = new ImageIcon(gifPath);
                enemyImage = enemyGif.getImage();
                
                if (enemyGif.getImageLoadStatus() != java.awt.MediaTracker.COMPLETE) {
                    enemyGif = null;
                    enemyImage = null;
                    System.err.println("Could not load GIF: " + gifPath);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading GIF: " + e.getMessage());
            enemyGif = null;
            enemyImage = null;
        }
    }
    
    @Override
    public void render(Graphics2D g2d) {
        if (enemyImage != null) {
            // Draw the animated GIF centered at the enemy's position
            int drawX = (int)position.getX() - IMAGE_WIDTH / 2;
            int drawY = (int)position.getY() - IMAGE_HEIGHT / 2;
            g2d.drawImage(enemyImage, drawX, drawY, IMAGE_WIDTH, IMAGE_HEIGHT, null);
        } else {
            // Fallback to static drawing
            drawFallbackEnemy(g2d);
        }
        
        // Draw health bar (inherited from Enemy class)
        drawHealthBar(g2d);
    }
    
    private void drawFallbackEnemy(Graphics2D g2d) {
        // Original rectangle drawing as fallback
        g2d.setColor(Color.RED);
        g2d.fillRect(
            (int)position.getX() - 10, 
            (int)position.getY() - 10, 
            20, 20
        );
        
        g2d.setColor(Color.BLACK);
        g2d.drawRect(
            (int)position.getX() - 10, 
            (int)position.getY() - 10, 
            20, 20
        );
    }
    
    // Get the ImageIcon for external use (if needed)
    public ImageIcon getGif() {
        return enemyGif;
    }
}