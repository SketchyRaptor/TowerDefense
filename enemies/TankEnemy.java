package enemies;

import entities.Enemy;
import utils.Vector2D;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Image;
import javax.swing.ImageIcon;
import java.net.URL;

public class TankEnemy extends Enemy {
    private Image enemyImage;
    private ImageIcon enemyGif;
    private static final int IMAGE_WIDTH = 100;
    private static final int IMAGE_HEIGHT = 100;
    
    public TankEnemy(Vector2D startPosition) {
        super(startPosition, 500, 0.5, 75);
        loadGif("enemies/tank_enemy.gif");
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
        g2d.setColor(new Color(128, 0, 128));
        g2d.fillRect(
            (int)position.getX() - 15, 
            (int)position.getY() - 15, 
            30, 30
        );
        
        g2d.setColor(Color.BLACK);
        g2d.drawRect(
            (int)position.getX() - 15, 
            (int)position.getY() - 15, 
            30, 30
        );
        
        g2d.setColor(new Color(100, 0, 100));
        g2d.fillRect(
            (int)position.getX() - 5, 
            (int)position.getY() - 25, 
            10, 20
        );
    }
}