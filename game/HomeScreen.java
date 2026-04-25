package game;

import io.GameSaveManager;
import utils.GameConstants;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class HomeScreen extends JPanel {
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private GameWindow gameWindow;
    
    // Animation variables
    private float waveOffset = 0;
    private long lastUpdateTime;
    private Timer animationTimer;
    
    public HomeScreen(CardLayout cardLayout, JPanel mainContainer, GameWindow gameWindow) {
        this.cardLayout = cardLayout;
        this.mainContainer = mainContainer;
        this.gameWindow = gameWindow;
        
        setLayout(new BorderLayout());
        setBackground(new Color(20, 20, 40));
        
        // Start animation timer
        lastUpdateTime = System.currentTimeMillis();
        animationTimer = new Timer(16, e -> {
            waveOffset += 0.05f;
            repaint();
        });
        animationTimer.start();
        
        setupUI();
    }
    
    private void setupUI() {
        // Main container with BorderLayout
        setLayout(new BorderLayout());
        
        // Top panel for title
        JPanel topPanel = createTitlePanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Center panel for buttons
        JPanel centerPanel = createButtonPanel();
        add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel for high scores
        JPanel bottomPanel = createHighScorePanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                
                // Draw animated background
                drawAnimatedBackground(g2d, getWidth(), getHeight());
                
                // Draw title
                g2d.setFont(new Font("Arial", Font.BOLD, 72));
                g2d.setColor(new Color(255, 215, 0)); // Gold color
                
                // Draw text with shadow
                g2d.setColor(Color.BLACK);
                g2d.drawString("TOWER DEFENSE", 52, 102);
                g2d.setColor(new Color(255, 215, 0));
                g2d.drawString("TOWER DEFENSE", 50, 100);
                
                // Draw subtitle
                g2d.setFont(new Font("Arial", Font.ITALIC, 24));
                g2d.setColor(Color.WHITE);
                g2d.drawString("Protect Your Kingdom!", 450, 140);
            }
        };
        
        panel.setPreferredSize(new Dimension(GameConstants.WINDOW_WIDTH, 200));
        panel.setOpaque(false);
        return panel;
    }
    
    private void drawAnimatedBackground(Graphics2D g2d, int width, int height) {
        // Draw gradient background
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(30, 30, 60),
            0, height, new Color(10, 10, 30)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);
        
        // Draw animated waves
        g2d.setColor(new Color(100, 150, 255, 50));
        for (int i = 0; i < 5; i++) {
            int y = (int)(height * 0.7 + Math.sin(waveOffset + i * 0.5) * 20);
            g2d.fillRect(0, y, width, 5);
        }
        
        // Draw tower silhouettes
        g2d.setColor(new Color(100, 100, 150, 100));
        int[] towerX = {100, 300, 500, 700, 900, 1100};
        for (int x : towerX) {
            // Tower base
            g2d.fillRect(x, height - 150, 40, 100);
            // Tower top
            g2d.fillRect(x - 10, height - 180, 60, 30);
            // Tower flag
            g2d.setColor(Color.RED);
            g2d.fillRect(x + 15, height - 220, 10, 40);
        }
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(20, 100, 20, 100);
        
        // Play Button
        JButton playButton = createStyledButton("PLAY", new Color(50, 200, 50));
        playButton.setFont(new Font("Arial", Font.BOLD, 32));
        playButton.setPreferredSize(new Dimension(300, 70));
        playButton.addActionListener(e -> {
            gameWindow.startNewGame();
            cardLayout.show(mainContainer, "GAME");
        });
        panel.add(playButton, gbc);
        
        // Continue Button (only if save exists)
        if (GameSaveManager.saveFileExists()) {
            JButton continueButton = createStyledButton("CONTINUE", new Color(50, 150, 255));
            continueButton.setFont(new Font("Arial", Font.BOLD, 28));
            continueButton.setPreferredSize(new Dimension(300, 60));
            continueButton.addActionListener(e -> {
                gameWindow.loadGame();
                cardLayout.show(mainContainer, "GAME");
            });
            panel.add(continueButton, gbc);
        }
        
        // Help Button
        JButton helpButton = createStyledButton("HELP", new Color(255, 200, 50));
        helpButton.setFont(new Font("Arial", Font.BOLD, 28));
        helpButton.setPreferredSize(new Dimension(300, 60));
        helpButton.addActionListener(e -> showHelpDialog());
        panel.add(helpButton, gbc);
        
        // Exit Button
        JButton exitButton = createStyledButton("EXIT", new Color(255, 80, 80));
        exitButton.setFont(new Font("Arial", Font.BOLD, 28));
        exitButton.setPreferredSize(new Dimension(300, 60));
        exitButton.addActionListener(e -> System.exit(0));
        panel.add(exitButton, gbc);
        
        return panel;
    }
    
    private JButton createStyledButton(String text, Color baseColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                
                // Draw button background with gradient
                GradientPaint gradient = new GradientPaint(
                    0, 0, baseColor.brighter(),
                    0, getHeight(), baseColor.darker()
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                // Draw border
                g2d.setColor(baseColor.darker().darker());
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 20, 20);
                
                // Draw text
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                
                g2d.setColor(Color.WHITE);
                g2d.drawString(getText(), x, y);
                
                g2d.dispose();
            }
        };
        
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setFont(button.getFont().deriveFont(Font.BOLD, button.getFont().getSize() + 2));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setFont(button.getFont().deriveFont(Font.BOLD, button.getFont().getSize() - 2));
            }
        });
        
        return button;
    }
    
    private JPanel createHighScorePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(30, 30, 60, 200));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Load high scores
        List<Integer> highScores = GameSaveManager.loadHighScores();
        
        JLabel titleLabel = new JLabel("HIGH SCORES", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.YELLOW);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel scoresPanel = new JPanel();
        scoresPanel.setLayout(new GridLayout(1, Math.min(10, highScores.size() + 1), 10, 0));
        scoresPanel.setOpaque(false);
        
        if (highScores.isEmpty()) {
            JLabel noScoresLabel = new JLabel("No high scores yet!", SwingConstants.CENTER);
            noScoresLabel.setFont(new Font("Arial", Font.ITALIC, 18));
            noScoresLabel.setForeground(Color.WHITE);
            scoresPanel.add(noScoresLabel);
        } else {
            // Display top 5 scores
            int displayCount = Math.min(5, highScores.size());
            for (int i = 0; i < displayCount; i++) {
                JPanel scorePanel = new JPanel();
                scorePanel.setLayout(new BorderLayout());
                scorePanel.setBackground(new Color(40, 40, 80, 200));
                scorePanel.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 2));
                
                JLabel rankLabel = new JLabel("#" + (i + 1), SwingConstants.CENTER);
                rankLabel.setFont(new Font("Arial", Font.BOLD, 20));
                rankLabel.setForeground(Color.YELLOW);
                
                JLabel scoreLabel = new JLabel(String.valueOf(highScores.get(i)), SwingConstants.CENTER);
                scoreLabel.setFont(new Font("Arial", Font.BOLD, 28));
                scoreLabel.setForeground(Color.WHITE);
                
                scorePanel.add(rankLabel, BorderLayout.NORTH);
                scorePanel.add(scoreLabel, BorderLayout.CENTER);
                
                scoresPanel.add(scorePanel);
            }
        }
        
        panel.add(scoresPanel, BorderLayout.CENTER);
        
        // Instructions label
        JLabel instructionsLabel = new JLabel(
            "<html><center>Use Mouse: Left-click to place/select towers, Right-click to cancel<br>" +
            "Hotkeys: Ctrl+S to Save, Ctrl+L to Load, ESC to return to menu</center></html>",
            SwingConstants.CENTER
        );
        instructionsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        instructionsLabel.setForeground(Color.LIGHT_GRAY);
        panel.add(instructionsLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void showHelpDialog() {
        String helpText = 
            "<html><div style='text-align: center; width: 400px;'>" +
            "<h2>Tower Defense Game - Help</h2>" +
            "<h3>Objective:</h3>" +
            "<p>Defend your kingdom from enemy waves! Don't let enemies reach the end of the path.</p>" +
            "<h3>How to Play:</h3>" +
            "<ul style='text-align: left;'>" +
            "<li><b>Gold:</b> Earn gold by defeating enemies and completing waves</li>" +
            "<li><b>Towers:</b> Place towers along the path to attack enemies</li>" +
            "<li><b>Health:</b> Lose health when enemies reach the end</li>" +
            "<li><b>Waves:</b> Survive as many waves as possible</li>" +
            "</ul>" +
            "<h3>Tower Types:</h3>" +
            "<ul style='text-align: left;'>" +
            "<li><b>Basic Tower:</b> Balanced attack, good for beginners (100 gold)</li>" +
            "<li><b>Sniper Tower:</b> Long range, high damage, slow firing (250 gold)</li>" +
            "<li><b>AOE Tower:</b> Area damage, good for groups (300 gold)</li>" +
            "</ul>" +
            "<h3>Controls:</h3>" +
            "<ul style='text-align: left;'>" +
            "<li><b>Left Click:</b> Place/Select towers</li>" +
            "<li><b>Right Click:</b> Cancel placement/Deselect</li>" +
            "<li><b>Ctrl+S:</b> Quick Save</li>" +
            "<li><b>Ctrl+L:</b> Quick Load</li>" +
            "<li><b>ESC:</b> Return to Menu</li>" +
            "</ul>" +
            "</div></html>";
        
        JOptionPane.showMessageDialog(this, helpText, "Help Guide", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Draw animated background
        drawAnimatedBackground(g2d, getWidth(), getHeight());
    }
    
    public void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }
}