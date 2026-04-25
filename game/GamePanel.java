package game;

import entities.Tower;
import entities.Enemy;
import towers.TowerFactory;
import utils.Vector2D;
import utils.GameConstants;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class GamePanel extends JPanel {
    private GameEngine gameEngine;
    private Tower selectedTower;
    private boolean placingTower;
    private String towerTypeToPlace;
    private Point mousePosition;
    
    // Double buffering
    private BufferedImage buffer;
    private Graphics2D bufferGraphics;
    
    // Background image
    private Image backgroundImage;
    private boolean hasBackground = false;
    
    // Rendering optimization
    private long lastRenderTime;
    private final long TARGET_FRAME_TIME = 1000000000L / 60; // 60 FPS target
    private boolean isRendering = false;
    
    // UI Components
    private JButton upgradeButton;
    private JButton sellButton;
    private JButton pauseButton;
    private JLabel towerInfoLabel;
    
    // Colors for buttons (for easy maintenance)
    private final Color UPGRADE_ENABLED_COLOR = new Color(50, 200, 50);   // Bright green
    private final Color UPGRADE_DISABLED_COLOR = new Color(30, 100, 30);  // Dark green
    private final Color UPGRADE_CANNOT_AFFORD = new Color(200, 50, 50);   // Red
    private final Color SELL_COLOR = new Color(220, 80, 80);             // Red
    private final Color PAUSE_COLOR = new Color(80, 80, 200);            // Blue
    private final Color RESUME_COLOR = new Color(200, 180, 50);          // Yellow/Orange
    private final Color BUTTON_TEXT_COLOR = Color.BLACK;                 // White text
    
    public GamePanel(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
        this.selectedTower = null;
        this.placingTower = false;
        this.mousePosition = new Point(0, 0);
        this.lastRenderTime = System.nanoTime();
        
        setPreferredSize(new Dimension(
            GameConstants.WINDOW_WIDTH, 
            GameConstants.WINDOW_HEIGHT
        ));
        setBackground(Color.DARK_GRAY);
        
        // Initialize double buffer
        buffer = new BufferedImage(
            GameConstants.WINDOW_WIDTH, 
            GameConstants.WINDOW_HEIGHT, 
            BufferedImage.TYPE_INT_ARGB
        );
        bufferGraphics = buffer.createGraphics();
        
        // Setup rendering hints for better performance
        setupRenderingHints();
        
        // Setup UI components
        setupUI();
        
        // Add mouse listeners
        addMouseListeners();
        
        // Load background image
        loadBackgroundImage();
        
        // Start continuous rendering
        startRenderingThread();
    }
    
    private void loadBackgroundImage() {
        // List of possible background image paths
        String[] imagePaths = {
            "background.jpg",
            "background.png",
            "images/background.jpg",
            "images/background.png",
            "bg.jpg",
            "bg.png"
        };
        
        for (String path : imagePaths) {
            File file = new File(path);
            if (file.exists()) {
                try {
                    backgroundImage = ImageIO.read(file);
                    if (backgroundImage != null) {
                        System.out.println("✓ Loaded background image: " + path);
                        hasBackground = true;
                        
                        // Scale background to match game window size if needed
                        int width = backgroundImage.getWidth(null);
                        int height = backgroundImage.getHeight(null);
                        
                        if (width != GameConstants.WINDOW_WIDTH || height != GameConstants.WINDOW_HEIGHT) {
                            backgroundImage = backgroundImage.getScaledInstance(
                                GameConstants.WINDOW_WIDTH, 
                                GameConstants.WINDOW_HEIGHT, 
                                Image.SCALE_SMOOTH
                            );
                            System.out.println("  Scaled background to: " + 
                                GameConstants.WINDOW_WIDTH + "x" + GameConstants.WINDOW_HEIGHT);
                        }
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("✗ Error loading background " + path + ": " + e.getMessage());
                }
            }
        }
        
        if (!hasBackground) {
            System.out.println("No background image found. Using default dark gray background.");
        }
    }
    
    private void setupRenderingHints() {
        bufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                      RenderingHints.VALUE_ANTIALIAS_ON);
        bufferGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, 
                                      RenderingHints.VALUE_RENDER_SPEED);
        bufferGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                      RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Create side panel with tower selection and actions
        JPanel sidePanel = createSidePanel();
        add(sidePanel, BorderLayout.EAST);
    }
    
    private JPanel createSidePanel() {
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setPreferredSize(new Dimension(180, 0));
        sidePanel.setBackground(new Color(40, 40, 60, 200)); // Semi-transparent
        sidePanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        
        // Tower type selection
        JLabel towerSelectLabel = new JLabel("TOWER TYPES");
        towerSelectLabel.setForeground(Color.YELLOW);
        towerSelectLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        towerSelectLabel.setFont(new Font("Arial", Font.BOLD, 16));
        sidePanel.add(towerSelectLabel);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        String[] towerTypes = {"Basic Tower", "Sniper Tower", "AOE Tower"};
        int[] towerCosts = {100, 250, 300};
        Color[] towerColors = {
            new Color(100, 149, 237),  // Basic - Cornflower blue
            new Color(34, 139, 34),    // Sniper - Forest green
            new Color(255, 140, 0)     // AOE - Dark orange
        };
        
        for (int i = 0; i < towerTypes.length; i++) {
            JButton btn = createTowerButton(towerTypes[i], towerCosts[i], towerColors[i]);
            sidePanel.add(btn);
            sidePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        
        sidePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Selected tower info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(50, 50, 80, 200)); // Semi-transparent
        infoPanel.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 1));
        infoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel selectedLabel = new JLabel("SELECTED TOWER");
        selectedLabel.setForeground(Color.YELLOW);
        selectedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        selectedLabel.setFont(new Font("Arial", Font.BOLD, 14));
        infoPanel.add(selectedLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        towerInfoLabel = new JLabel("No tower selected");
        towerInfoLabel.setForeground(Color.WHITE);
        towerInfoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        towerInfoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        infoPanel.add(towerInfoLabel);
        
        infoPanel.setMaximumSize(new Dimension(160, 80));
        sidePanel.add(infoPanel);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Upgrade button
        upgradeButton = new JButton("UPGRADE");
        styleActionButton(upgradeButton, UPGRADE_DISABLED_COLOR);
        upgradeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        upgradeButton.setMaximumSize(new Dimension(150, 35));
        upgradeButton.setEnabled(false);
        upgradeButton.addActionListener(e -> upgradeSelectedTower());
        sidePanel.add(upgradeButton);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        // Sell button
        sellButton = new JButton("SELL");
        styleActionButton(sellButton, SELL_COLOR);
        sellButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        sellButton.setMaximumSize(new Dimension(150, 35));
        sellButton.setEnabled(false);
        sellButton.addActionListener(e -> sellSelectedTower());
        sidePanel.add(sellButton);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Pause button
        pauseButton = new JButton("PAUSE");
        styleActionButton(pauseButton, PAUSE_COLOR);
        pauseButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        pauseButton.setMaximumSize(new Dimension(150, 35));
        pauseButton.addActionListener(e -> togglePause());
        sidePanel.add(pauseButton);
        
        // Game stats at bottom
        sidePanel.add(Box.createVerticalGlue());
        addGameStats(sidePanel);
        
        return sidePanel;
    }
    
    private JButton createTowerButton(String type, int cost, Color towerColor) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                
                // Draw button background with gradient
                GradientPaint gradient = new GradientPaint(
                    0, 0, towerColor.brighter(),
                    0, getHeight(), towerColor.darker()
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                
                // Draw border
                g2d.setColor(towerColor.darker().darker());
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
                
                // Draw text with shadow for readability
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                String[] lines = {
                    type,
                    "Cost: " + cost + " Gold"
                };
                
                // Draw text shadow (black)
                g2d.setColor(Color.BLACK);
                for (int i = 0; i < lines.length; i++) {
                    FontMetrics fm = g2d.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(lines[i])) / 2;
                    int y = getHeight() / 3 + (i * 20) + fm.getAscent();
                    g2d.drawString(lines[i], x + 1, y + 1);
                }
                
                // Draw main text (white)
                g2d.setColor(Color.WHITE);
                for (int i = 0; i < lines.length; i++) {
                    FontMetrics fm = g2d.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(lines[i])) / 2;
                    int y = getHeight() / 3 + (i * 20) + fm.getAscent();
                    g2d.drawString(lines[i], x, y);
                }
                
                g2d.dispose();
            }
        };
        
        btn.setPreferredSize(new Dimension(160, 60));
        btn.setMaximumSize(new Dimension(160, 60));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setToolTipText(getTowerDescription(type));
            }
        });
        
        btn.addActionListener(e -> {
            towerTypeToPlace = type;
            placingTower = true;
            selectedTower = null;
            updateButtonStates();
        });
        
        return btn;
    }
    
    private String getTowerDescription(String towerType) {
        switch(towerType) {
            case "Basic Tower":
                return "Balanced tower with medium range and damage";
            case "Sniper Tower":
                return "Long range, high damage, but slow firing rate";
            case "AOE Tower":
                return "Area damage, good against groups of enemies";
            default:
                return "Unknown tower type";
        }
    }
    
    private void styleActionButton(JButton button, Color backgroundColor) {
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(BUTTON_TEXT_COLOR); // Always white text
        button.setBackground(backgroundColor);
        button.setOpaque(true);
        button.setBorderPainted(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(backgroundColor.darker(), 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(backgroundColor.brighter());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(backgroundColor);
            }
        });
    }
    
    private void addGameStats(JPanel panel) {
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBackground(new Color(30, 30, 50, 200)); // Semi-transparent
        statsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        statsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsPanel.setMaximumSize(new Dimension(160, 80));
        
        JLabel statsLabel = new JLabel("GAME STATS");
        statsLabel.setForeground(Color.CYAN);
        statsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsLabel.setFont(new Font("Arial", Font.BOLD, 12));
        statsPanel.add(statsLabel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        JLabel hotkeysLabel = new JLabel("<html><center>"
            + "Hotkeys:<br>"
            + "• ESC: Menu<br>"
            + "• Ctrl+S: Save<br>"
            + "• Ctrl+L: Load<br>"
            + "</center></html>");
        hotkeysLabel.setForeground(Color.LIGHT_GRAY);
        hotkeysLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        hotkeysLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        statsPanel.add(hotkeysLabel);
        
        panel.add(statsPanel);
    }
    
    private void addMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e);
            }
        });
        
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mousePosition = e.getPoint();
                if (placingTower) {
                    repaint();
                }
            }
        });
    }
    
    private void startRenderingThread() {
        Thread renderThread = new Thread(() -> {
            while (true) {
                try {
                    long currentTime = System.nanoTime();
                    long elapsed = currentTime - lastRenderTime;
                    
                    if (elapsed >= TARGET_FRAME_TIME) {
                        repaint();
                        lastRenderTime = currentTime;
                        
                        // Calculate sleep time to maintain target FPS
                        long sleepTime = (TARGET_FRAME_TIME - (System.nanoTime() - currentTime)) / 1000000;
                        if (sleepTime > 0) {
                            Thread.sleep(sleepTime);
                        }
                    } else {
                        Thread.sleep(1);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "Rendering-Thread");
        renderThread.setDaemon(true);
        renderThread.start();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (isRendering) return;
        isRendering = true;
        
        try {
            // Clear buffer
            if (hasBackground && backgroundImage != null) {
                // Draw background image
                bufferGraphics.drawImage(backgroundImage, 0, 0, this);
            } else {
                // Fallback to solid color
                bufferGraphics.setColor(Color.DARK_GRAY);
                bufferGraphics.fillRect(0, 0, getWidth(), getHeight());
            }
            
            // Draw game elements to buffer
            drawGameToBuffer();
            
            // Draw buffer to screen
            g.drawImage(buffer, 0, 0, this);
        } finally {
            isRendering = false;
        }
    }
    
    private void drawGameToBuffer() {
        if (gameEngine == null || gameEngine.getGameState() == null) return;
        
        GameState state = gameEngine.getGameState();
        
        // Draw grid with transparency if we have a background
        if (hasBackground) {
            drawGrid(bufferGraphics, new Color(50, 50, 50, 80)); // Semi-transparent grid
        } else {
            drawGrid(bufferGraphics, new Color(50, 50, 50)); // Solid grid
        }
        
        // Draw path
        state.getPath().render(bufferGraphics);
        
        // Draw towers
        for (Tower tower : state.getTowers()) {
            // Draw selected tower with highlight
            if (tower == selectedTower) {
                drawTowerHighlight(bufferGraphics, tower);
            }
            tower.render(bufferGraphics);
        }
        
        // Draw enemies
        for (Enemy enemy : state.getEnemies()) {
            enemy.render(bufferGraphics);
        }
        
        // Draw UI
        drawUI(bufferGraphics);
        
        // Draw tower placement preview
        if (placingTower) {
            drawTowerPlacementPreview(bufferGraphics);
        }
    }
    
    private void drawTowerHighlight(Graphics2D g2d, Tower tower) {
        // Draw a yellow highlight around selected tower
        g2d.setColor(Color.YELLOW);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(
            (int)tower.getPosition().getX() - 20,
            (int)tower.getPosition().getY() - 20,
            40, 40
        );
        
        // Draw sell value info
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        int sellValue = calculateSellValue(tower);
        g2d.drawString("Sell: " + sellValue + "G",
            (int)tower.getPosition().getX() - 20,
            (int)tower.getPosition().getY() - 25);
    }
    
    private int calculateSellValue(Tower tower) {
        int baseValue = tower.getCost();
        int upgradeValue = 0;
        
        // Calculate value from upgrades
        for (int i = 1; i < tower.getLevel(); i++) {
            upgradeValue += (baseValue / 2) * 0.5;
        }
        
        return (int)(baseValue * 0.7) + upgradeValue;
    }
    
    private void drawGrid(Graphics2D g2d, Color gridColor) {
        g2d.setColor(gridColor);
        for (int x = 0; x < getWidth(); x += GameConstants.GRID_SIZE) {
            g2d.drawLine(x, 0, x, getHeight());
        }
        for (int y = 0; y < getHeight(); y += GameConstants.GRID_SIZE) {
            g2d.drawLine(0, y, getWidth(), y);
        }
    }
    
    private void drawUI(Graphics2D g2d) {
        GameState state = gameEngine.getGameState();
        
        // Draw semi-transparent UI background
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(5, 5, 200, 140);
        
        // Draw border around UI
        g2d.setColor(Color.YELLOW);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(5, 5, 200, 140);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        
        g2d.drawString("Wave: " + state.getWaveNumber(), 10, 25);
        g2d.drawString("Gold: " + state.getPlayerGold(), 10, 50);
        g2d.drawString("Health: " + state.getPlayerHealth(), 10, 75);
        g2d.drawString("Score: " + state.getScore(), 10, 100);
        
        // Draw high score
        g2d.setColor(Color.CYAN);
        g2d.drawString("High: " + getHighestScore(), 10, 125);
        
        if (state.isPaused()) {
            g2d.setColor(new Color(255, 255, 0, 200)); // Semi-transparent yellow
            g2d.fillRect(getWidth()/2 - 100, getHeight()/2 - 50, 200, 100);
            
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            g2d.drawString("PAUSED", getWidth() / 2 - 50, getHeight() / 2);
        }
        
        if (state.isGameOver()) {
            g2d.setColor(new Color(255, 0, 0, 200)); // Semi-transparent red
            g2d.fillRect(getWidth()/2 - 200, getHeight()/2 - 75, 400, 150);
            
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            g2d.drawString("GAME OVER", getWidth() / 2 - 150, getHeight() / 2);
            
            // Draw final score
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 32));
            g2d.drawString("Final Score: " + state.getScore(), 
                getWidth() / 2 - 120, getHeight() / 2 + 50);
        }
    }
    
    private int getHighestScore() {
        // This would come from your GameSaveManager
        // For now, return a placeholder
        return 0;
    }
    
    private void drawTowerPlacementPreview(Graphics2D g2d) {
        // Draw range indicator
        g2d.setColor(new Color(0, 255, 0, 30));
        int range = getTowerRange(towerTypeToPlace);
        g2d.fillOval(mousePosition.x - range, mousePosition.y - range, range * 2, range * 2);
        
        // Draw preview tower
        Color towerColor = getTowerColor(towerTypeToPlace);
        g2d.setColor(new Color(towerColor.getRed(), towerColor.getGreen(), towerColor.getBlue(), 150));
        g2d.fillRect(mousePosition.x - 15, mousePosition.y - 15, 30, 30);
        
        // Draw tower cost
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        int cost = getTowerCost(towerTypeToPlace);
        g2d.drawString("Cost: " + cost + "G", mousePosition.x - 25, mousePosition.y - 80);
        
        // Draw placement instructions
        g2d.setColor(Color.YELLOW);
        g2d.drawString("Click to place", mousePosition.x - 30, mousePosition.y + 40);
        g2d.drawString("Right-click to cancel", mousePosition.x - 45, mousePosition.y + 60);
    }
    
    private Color getTowerColor(String towerType) {
        switch(towerType) {
            case "Basic Tower": return new Color(100, 149, 237);
            case "Sniper Tower": return new Color(34, 139, 34);
            case "AOE Tower": return new Color(255, 140, 0);
            default: return Color.GRAY;
        }
    }
    
    private int getTowerCost(String towerType) {
        switch(towerType) {
            case "Basic Tower": return 100;
            case "Sniper Tower": return 250;
            case "AOE Tower": return 300;
            default: return 0;
        }
    }
    
    private int getTowerRange(String towerType) {
        switch(towerType) {
            case "Basic Tower": return 150;
            case "Sniper Tower": return 300;
            case "AOE Tower": return 120;
            default: return 0;
        }
    }
    
    private void handleMouseClick(MouseEvent e) {
        if (placingTower && e.getButton() == MouseEvent.BUTTON1) {
            // Place new tower
            Vector2D pos = new Vector2D(e.getX(), e.getY());
            Tower tower = TowerFactory.createTower(towerTypeToPlace, pos);
            
            boolean success = gameEngine.getGameState().placeTower(tower);
            if (success) {
                placingTower = false;
                selectedTower = tower;
                updateTowerInfo();
                updateButtonStates();
            } else {
                // Show error message
                JOptionPane.showMessageDialog(this,
                    "Cannot place tower here!\n" +
                    "• Check if you have enough gold\n" +
                    "• Make sure it's not on the path\n" +
                    "• Keep distance from other towers",
                    "Placement Error", JOptionPane.WARNING_MESSAGE);
            }
        } else if (e.getButton() == MouseEvent.BUTTON1) {
            // Select existing tower
            selectTowerAt(e.getX(), e.getY());
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            // Right click: cancel placement or deselect
            placingTower = false;
            selectedTower = null;
            updateButtonStates();
            updateTowerInfo();
        }
    }
    
    private void selectTowerAt(int x, int y) {
        selectedTower = null;
        
        for (Tower tower : gameEngine.getGameState().getTowers()) {
            Vector2D towerPos = tower.getPosition();
            double distance = Math.sqrt(
                Math.pow(towerPos.getX() - x, 2) + 
                Math.pow(towerPos.getY() - y, 2)
            );
            
            if (distance < 30) { // Click radius for tower selection
                selectedTower = tower;
                break;
            }
        }
        
        updateTowerInfo();
        updateButtonStates();
        repaint();
    }
    
    // FIXED: Update button states with proper text visibility
    private void updateButtonStates() {
        boolean hasSelectedTower = (selectedTower != null);
        
        // Enable/disable buttons
        upgradeButton.setEnabled(hasSelectedTower);
        sellButton.setEnabled(hasSelectedTower);
        
        if (hasSelectedTower) {
            // Set button text with HTML for better formatting
            String upgradeText = "<html><center>UPGRADE<br><font size='-1'>(" + 
                                selectedTower.getUpgradeCost() + "G)</font></center></html>";
            String sellText = "<html><center>SELL<br><font size='-1'>(" + 
                             calculateSellValue(selectedTower) + "G)</font></center></html>";
            
            upgradeButton.setText(upgradeText);
            sellButton.setText(sellText);
            
            // Set button colors based on affordability
            GameState state = gameEngine.getGameState();
            if (state.getPlayerGold() >= selectedTower.getUpgradeCost()) {
                upgradeButton.setBackground(UPGRADE_ENABLED_COLOR);
                upgradeButton.setForeground(BUTTON_TEXT_COLOR); // White text
            } else {
                upgradeButton.setBackground(UPGRADE_CANNOT_AFFORD);
                upgradeButton.setForeground(BUTTON_TEXT_COLOR); // White text
            }
            
            // Always set sell button color
            sellButton.setBackground(SELL_COLOR);
            sellButton.setForeground(BUTTON_TEXT_COLOR); // White text
            
        } else {
            // No tower selected
            upgradeButton.setText("UPGRADE");
            sellButton.setText("SELL");
            
            upgradeButton.setBackground(UPGRADE_DISABLED_COLOR);
            upgradeButton.setForeground(BUTTON_TEXT_COLOR); // White text
            
            sellButton.setBackground(SELL_COLOR);
            sellButton.setForeground(BUTTON_TEXT_COLOR); // White text
        }
        
        // Ensure button text is visible by forcing repaint
        upgradeButton.repaint();
        sellButton.repaint();
    }
    
    // FIXED: Toggle pause with proper button colors
    private void togglePause() {
        if (gameEngine == null || gameEngine.getGameState() == null) return;
        
        GameState state = gameEngine.getGameState();
        boolean isPaused = state.isPaused();
        state.setPaused(!isPaused);
        
        if (!isPaused) {
            // Game is now paused
            pauseButton.setText("RESUME");
            pauseButton.setBackground(RESUME_COLOR);
            pauseButton.setForeground(Color.BLACK); // Black text for yellow background
            
            // Disable other buttons when paused
            upgradeButton.setEnabled(false);
            sellButton.setEnabled(false);
        } else {
            // Game is now resumed
            pauseButton.setText("PAUSE");
            pauseButton.setBackground(PAUSE_COLOR);
            pauseButton.setForeground(BUTTON_TEXT_COLOR); // White text for blue background
            
            // Re-enable buttons if a tower is selected
            if (selectedTower != null) {
                upgradeButton.setEnabled(true);
                sellButton.setEnabled(true);
                updateButtonStates(); // Update colors for re-enabled buttons
            }
        }
        
        repaint();
    }
    
    private void updateTowerInfo() {
        if (selectedTower == null) {
            towerInfoLabel.setText("No tower selected");
            towerInfoLabel.setForeground(Color.LIGHT_GRAY);
        } else {
            String towerType = selectedTower.getClass().getSimpleName();
            String level = "Level: " + selectedTower.getLevel();
            String damage = "Damage: " + selectedTower.getDamage();
            String range = "Range: " + (int)selectedTower.getRange();
            String sellValue = "Sell: " + calculateSellValue(selectedTower) + "G";
            
            towerInfoLabel.setText("<html><center><b>" + towerType + "</b><br>" +
                                  level + "<br>" + 
                                  damage + "<br>" + 
                                  range + "<br>" +
                                  sellValue + "</center></html>");
            towerInfoLabel.setForeground(Color.WHITE);
        }
    }
    
    private void upgradeSelectedTower() {
        if (selectedTower != null) {
            boolean success = gameEngine.getGameState().upgradeTower(selectedTower);
            if (success) {
                updateTowerInfo();
                updateButtonStates();
                repaint();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Not enough gold to upgrade!\n" +
                    "Need: " + selectedTower.getUpgradeCost() + " Gold\n" +
                    "Have: " + gameEngine.getGameState().getPlayerGold() + " Gold",
                    "Upgrade Failed", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    private void sellSelectedTower() {
        if (selectedTower != null) {
            int sellValue = calculateSellValue(selectedTower);
            int confirm = JOptionPane.showConfirmDialog(this,
                "Sell this tower for " + sellValue + " Gold?",
                "Confirm Sell", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = gameEngine.getGameState().sellTower(selectedTower);
                if (success) {
                    selectedTower = null;
                    updateTowerInfo();
                    updateButtonStates();
                    repaint();
                }
            }
        }
    }
    
    public void setGameEngine(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }
    
    // Method to manually set a background image (optional)
    public void setBackgroundImage(Image image) {
        this.backgroundImage = image;
        this.hasBackground = (image != null);
        if (hasBackground && image.getWidth(null) != GameConstants.WINDOW_WIDTH || 
            image.getHeight(null) != GameConstants.WINDOW_HEIGHT) {
            backgroundImage = backgroundImage.getScaledInstance(
                GameConstants.WINDOW_WIDTH, 
                GameConstants.WINDOW_HEIGHT, 
                Image.SCALE_SMOOTH
            );
        }
        repaint();
    }
    
    // Method to load background image from file path (optional)
    public boolean loadBackgroundImage(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                backgroundImage = ImageIO.read(file);
                hasBackground = (backgroundImage != null);
                if (hasBackground) {
                    backgroundImage = backgroundImage.getScaledInstance(
                        GameConstants.WINDOW_WIDTH, 
                        GameConstants.WINDOW_HEIGHT, 
                        Image.SCALE_SMOOTH
                    );
                    System.out.println("✓ Loaded background image: " + path);
                }
                repaint();
                return hasBackground;
            }
        } catch (Exception e) {
            System.out.println("✗ Error loading background image: " + e.getMessage());
        }
        return false;
    }
    
    public void cleanup() {
        if (bufferGraphics != null) {
            bufferGraphics.dispose();
        }
    }
}