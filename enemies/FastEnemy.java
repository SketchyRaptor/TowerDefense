package enemies;

import entities.Enemy;
import utils.Vector2D;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Image;
import javax.swing.ImageIcon;
import java.net.URL;
import java.io.File;

public class FastEnemy extends Enemy {
    private Image enemyImage;
    private ImageIcon enemyGif;
    private static final int IMAGE_WIDTH = 70;
    private static final int IMAGE_HEIGHT = 60;
    private volatile boolean imageLoaded = false;
    
    public FastEnemy(Vector2D startPosition) {
        super(startPosition, 30, 2.0, 15);
        // Load GIF asynchronously to prevent blocking
        loadGifAsync("enemies/fast_enemy.gif");
    }
    
    private void loadGifAsync(String gifPath) {
        Thread loadThread = new Thread(() -> {
            loadGif(gifPath);
            imageLoaded = true;
        }, "FastEnemy-ImageLoader");
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
            int drawX = (int)position.getX() - IMAGE_WIDTH / 2;
            int drawY = (int)position.getY() - IMAGE_HEIGHT / 2;
            g2d.drawImage(enemyImage, drawX, drawY, IMAGE_WIDTH, IMAGE_HEIGHT, null);
        } else {
            drawFallbackEnemy(g2d);
        }
        
        drawHealthBar(g2d);
    }
    
    private void drawFallbackEnemy(Graphics2D g2d) {
        g2d.setColor(Color.YELLOW);
        g2d.fillOval(
            (int)position.getX() - 10, 
            (int)position.getY() - 10, 
            20, 20
        );
        
        g2d.setColor(Color.BLACK);
        g2d.drawOval(
            (int)position.getX() - 10, 
            (int)position.getY() - 10, 
            20, 20
        );
    }
}
