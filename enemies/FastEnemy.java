package enemies;

import entities.Enemy;
import utils.Vector2D;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Image;
import javax.swing.ImageIcon;
import java.net.URL;

public class FastEnemy extends Enemy {
    private Image enemyImage;
    private ImageIcon enemyGif;
    private static final int IMAGE_WIDTH = 70;
    private static final int IMAGE_HEIGHT = 60;
    
    public FastEnemy(Vector2D startPosition) {
        super(startPosition, 30, 2.0, 15);
        loadGif("enemies/fast_enemy.gif");
    }
    
    private void loadGif(String gifPath) {
        try {
            URL gifUrl = getClass().getClassLoader().getResource(gifPath);
            if (gifUrl != null) {
                enemyGif = new ImageIcon(gifUrl);
                enemyImage = enemyGif.getImage();
            } else {
                enemyGif = new ImageIcon(gifPath);
                enemyImage = enemyGif.getImage();
                
                if (enemyGif.getImageLoadStatus() != java.awt.MediaTracker.COMPLETE) {
                    enemyGif = null;
                    enemyImage = null;
                }
            }
        } catch (Exception e) {
            enemyGif = null;
            enemyImage = null;
        }
    }
    
    @Override
    public void render(Graphics2D g2d) {
        if (enemyImage != null) {
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