package game;

import entities.Tower;
import entities.Enemy;
import entities.Path;
import utils.WaveManager;
import utils.Vector2D;
import utils.GameConstants;
import io.GameSaveManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int playerGold;
    private int playerHealth;
    private int score;
    private int waveNumber;
    private long gameStartTime;
    
    private List<Tower> towers;
    private List<Enemy> enemies;
    private Path path;
    private WaveManager waveManager;
    
    private boolean isPaused;
    private boolean isGameOver;
    
    // Track if high score was saved to prevent duplicate saves
    private boolean highScoreSaved = false;
    
    public GameState() {
        this.playerGold = GameConstants.INITIAL_GOLD;
        this.playerHealth = GameConstants.INITIAL_HEALTH;
        this.score = 0;
        this.waveNumber = 1;
        this.gameStartTime = System.currentTimeMillis();
        
        this.towers = new CopyOnWriteArrayList<>();
        this.enemies = new CopyOnWriteArrayList<>();
        this.path = createDefaultPath();
        this.waveManager = new WaveManager();
        
        this.isPaused = false;
        this.isGameOver = false;
        this.highScoreSaved = false;
        
        // Spawn initial wave
        spawnInitialWave();
    }
    
    private void spawnInitialWave() {
        List<Enemy> newWave = waveManager.generateWave(waveNumber, path.getStartPoint());
        enemies.addAll(newWave);
    }
    
    private Path createDefaultPath() {
        List<Vector2D> pathPoints = new ArrayList<>();
        pathPoints.add(new Vector2D(-50, 300));
        pathPoints.add(new Vector2D(200, 300));
        pathPoints.add(new Vector2D(200, 500));
        pathPoints.add(new Vector2D(400, 500));
        pathPoints.add(new Vector2D(400, 200));
        pathPoints.add(new Vector2D(600, 200));
        pathPoints.add(new Vector2D(600, 400));
        pathPoints.add(new Vector2D(800, 400));
        pathPoints.add(new Vector2D(800, 100));
        pathPoints.add(new Vector2D(1000, 100));
        pathPoints.add(new Vector2D(1000, 300));
        pathPoints.add(new Vector2D(1250, 300));
        return new Path(pathPoints);
    }
    
    public void update(double deltaTime) {
        if (isPaused || isGameOver) return;
        
        // Update enemies
        List<Enemy> enemiesToRemove = new ArrayList<>();
        for (Enemy enemy : enemies) {
            enemy.update(deltaTime, path.getPathPoints());
            
            if (enemy.isDead()) {
                playerGold += enemy.getReward();
                score += enemy.getReward() * 10;
                enemiesToRemove.add(enemy);
            } else if (enemy.hasReachedEnd(path.getPathPoints())) {
                playerHealth -= 10;
                enemiesToRemove.add(enemy);
                
                if (playerHealth <= 0) {
                    playerHealth = 0;
                    gameOver();
                }
            }
        }
        enemies.removeAll(enemiesToRemove);
        
        // Update towers
        for (Tower tower : towers) {
            tower.update(deltaTime, new ArrayList<>(enemies));
        }
        
        // Spawn new wave if needed
        if (enemies.isEmpty()) {
            waveNumber++;
            List<Enemy> newWave = waveManager.generateWave(waveNumber, path.getStartPoint());
            enemies.addAll(newWave);
            playerGold += waveNumber * 25;
        }
        
        // Auto-save high score every 10000 points
        if (score > 0 && score % 10000 == 0 && !highScoreSaved) {
            saveHighScore();
            highScoreSaved = true;
        } else if (score % 10000 != 0) {
            highScoreSaved = false;
        }
    }
    
    public boolean placeTower(Tower tower) {
        if (playerGold < tower.getCost()) {
            return false;
        }
        
        // Simple placement check
        for (Tower existingTower : towers) {
            if (tower.getPosition().distanceTo(existingTower.getPosition()) < 60) {
                return false;
            }
        }
        
        towers.add(tower);
        playerGold -= tower.getCost();
        return true;
    }
    
    public boolean upgradeTower(Tower tower) {
        if (playerGold < tower.getUpgradeCost()) {
            return false;
        }
        
        tower.upgrade();
        playerGold -= tower.getUpgradeCost();
        return true;
    }
    
    public boolean sellTower(Tower tower) {
        if (!towers.contains(tower)) {
            return false;
        }
        
        int sellValue = calculateSellValue(tower);
        towers.remove(tower);
        playerGold += sellValue;
        return true;
    }
    
    private int calculateSellValue(Tower tower) {
        int baseValue = tower.getCost();
        int upgradeValue = 0;
        
        for (int i = 1; i < tower.getLevel(); i++) {
            upgradeValue += (baseValue / 2) * 0.5;
        }
        
        return (int)(baseValue * 0.7) + upgradeValue;
    }
    
    private void gameOver() {
        isGameOver = true;
        saveHighScore();
    }
    
    // NEW: Separate method for saving high score
    private void saveHighScore() {
        if (score > 0) {
            GameSaveManager.saveHighScores(score);
            System.out.println("High score saved: " + score);
        }
    }
    
    // Getters
    public int getPlayerGold() { return playerGold; }
    public int getPlayerHealth() { return playerHealth; }
    public int getScore() { return score; }
    public int getWaveNumber() { return waveNumber; }
    public long getGameStartTime() { return gameStartTime; }
    public List<Tower> getTowers() { return new ArrayList<>(towers); }
    public List<Enemy> getEnemies() { return new ArrayList<>(enemies); }
    public Path getPath() { return path; }
    public boolean isPaused() { return isPaused; }
    public boolean isGameOver() { return isGameOver; }
    
    // Setters
    public void setWaveNumber(int waveNumber) { this.waveNumber = waveNumber; }
    public void setPlayerGold(int playerGold) { this.playerGold = playerGold; }
    public void setPlayerHealth(int playerHealth) { this.playerHealth = playerHealth; }
    public void setScore(int score) { this.score = score; }
    public void setPaused(boolean paused) { isPaused = paused; }
    public void addGold(int amount) { playerGold += amount; }
    public void addTower(Tower tower) { towers.add(tower); }
    public void clearEnemies() { enemies.clear(); }
    public void clearTowers() { towers.clear(); }
    
    public void reset() {
        playerGold = GameConstants.INITIAL_GOLD;
        playerHealth = GameConstants.INITIAL_HEALTH;
        score = 0;
        waveNumber = 1;
        gameStartTime = System.currentTimeMillis();
        
        towers.clear();
        enemies.clear();
        waveManager = new WaveManager();
        
        isPaused = false;
        isGameOver = false;
        highScoreSaved = false;
        
        spawnInitialWave();
    }
}