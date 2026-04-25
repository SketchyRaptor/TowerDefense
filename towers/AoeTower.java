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
import java.util.ArrayList;

public class AoeTower extends Tower {
    private Image towerImage;
    private boolean usingFallback = false;
    
    // Tower size - change these!
    private final int DRAW_WIDTH = 80;   // AOE towers are larger
    private final int DRAW_HEIGHT = 80;
    
    // Static image cache to prevent reloading
    private static Image cachedTowerImage = null;
    private static boolean imageLoaded = false;
    
    // Explosion animation properties
    private double explosionRadius = 0;
    private long explosionStartTime = 0;
    private final long EXPLOSION_DURATION = 400; // Longer explosion for AOE
    private Color explosionColor = new Color(255, 140, 0); // Orange
    
    public AoeTower(Vector2D position) {
        super(position, 300);
        this.damage = 25;
        this.range = 120;
        this.fireRate = 0.8;
        
        System.out.println("=== Creating AoeTower ===");
        loadImage();
    }
    
    private void loadImage() {
        // Check if image is already cached (prevents reloading every time)
        if (cachedTowerImage != null) {
            towerImage = cachedTowerImage;
            System.out.println("✓ Using cached AOE tower image");
            return;
        }
        
        // List of paths to check
        String[] imagePaths = {
            "aoe_tower.gif",
            "towers/aoe_tower.gif",
            "aoe_tower.png",
            "towers/aoe_tower.png",
            "images/aoe_tower.png",
            "images/towers/aoe_tower.gif",
            "images/towers/aoe_tower.png"
        };
        
        boolean imageFound = false;
        
        for (String path : imagePaths) {
            File file = new File(path);
            if (file.exists()) {
                System.out.println("Found AOE image at: " + file.getAbsolutePath());
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
                            System.out.println("✓ Using AOE GIF (waited " + waited + "ms): " + path);
                            System.out.println("  Image size: " + icon.getIconWidth() + "x" + icon.getIconHeight());
                            imageFound = true;
                            break;
                        }
                    } else {
                        // Load PNG/JPG (synchronous)
                        towerImage = javax.imageio.ImageIO.read(file);
                        cachedTowerImage = towerImage;
                        System.out.println("✓ Loaded AOE static image: " + path);
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
            System.out.println("No AOE tower image found, creating fallback...");
            usingFallback = true;
            createFallbackImage();
            // Cache the fallback image too
            cachedTowerImage = towerImage;
        }
        
        imageLoaded = true;
    }
    
    private void createFallbackImage() {
        System.out.println("Creating AOE fallback tower image...");
        
        // Create a proper AOE tower image
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
            DRAW_WIDTH, DRAW_HEIGHT, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        
        // Transparent background
        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, DRAW_WIDTH, DRAW_HEIGHT);
        
        // Orange tower base (larger for AOE)
        g.setColor(new Color(255, 140, 0));
        g.fillRoundRect(5, 5, DRAW_WIDTH - 10, DRAW_HEIGHT - 10, 15, 15);
        
        // Darker orange center
        g.setColor(new Color(200, 100, 0));
        g.fillOval(15, 15, DRAW_WIDTH - 30, DRAW_HEIGHT - 30);
        
        // Explosion symbol (8 rays)
        g.setColor(Color.YELLOW);
        int centerX = DRAW_WIDTH / 2;
        int centerY = DRAW_HEIGHT / 2;
        int rayLength = DRAW_WIDTH / 3;
        
        for (int i = 0; i < 8; i++) {
            double angle = (i * Math.PI / 4);
            int endX = centerX + (int)(Math.cos(angle) * rayLength);
            int endY = centerY + (int)(Math.sin(angle) * rayLength);
            g.drawLine(centerX, centerY, endX, endY);
        }
        
        // Center dot
        g.setColor(Color.RED);
        g.fillOval(centerX - 4, centerY - 4, 8, 8);
        
