package enemies;

import entities.Enemy;
import utils.Vector2D;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Image;
import javax.swing.ImageIcon;
import java.net.URL;
import java.io.File;

public class BasicEnemy extends Enemy {
    private Image enemyImage;
    private ImageIcon enemyGif;
    private static final int IMAGE_WIDTH = 70;
    private static final int IMAGE_HEIGHT = 60;
    private volatile boolean imageLoaded = false;
    
    public BasicEnemy(Vector2D startPosition) {
        super(startPosition, 50, 1.0, 25);
        // Load GIF asynchronously to prevent blocking
        loadGifAsync("enemies/basic_enemy.gif");
    }
    
    private void loadGifAsync(String gifPath) {
        Thread loadThread = new Thread(() -> {
            loadGif(gifPath);
            imageLoaded = true;
        }, "BasicEnemy-ImageLoader");
        loadThread.setDaemon(true);
        loadThread.start();
    }
    
    private void loadGif(String gifPath) {
        try {
            // Try classpath first
            URL gifUrl = getClass().getClassLoader().getResource(gifPath);
            if (gifUrl != null) {
                loadFromUrl(gifUrl);
            } else {
                // Try direct file path from working directory
                if (loadFromFile(gifPath)) {
                    return;
                }
                
                // Try relative to parent directory
                if (loadFromFile("../" + gifPath)) {
                    return;
                }
                
                // Try absolute path
                if (loadFromFile(new File(gifPath).getAbsolutePath())) {
                    return;
                }
                
                System.err.println("Could not load GIF: " + gifPath);
            }
        } catch (Exception e) {
            System.err.println("Error loading GIF: " + e.getMessage());
        }
    }
    
    private void loadFromUrl(URL url) {
        try {
            enemyGif = new ImageIcon(url);
            enemyImage = enemyGif.getImage();
        } catch (Exception e) {
            System.err.println("Error loading from URL: " + e.getMessage());
            enemyGif = null;
            enemyImage = null;
        }
    }
    
    private boolean loadFromFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                enemyGif = new ImageIcon(filePath);
                enemyImage = enemyGif.getImage();
                return true;
            }
        } catch (Exception e) {
            // Silent fail, try next path
        }
        return false;
    }
    
    @Override
    public void render(Graphics2D g2d) {
        if (enemyImage != null && imageLoaded) {
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
