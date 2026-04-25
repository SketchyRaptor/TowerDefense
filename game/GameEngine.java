package game;

import io.GameSave;
import towers.TowerFactory;
import entities.Tower;
import utils.Vector2D;

public class GameEngine implements Runnable {
    private GameState gameState;
    private volatile boolean running;
    private Thread gameThread;
    
    public GameEngine() {
        this.gameState = new GameState();
        this.running = false;
    }
    
    public void start() {
        if (running) return;
        
        running = true;
        gameThread = new Thread(this, "GameEngine-Thread");
        gameThread.start();
    }
    
    @Override
    public void run() {
        long lastTime = System.nanoTime();
        final double NS_PER_UPDATE = 1000000000.0 / 60.0; // 60 updates per second
        double delta = 0;
        
        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / NS_PER_UPDATE;
            lastTime = now;
            
            // Update game logic at fixed rate
            while (delta >= 1) {
                updateGame();
                delta--;
            }
            
            // Small sleep to prevent CPU hogging
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    private void updateGame() {
        try {
            // Simple update with fixed delta time
            gameState.update(1.0 / 60.0);
        } catch (Exception e) {
            System.err.println("Error in game update: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void loadFromSave(GameSave save) {
        if (save == null) return;
        
        // Create new game state
        GameState newState = new GameState();
        newState.setWaveNumber(save.getWaveNumber());
        newState.setPlayerGold(save.getPlayerGold());
        newState.setPlayerHealth(save.getPlayerHealth());
        newState.setScore(save.getScore());
        
        // Load towers
        for (GameSave.TowerSaveData towerData : save.getTowers()) {
            Vector2D pos = new Vector2D(towerData.getX(), towerData.getY());
            Tower tower = TowerFactory.createTower(towerData.getTowerType(), pos);
            tower.setLevel(towerData.getLevel());
            newState.addTower(tower);
        }
        
        // Set new game state
        this.gameState = newState;
    }
    
    public GameState getGameState() {
        return gameState;
    }
    
    public void stop() {
        running = false;
        if (gameThread != null) {
            try {
                gameThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    public boolean isRunning() {
        return running;
    }
}