        // Add "AOE" text for clarity
        g.setColor(Color.WHITE);
        g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 10));
        g.drawString("AOE", centerX - 12, centerY + 25);
        
        g.dispose();
        towerImage = img;
        
        // Save it for next time
        try {
            File output = new File("aoe_tower_fallback.png");
            javax.imageio.ImageIO.write(img, "PNG", output);
            System.out.println("Saved AOE fallback image to: " + output.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("Couldn't save AOE fallback image: " + e.getMessage());
        }
    }
    
    @Override
    public void update(double deltaTime, List<Enemy> enemies) {
        double currentTime = System.currentTimeMillis() / 1000.0;
        
        // Update explosion animation
        if (explosionStartTime > 0) {
            long elapsed = System.currentTimeMillis() - explosionStartTime;
            if (elapsed > EXPLOSION_DURATION) {
                explosionStartTime = 0; // Reset explosion
                explosionRadius = 0;
            } else {
                // Calculate current explosion radius (expands then contracts)
                float progress = elapsed / (float)EXPLOSION_DURATION;
                explosionRadius = calculateExplosionRadius(progress);
            }
        }
        
        if (canShoot(currentTime) && !enemies.isEmpty()) {
            List<Enemy> targets = findTargets(enemies);
            if (!targets.isEmpty()) {
                // Damage all targets
                for (Enemy target : targets) {
                    target.takeDamage(damage);
                }
                
                // Start explosion animation
                explosionStartTime = System.currentTimeMillis();
                explosionRadius = 0;
                
                lastShotTime = currentTime;
            }
        }
    }
    
    private double calculateExplosionRadius(float progress) {
        // Expand to full range and then contract
        if (progress < 0.7f) {
            // Expanding phase
            return range * (progress / 0.7f);
        } else {
            // Contracting phase
            float contractProgress = (progress - 0.7f) / 0.3f;
            return range * (1.0f - contractProgress * 0.5f);
        }
    }
    
    private List<Enemy> findTargets(List<Enemy> enemies) {
        List<Enemy> targets = new ArrayList<>();
        for (Enemy enemy : enemies) {
            Vector2D enemyPos = enemy.getPosition();
            double distance = position.distanceTo(enemyPos);
            if (distance <= range) {
                targets.add(enemy);
            }
        }
        return targets;
    }
    
    @Override
    public void render(Graphics2D g2d) {
        // Draw explosion effect first (so it appears behind the tower)
        if (explosionStartTime > 0) {
            drawExplosionEffect(g2d);
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
        
        g2d.setColor(new Color(255, 140, 0, (int)(pulse * 255)));
        g2d.fillRect(x, y, DRAW_WIDTH, DRAW_HEIGHT);
        
        g2d.setColor(Color.WHITE);
        g2d.drawString("Loading...", x + 5, y + DRAW_HEIGHT/2);
    }
    
    private void drawExplosionEffect(Graphics2D g2d) {
        long elapsed = System.currentTimeMillis() - explosionStartTime;
        float progress = elapsed / (float)EXPLOSION_DURATION;
        float opacity = 1.0f - (progress * 0.8f);
        
        // Save original stroke and color
        java.awt.Stroke originalStroke = g2d.getStroke();
        Color originalColor = g2d.getColor();
        
        // Draw expanding shockwave ring
        if (explosionRadius > 0) {
            // Outer glow (semi-transparent)
            g2d.setColor(new Color(255, 200, 0, (int)(opacity * 80)));
            g2d.setStroke(new BasicStroke(8.0f));
            g2d.drawOval(
                (int)(position.getX() - explosionRadius),
                (int)(position.getY() - explosionRadius),
                (int)(explosionRadius * 2),
                (int)(explosionRadius * 2)
            );
            
            // Inner ring (more solid)
            g2d.setColor(new Color(255, 100, 0, (int)(opacity * 150)));
            g2d.setStroke(new BasicStroke(4.0f));
            g2d.drawOval(
                (int)(position.getX() - explosionRadius * 0.8),
                (int)(position.getY() - explosionRadius * 0.8),
                (int)(explosionRadius * 1.6),
                (int)(explosionRadius * 1.6)
            );
            
            // Central flash (pulsing effect)
            int flashSize = (int)(15 + Math.sin(progress * Math.PI * 8) * 5);
            g2d.setColor(new Color(255, 255, 200, (int)(opacity * 200)));
            g2d.fillOval(
                (int)position.getX() - flashSize/2,
                (int)position.getY() - flashSize/2,
                flashSize,
                flashSize
            );
            
            // Draw explosion particles
            drawExplosionParticles(g2d, opacity);
        }
        
        // Restore stroke and color
        g2d.setStroke(originalStroke);
        g2d.setColor(originalColor);
    }
    
    private void drawExplosionParticles(Graphics2D g2d, float opacity) {
        int particleCount = 16 + (int)(System.currentTimeMillis() % 8);
        
        for (int i = 0; i < particleCount; i++) {
            double angle = (i * Math.PI * 2 / particleCount) + 
                         (System.currentTimeMillis() / 100.0) % (Math.PI * 2);
            
            // Particles move outward
            double particleDistance = explosionRadius * (0.6 + Math.random() * 0.4);
            int particleX = (int)(position.getX() + Math.cos(angle) * particleDistance);
            int particleY = (int)(position.getY() + Math.sin(angle) * particleDistance);
            
            // Random particle size
            int particleSize = 2 + (int)(Math.random() * 4);
            
            // Random particle color (yellow/orange)
            if (Math.random() > 0.5) {
                g2d.setColor(new Color(255, 200, 0, (int)(opacity * 200)));
            } else {
                g2d.setColor(new Color(255, 100, 0, (int)(opacity * 150)));
            }
            
            g2d.fillOval(
                particleX - particleSize/2,
                particleY - particleSize/2,
                particleSize,
                particleSize
            );
        }
    }
    
    private void drawFallbackTower(Graphics2D g2d) {
        // Fallback drawing
        int drawX = (int)position.getX() - DRAW_WIDTH / 2;
        int drawY = (int)position.getY() - DRAW_HEIGHT / 2;
        
        // Orange tower base
        g2d.setColor(new Color(255, 140, 0));
        g2d.fillRoundRect(drawX, drawY, DRAW_WIDTH, DRAW_HEIGHT, 15, 15);
        
        // Darker orange center
        g2d.setColor(new Color(200, 100, 0));
        g2d.fillOval(drawX + 10, drawY + 10, DRAW_WIDTH - 20, DRAW_HEIGHT - 20);
        
        // Explosion rays
        int centerX = (int)position.getX();
        int centerY = (int)position.getY();
        int rayLength = DRAW_WIDTH / 3;
        
        g2d.setColor(Color.YELLOW);
        for (int i = 0; i < 8; i++) {
            double angle = (i * Math.PI / 4);
            int endX = centerX + (int)(Math.cos(angle) * rayLength);
            int endY = centerY + (int)(Math.sin(angle) * rayLength);
            g2d.drawLine(centerX, centerY, endX, endY);
        }
        
        // Add "AOE" text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 10));
        g2d.drawString("AOE", centerX - 12, centerY + 25);
    }
    
    private void drawLevelIndicator(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 11));
        
        // Shadow
        g2d.setColor(Color.BLACK);
        g2d.drawString("AOE " + level, (int)position.getX() - 16, (int)position.getY() + 26);
        
        // Text
        g2d.setColor(Color.WHITE);
        g2d.drawString("AOE " + level, (int)position.getX() - 15, (int)position.getY() + 25);
    }
    
    private void drawRangeCircle(Graphics2D g2d) {
        if (false) { // Change to true to debug range
            // Semi-transparent orange circle
            g2d.setColor(new Color(255, 140, 0, 30));
            g2d.fillOval(
                (int)(position.getX() - range),  // FIXED: Added parentheses
                (int)(position.getY() - range),  // FIXED: Added parentheses
                (int)(range * 2),                // FIXED: Cast to int
                (int)(range * 2)                 // FIXED: Cast to int
            );
            
            // Outline
            g2d.setColor(new Color(255, 100, 0, 100));
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
        damage += 10;
        range += 15;
        fireRate += 0.1;
        upgradeCost += 75;
        
        // Change explosion color based on level
        if (level >= 2) {
            explosionColor = new Color(255, 100, 0); // Darker orange
        }
        if (level >= 3) {
            explosionColor = new Color(255, 50, 50); // Red-orange
        }
    }
    
    // Static method to preload the image (call this at game startup)
    public static void preloadImage() {
        if (!imageLoaded) {
            System.out.println("=== Preloading AoeTower image ===");
            AoeTower temp = new AoeTower(new Vector2D(0, 0));
            System.out.println("=== AoeTower preloading complete ===");
        }
    }
}