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

public class BasicTower extends Tower {
    private Image towerImage;
    private boolean usingFallback = false;
    
    // Tower size - same pattern as AoeTower
    private final int DRAW_WIDTH = 70;
    private final int DRAW_HEIGHT = 70;
    
    // Laser properties
    private double laserTargetX = -1;
    private double laserTargetY = -1;
    private long laserStartTime = 0;
    private final long LASER_DURATION = 200;
    
    // Static image cache to prevent reloading
    private static Image cachedTowerImage = null;
    private static boolean imageLoaded = false;
    
    public BasicTower(Vector2D position) {
        super(position, 100);
        this.damage = 10;
        this.range = 150;
        this.fireRate = 1.0;
        
        System.out.println("=== Creating BasicTower ===");
        loadImage();
    }
    
    private void loadImage() {
        // Check if image is already cached (prevents reloading every time)
        if (cachedTowerImage != null) {
            towerImage = cachedTowerImage;
            System.out.println("✓ Using cached tower image");
            return;
        }
        
        // List of paths to check
        String[] imagePaths = {
            "basic_tower.gif",
            "towers/basic_tower.gif",
            "basic_tower.png",
            "towers/basic_tower.png",
            "images/basic_tower.png",
            "images/towers/basic_tower.gif",
            "images/towers/basic_tower.png"
        };
        
        boolean imageFound = false;
        
        for (String path : imagePaths) {
            File file = new File(path);
            if (file.exists()) {
                System.out.println("Found image at: " + file.getAbsolutePath());
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
                            System.out.println("✓ Using GIF (waited " + waited + "ms): " + path);
                            System.out.println("  Image size: " + icon.getIconWidth() + "x" + icon.getIconHeight());
                            imageFound = true;
                            break;
                        }
                    } else {
                        // Load PNG/JPG (synchronous)
                        towerImage = javax.imageio.ImageIO.read(file);
                        cachedTowerImage = towerImage;
                        System.out.println("✓ Loaded static image: " + path);
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
            System.out.println("No basic tower image found, creating fallback...");
            usingFallback = true;
            createFallbackImage();
            // Cache the fallback image too
            cachedTowerImage = towerImage;
        }
        
        imageLoaded = true;
    }
    
    private void createFallbackImage() {
        System.out.println("Creating fallback tower image...");
        
        // Create image at desired drawing size
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
            DRAW_WIDTH, DRAW_HEIGHT, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        
        // Transparent background
        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, DRAW_WIDTH, DRAW_HEIGHT);
        
        // Blue tower base
        g.setColor(new Color(100, 149, 237)); // Cornflower blue
        g.fillRect(5, 5, DRAW_WIDTH - 10, DRAW_HEIGHT - 10);
        
        // Darker blue turret
        g.setColor(new Color(65, 105, 225)); // Royal blue
        g.fillOval(10, 10, DRAW_WIDTH - 20, DRAW_HEIGHT - 20);
        
        // Gun pointing upward
        g.setColor(Color.DARK_GRAY);
        g.fillRect(DRAW_WIDTH/2 - 2, 5, 4, 15);
        
        // Crosshair or target symbol (like a basic tower)
        g.setColor(Color.YELLOW);
        int centerX = DRAW_WIDTH / 2;
        int centerY = DRAW_HEIGHT / 2;
        
        // Draw crosshair
        g.drawLine(centerX, centerY - 8, centerX, centerY + 8);
        g.drawLine(centerX - 8, centerY, centerX + 8, centerY);
        g.drawOval(centerX - 6, centerY - 6, 12, 12);
        
