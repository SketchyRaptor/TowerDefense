package game;

import io.GameSave;
import io.GameSaveManager;
import utils.GameConstants;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameWindow extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private HomeScreen homeScreen;
    private GamePanel gamePanel;
    private GameEngine gameEngine;
    
    public GameWindow() {
        setTitle("Java Tower Defense - Ultimate Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Setup CardLayout for screen switching
        setupCardLayout();
        
        // Create menu bar
        createMenuBar();
        
        // Add ESC key listener to return to menu
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    returnToMenu();
                } else if (e.isControlDown()) {
                    if (e.getKeyCode() == KeyEvent.VK_S) {
                        if (gameEngine != null) {
                            GameSaveManager.saveGame(gameEngine.getGameState());
                            JOptionPane.showMessageDialog(GameWindow.this, 
                                "Game saved successfully!", "Save Game", 
                                JOptionPane.INFORMATION_MESSAGE);
                        }
                    } else if (e.getKeyCode() == KeyEvent.VK_L) {
                        loadGame();
                    }
                }
            }
        });
        
        setFocusable(true);
        requestFocus();
        
        // Add window listener for cleanup
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                // Window opened
            }
            
            @Override
            public void windowClosing(WindowEvent e) {
                // Save high scores and cleanup
                if (gameEngine != null) {
                    gameEngine.stop();
                }
                if (gamePanel != null) {
                    gamePanel.cleanup();
                }
                if (homeScreen != null) {
                    homeScreen.stopAnimation();
                }
                
                // Save high scores one last time
                if (gameEngine != null && gameEngine.getGameState() != null) {
                    GameSaveManager.saveHighScores(gameEngine.getGameState().getScore());
                }
            }
        });
    }
    
    private void setupCardLayout() {
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        
        // Create home screen
        homeScreen = new HomeScreen(cardLayout, mainContainer, this);
        
        // Create game panel (initially empty, will be created when game starts)
        gamePanel = new GamePanel(null);
        
        // Add screens to container
        mainContainer.add(homeScreen, "HOME");
        mainContainer.add(gamePanel, "GAME");
        
        // Show home screen first
        cardLayout.show(mainContainer, "HOME");
        
        // Add container to frame
        add(mainContainer);
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");
        
        JMenuItem newGameItem = new JMenuItem("New Game");
        JMenuItem saveGameItem = new JMenuItem("Save Game");
        JMenuItem loadGameItem = new JMenuItem("Load Game");
        JMenuItem returnToMenuItem = new JMenuItem("Return to Menu");
        JMenuItem exitItem = new JMenuItem("Exit");
        
        newGameItem.addActionListener(e -> startNewGame());
        saveGameItem.addActionListener(e -> {
            if (gameEngine != null) {
                GameSaveManager.saveGame(gameEngine.getGameState());
                JOptionPane.showMessageDialog(this, 
                    "Game saved successfully!", "Save Game", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });
        loadGameItem.addActionListener(e -> loadGame());
        returnToMenuItem.addActionListener(e -> returnToMenu());
        exitItem.addActionListener(e -> System.exit(0));
        
        // Set accelerator keys
        newGameItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        saveGameItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        loadGameItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));
        returnToMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
        
        gameMenu.add(newGameItem);
        gameMenu.add(saveGameItem);
        gameMenu.add(loadGameItem);
        gameMenu.addSeparator();
        gameMenu.add(returnToMenuItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);
        
        menuBar.add(gameMenu);
        
        // Help menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem helpItem = new JMenuItem("Show Help");
        helpItem.addActionListener(e -> showHelp());
        helpMenu.add(helpItem);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    public void startNewGame() {
        // Stop current game if running
        if (gameEngine != null) {
            gameEngine.stop();
        }
        
        // Create new game engine
        gameEngine = new GameEngine();
        gamePanel = new GamePanel(gameEngine);
        
        // Update container
        mainContainer.remove(gamePanel);
        mainContainer.add(gamePanel, "GAME");
        
        // Switch to game screen
        cardLayout.show(mainContainer, "GAME");
        
        // Start game engine
        gameEngine.start();
        
        // Request focus for key events
        gamePanel.requestFocus();
    }
    
    public void loadGame() {
        GameSave save = GameSaveManager.loadGame();
        if (save != null) {
            // Stop current game if running
            if (gameEngine != null) {
                gameEngine.stop();
            }
            
            // Create new game engine with loaded save
            gameEngine = new GameEngine();
            gameEngine.loadFromSave(save);
            gamePanel = new GamePanel(gameEngine);
            
            // Update container
            mainContainer.remove(gamePanel);
            mainContainer.add(gamePanel, "GAME");
            
            // Switch to game screen
            cardLayout.show(mainContainer, "GAME");
            
            // Start game engine
            gameEngine.start();
            
            // Request focus for key events
            gamePanel.requestFocus();
            
            JOptionPane.showMessageDialog(this, 
                "Game loaded successfully!\nWave: " + save.getWaveNumber() + 
                "\nGold: " + save.getPlayerGold() + 
                "\nHealth: " + save.getPlayerHealth(), 
                "Load Game", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void returnToMenu() {
        // Stop game engine
        if (gameEngine != null) {
            gameEngine.stop();
        }
        
        // Cleanup game panel
        if (gamePanel != null) {
            gamePanel.cleanup();
        }
        
        // Update home screen to show latest high scores
        mainContainer.remove(homeScreen);
        homeScreen = new HomeScreen(cardLayout, mainContainer, this);
        mainContainer.add(homeScreen, "HOME");
        
        // Switch to home screen
        cardLayout.show(mainContainer, "HOME");
        
        // Request focus
        homeScreen.requestFocus();
    }
    
    private void showHelp() {
        String helpText = 
            "<html><div style='text-align: center; width: 400px;'>" +
            "<h2>Quick Help</h2>" +
            "<p><b>Goal:</b> Survive endless waves of enemies!</p>" +
            "<p><b>Tip:</b> Place towers strategically at path corners.</p>" +
            "<p><b>Hotkeys:</b><br>" +
            "• ESC: Return to Menu<br>" +
            "• Ctrl+S: Quick Save<br>" +
            "• Ctrl+L: Quick Load<br>" +
            "• Ctrl+N: New Game</p>" +
            "</div></html>";
        
        JOptionPane.showMessageDialog(this, helpText, "Quick Help", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void main(String[] args) {
        // Set system properties for better performance
        System.setProperty("sun.java2d.opengl", "True");
        System.setProperty("sun.java2d.d3d", "False");
        
        // Run on EDT
        SwingUtilities.invokeLater(() -> {
            try {
                GameWindow window = new GameWindow();
                window.setVisible(true);
                System.out.println("Tower Defense Ultimate Edition Started!");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Error starting game: " + e.getMessage(),
                    "Startup Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}