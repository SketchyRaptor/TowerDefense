package towers;

import entities.Tower;
import entities.Enemy;
import utils.Vector2D;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Image;
import java.awt.BasicStroke;
import javax.swing.ImageIcon;
import java.io.File;
import java.util.List;

public class SniperTower extends Tower {
    private Image towerImage;
    private boolean usingFallback = false;
    
    // Tower size
    private final int DRAW_WIDTH = 90;   // Sniper is medium size
    private final int DRAW_HEIGHT = 90;  // Taller for sniper look
    
    // Static image cache to prevent reloading
    private static Image cachedTowerImage = null;
    private static boolean imageLoaded = false;
    
    // Sniper laser properties
    private double laserTargetX = -1;
    private double laserTargetY = -1;
    private long laserStartTime = 0;
    private final long LASER_DURATION = 150; // Shorter for sniper (faster shot)
    private Color laserColor = new Color(0, 255, 100); // Green laser for sniper
    
    // Sniper-specific properties
    public SniperTower(Vector2D position) {
        super(position, 250);
        this.damage = 40;     // High single-target damage
        this.range = 250;     // Very long range
        this.fireRate = 0.4;  // Slow firing rate
        
        System.out.println("=== Creating SniperTower ===");
        loadImage();
    }
    
    private void loadImage() {
        // Check if image is already cached (prevents reloading every time)
        if (cachedTowerImage != null) {
            towerImage = cachedTowerImage;
            System.out.println("✓ Using cached sniper tower image");
            return;
        }
        
        // List of paths to check
        String[] imagePaths = {
            "sniper_tower.gif",
            "towers/sniper_tower.gif",
            "sniper_tower.png",
            "towers/sniper_tower.png",
            "images/sniper_tower.png",
            "images/towers/sniper_tower.gif",
            "images/towers/sniper_tower.png"
        };
        
        boolean imageFound = false;
        
        for (String path : imagePaths) {
            File file = new File(path);
            if (file.exists()) {
                System.out.println("Found sniper image at: " + file.getAbsolutePath());
                try {
                    if (path.toLowerCase().endsWith(".gif")) {
                        // Load GIF with better handling
                        ImageIcon icon = new ImageIcon(file.getAbsolutePath());
                        
                        // If image is already loaded, use it immediately
                        if (icon.getImageLoadStatus() == java.awt.MediaTracker.COMPLETE) {
                            towerImage = icon.getImage();
                            cachedTowerImage = towerImage;
                            System.out.println("✓ GIF already loaded: " + path);
                            imageFound = true;
                            break;
                        }
                        
                        // Wait for the GIF to load (but not too long)
                        int maxWait = 200; // Only wait 200ms max
                        int waited = 0;
                        int checkInterval = 10;
                        
                        while (icon.getImageLoadStatus() == java.awt.MediaTracker.LOADING 
                               && waited < maxWait) {
                            try {
                                Thread.sleep(checkInterval);
                                waited += checkInterval;
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                        
                        // Use the image even if not fully loaded yet
                        if (icon.getImage() != null) {
                            towerImage = icon.getImage();
                            cachedTowerImage = towerImage;
                            System.out.println("✓ Using sniper GIF (waited " + waited + "ms): " + path);
                            System.out.println("  Image size: " + icon.getIconWidth() + "x" + icon.getIconHeight());
                            imageFound = true;
                            break;
                        }
                    } else {
                        // Load PNG/JPG (synchronous)
                        towerImage = javax.imageio.ImageIO.read(file);
                        cachedTowerImage = towerImage;
                        System.out.println("✓ Loaded sniper static image: " + path);
                        if (towerImage != null) {
                            System.out.println("  Image size: " + towerImage.getWidth(null) + "x" + towerImage.getHeight(null));
                        }
                        imageFound = true;
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("✗ Error loading " + path + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        
        if (!imageFound) {
            System.out.println("No sniper tower image found, creating fallback...");
            usingFallback = true;
            createFallbackImage();
            // Cache the fallback image too
            cachedTowerImage = towerImage;
        }
        
        imageLoaded = true;
    }
    
    private void createFallbackImage() {
        System.out.println("Creating sniper fallback tower image...");
        
        // Create a sniper tower image
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
            DRAW_WIDTH, DRAW_HEIGHT, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        
        // Transparent background
        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, DRAW_WIDTH, DRAW_HEIGHT);
        
        // Dark green tower base (sniper color)
        g.setColor(new Color(0, 100, 0));
        g.fillRect(5, 5, DRAW_WIDTH - 10, DRAW_HEIGHT - 10);
        
        // Green sniper scope/top
        g.setColor(new Color(0, 150, 0));
        g.fillOval(10, 5, DRAW_WIDTH - 20, 20);
        
        // Long sniper barrel
        g.setColor(Color.DARK_GRAY);
        g.fillRect(DRAW_WIDTH/2 - 1, 15, 2, DRAW_HEIGHT - 25);
        
        // Scope crosshair
        g.setColor(Color.WHITE);
        int centerX = DRAW_WIDTH / 2;
        int centerY = 15;
        g.drawLine(centerX - 5, centerY, centerX + 5, centerY);
        g.drawLine(centerX, centerY - 5, centerX, centerY + 5);
        
        // Add "SNP" text for clarity
        g.setColor(Color.WHITE);
        g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 9));
        g.drawString("SNP", centerX - 10, DRAW_HEIGHT - 5);
        
        g.dispose();
        towerImage = img;
        
        // Save it for next time
        try {
            File output = new File("sniper_tower_fallback.png");
            javax.imageio.ImageIO.write(img, "PNG", output);
            System.out.println("Saved sniper fallback image to: " + output.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("Couldn't save sniper fallback image: " + e.getMessage());
        }
    }
    
    @Override
    public void update(double deltaTime, List<Enemy> enemies) {
        double currentTime = System.currentTimeMillis() / 1000.0;
        
        // Clear laser after duration
        if (laserTargetX >= 0 && System.currentTimeMillis() - laserStartTime > LASER_DURATION) {
            laserTargetX = -1;
        }
        
        if (canShoot(currentTime)) {
            Enemy target = findTarget(enemies);
            if (target != null) {
                // Deal high damage to single target
                target.takeDamage(damage);
                
                // Store target position for laser
                Vector2D targetPos = target.getPosition();
                laserTargetX = targetPos.getX();
                laserTargetY = targetPos.getY();
                laserStartTime = System.currentTimeMillis();
                lastShotTime = currentTime;
            }
        }
    }
    
    @Override
    public void render(Graphics2D g2d) {
        // Draw sniper laser first (thinner, more precise)
        if (laserTargetX >= 0) {
            drawSniperLaser(g2d);
        }
        
        // Draw the tower
        int drawX = (int)position.getX() - DRAW_WIDTH / 2;
        int drawY = (int)position.getY() - DRAW_HEIGHT / 2;
        
        if (towerImage != null) {
            // Draw with red tint if using fallback (for debugging)
            if (usingFallback && false) { // Set to true to see fallback towers
                g2d.setColor(new Color(255, 0, 0, 30));
                g2d.fillRect(drawX, drawY, DRAW_WIDTH, DRAW_HEIGHT);
            }
            
            // Draw the image
            g2d.drawImage(towerImage, drawX, drawY, DRAW_WIDTH, DRAW_HEIGHT, null);
            
            // If image is still loading (null size), draw a placeholder
            if (towerImage.getWidth(null) <= 0 || towerImage.getHeight(null) <= 0) {
                drawLoadingPlaceholder(g2d, drawX, drawY);
            }
        } else {
            // If no image at all, draw the fallback directly
            drawFallbackTower(g2d);
        }
        
        // Draw level indicator
        drawLevelIndicator(g2d);
        
        // Optional: Draw range circle (for debugging)
        drawRangeCircle(g2d);
    }
    
    private void drawLoadingPlaceholder(Graphics2D g2d, int x, int y) {
        // Draw a pulsing loading indicator
        long time = System.currentTimeMillis();
        float pulse = (float) (Math.sin(time * 0.01) * 0.3 + 0.7);
        
        g2d.setColor(new Color(0, 100, 0, (int)(pulse * 255)));
        g2d.fillRect(x, y, DRAW_WIDTH, DRAW_HEIGHT);
        
        g2d.setColor(Color.WHITE);
        g2d.drawString("Loading...", x + 5, y + DRAW_HEIGHT/2);
    }
    
    private void drawSniperLaser(Graphics2D g2d) {
        long elapsed = System.currentTimeMillis() - laserStartTime;
        float opacity = 1.0f - (elapsed / (float)LASER_DURATION);
        opacity = Math.max(0.1f, opacity);
        
        // Save original stroke and color
        java.awt.Stroke originalStroke = g2d.getStroke();
        Color originalColor = g2d.getColor();
        
        // Sniper laser is thinner and more precise
        // Outer glow (very thin)
        g2d.setColor(new Color(0, 255, 100, (int)(opacity * 60)));
        g2d.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(
            (int)position.getX(),
            (int)position.getY(),
            (int)laserTargetX,
            (int)laserTargetY
        );
        
        // Main laser line (bright green)
        g2d.setColor(new Color(0, 255, 100, (int)(opacity * 200)));
        g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(
            (int)position.getX(),
            (int)position.getY(),
            (int)laserTargetX,
            (int)laserTargetY
        );
        
        // Inner core (white, very thin)
        g2d.setColor(new Color(255, 255, 255, (int)(opacity * 255)));
        g2d.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(
            (int)position.getX(),
            (int)position.getY(),
            (int)laserTargetX,
            (int)laserTargetY
        );
        
        // Sniper impact effect (small, precise)
        drawSniperImpact(g2d, opacity);
        
        // Restore stroke and color
        g2d.setStroke(originalStroke);
        g2d.setColor(originalColor);
    }
    
    private void drawSniperImpact(Graphics2D g2d, float opacity) {
        // Small, precise impact point for sniper
        g2d.setColor(new Color(0, 255, 100, (int)(opacity * 200)));
        g2d.fillOval(
            (int)laserTargetX - 3,
            (int)laserTargetY - 3,
            6,
            6
        );
        
        // White center
        g2d.setColor(new Color(255, 255, 255, (int)(opacity * 255)));
        g2d.fillOval(
            (int)laserTargetX - 1,
            (int)laserTargetY - 1,
            2,
            2
        );
        
        // Small expanding ring (sniper bullet impact)
        long elapsed = System.currentTimeMillis() - laserStartTime;
        int ringSize = 4 + (int)(elapsed / 20);
        if (ringSize < 15) {
            g2d.setColor(new Color(0, 255, 100, (int)(opacity * (1.0f - (float)ringSize/15.0f) * 100)));
            g2d.setStroke(new BasicStroke(1.0f));
            g2d.drawOval(
                (int)laserTargetX - ringSize/2,
                (int)laserTargetY - ringSize/2,
                ringSize,
                ringSize
            );
        }
    }
    
    private void drawFallbackTower(Graphics2D g2d) {
        // Fallback drawing
        int drawX = (int)position.getX() - DRAW_WIDTH / 2;
        int drawY = (int)position.getY() - DRAW_HEIGHT / 2;
        
        // Dark green tower base
        g2d.setColor(new Color(0, 100, 0));
        g2d.fillRect(drawX, drawY, DRAW_WIDTH, DRAW_HEIGHT);
        
        // Green sniper scope
        g2d.setColor(new Color(0, 150, 0));
        g2d.fillOval(drawX + 5, drawY, DRAW_WIDTH - 10, 20);
        
        // Long sniper barrel
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect((int)position.getX() - 1, drawY + 10, 2, DRAW_HEIGHT - 20);
        
        // Scope crosshair
        g2d.setColor(Color.WHITE);
        g2d.drawLine((int)position.getX() - 5, drawY + 10, 
                     (int)position.getX() + 5, drawY + 10);
        g2d.drawLine((int)position.getX(), drawY + 5, 
                     (int)position.getX(), drawY + 15);
        
        // Add "SNP" text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 9));
        g2d.drawString("SNP", (int)position.getX() - 10, drawY + DRAW_HEIGHT - 5);
    }
    
    private void drawLevelIndicator(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 10));
        
        // Shadow
        g2d.setColor(Color.BLACK);
        g2d.drawString("SNP " + level, (int)position.getX() - 13, (int)position.getY() + 6);
        
        // Text
        g2d.setColor(Color.WHITE);
        g2d.drawString("SNP " + level, (int)position.getX() - 14, (int)position.getY() + 5);
    }
    
    private void drawRangeCircle(Graphics2D g2d) {
        if (false) { // Change to true to debug range
            // Semi-transparent green circle (sniper range)
            g2d.setColor(new Color(0, 150, 0, 20));
            g2d.fillOval(
                (int)(position.getX() - range),  // FIXED: Added parentheses
                (int)(position.getY() - range),  // FIXED: Added parentheses
                (int)(range * 2),                // FIXED: Cast to int
                (int)(range * 2)                 // FIXED: Cast to int
            );
            
            // Outline
            g2d.setColor(new Color(0, 150, 0, 80));
            g2d.drawOval(
                (int)(position.getX() - range),  // FIXED: Added parentheses
                (int)(position.getY() - range),  // FIXED: Added parentheses
                (int)(range * 2),                // FIXED: Cast to int
                (int)(range * 2)                 // FIXED: Cast to int
            );
        }
    }
    
    @Override
    public void upgrade() {
        level++;
        damage += 20;      // Big damage increase for sniper
        range += 30;       // Even longer range
        fireRate += 0.05;  // Slight firing rate increase
        upgradeCost += 100;
        
        // Change laser color based on level
        if (level >= 2) {
            laserColor = new Color(100, 255, 100); // Brighter green
        }
        if (level >= 3) {
            laserColor = new Color(150, 255, 150); // Light green
        }
    }
    
    // Static method to preload the image (call this at game startup)
    public static void preloadImage() {
        if (!imageLoaded) {
            System.out.println("=== Preloading SniperTower image ===");
            SniperTower temp = new SniperTower(new Vector2D(0, 0));
            System.out.println("=== SniperTower preloading complete ===");
        }
    }
}