        // Add "Basic" text for clarity
        g.setColor(Color.WHITE);
        g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 9));
        g.drawString("Basic", centerX - 15, centerY + 20);
        
        g.dispose();
        towerImage = img;
        
        // Save it for next time
        try {
            File output = new File("basic_tower_fallback.png");
            javax.imageio.ImageIO.write(img, "PNG", output);
            System.out.println("Saved fallback image to: " + output.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("Couldn't save fallback image: " + e.getMessage());
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
                target.takeDamage(damage);
                
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
        // Draw laser if active
        if (laserTargetX >= 0) {
            drawLaserBeam(g2d);
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
    }
    
    private void drawLoadingPlaceholder(Graphics2D g2d, int x, int y) {
        // Draw a pulsing loading indicator
        long time = System.currentTimeMillis();
        float pulse = (float) (Math.sin(time * 0.01) * 0.3 + 0.7);
        
        g2d.setColor(new Color(100, 149, 237, (int)(pulse * 255)));
        g2d.fillRect(x, y, DRAW_WIDTH, DRAW_HEIGHT);
        
        g2d.setColor(Color.WHITE);
        g2d.drawString("Loading...", x + 5, y + DRAW_HEIGHT/2);
    }
    
    private void drawLaserBeam(Graphics2D g2d) {
        long elapsed = System.currentTimeMillis() - laserStartTime;
        float opacity = 1.0f - (elapsed / (float)LASER_DURATION);
        opacity = Math.max(0.1f, opacity);
        
        java.awt.Stroke originalStroke = g2d.getStroke();
        
        // Main laser
        g2d.setColor(new Color(255, 0, 0, (int)(opacity * 200)));
        g2d.setStroke(new BasicStroke(3.0f));
        g2d.drawLine(
            (int)position.getX(),
            (int)position.getY(),
            (int)laserTargetX,
            (int)laserTargetY
        );
        
        // Inner energy
        g2d.setColor(new Color(255, 255, 100, (int)(opacity * 255)));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawLine(
            (int)position.getX(),
            (int)position.getY(),
            (int)laserTargetX,
            (int)laserTargetY
        );
        
        // Impact point
        g2d.setColor(new Color(255, 200, 100, (int)(opacity * 150)));
        g2d.fillOval((int)laserTargetX - 5, (int)laserTargetY - 5, 10, 10);
        
        g2d.setStroke(originalStroke);
    }
    
    private void drawFallbackTower(Graphics2D g2d) {
        // Fallback drawing
        int drawX = (int)position.getX() - DRAW_WIDTH / 2;
        int drawY = (int)position.getY() - DRAW_HEIGHT / 2;
        
        // Blue tower base
        g2d.setColor(new Color(100, 149, 237));
        g2d.fillRect(drawX, drawY, DRAW_WIDTH, DRAW_HEIGHT);
        
        // Darker blue turret
        g2d.setColor(new Color(65, 105, 225));
        g2d.fillOval(drawX + 10, drawY + 10, DRAW_WIDTH - 20, DRAW_HEIGHT - 20);
        
        // Crosshair symbol
        int centerX = (int)position.getX();
        int centerY = (int)position.getY();
        
        g2d.setColor(Color.YELLOW);
        g2d.drawLine(centerX, centerY - 8, centerX, centerY + 8);
        g2d.drawLine(centerX - 8, centerY, centerX + 8, centerY);
        g2d.drawOval(centerX - 6, centerY - 6, 12, 12);
        
        // Add text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 9));
        g2d.drawString("Basic", centerX - 15, centerY + 20);
    }
    
    private void drawLevelIndicator(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 11));
        
        // Shadow
        g2d.setColor(Color.BLACK);
        g2d.drawString("Lvl " + level, (int)position.getX() - 18, (int)position.getY() + 26);
        
        // Text
        g2d.setColor(Color.WHITE);
        g2d.drawString("Lvl " + level, (int)position.getX() - 17, (int)position.getY() + 25);
    }
    
    @Override
    public void upgrade() {
        level++;
        damage += 5;
        range += 20;
        fireRate += 0.2;
        upgradeCost += 50;
    }
    
    // Getter methods for size
    public int getDrawWidth() { return DRAW_WIDTH; }
    public int getDrawHeight() { return DRAW_HEIGHT; }
    
    // Static method to preload the image (call this at game startup)
    public static void preloadImage() {
        if (!imageLoaded) {
            System.out.println("=== Preloading BasicTower image ===");
            BasicTower temp = new BasicTower(new Vector2D(0, 0));
            System.out.println("=== BasicTower preloading complete ===");
        }
    }
}