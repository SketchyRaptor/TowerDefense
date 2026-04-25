// src/io/GameSave.java
package io;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

public class GameSave implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int waveNumber;
    private int playerGold;
    private int playerHealth;
    private int score;
    private List<TowerSaveData> towers;
    private long playTime;
    
    public GameSave() {
        this.towers = new ArrayList<>();
    }
    
    // Getters and setters
    public int getWaveNumber() { return waveNumber; }
    public void setWaveNumber(int waveNumber) { this.waveNumber = waveNumber; }
    
    public int getPlayerGold() { return playerGold; }
    public void setPlayerGold(int playerGold) { this.playerGold = playerGold; }
    
    public int getPlayerHealth() { return playerHealth; }
    public void setPlayerHealth(int playerHealth) { this.playerHealth = playerHealth; }
    
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    
    public List<TowerSaveData> getTowers() { return towers; }
    public void addTower(TowerSaveData tower) { towers.add(tower); }
    
    public long getPlayTime() { return playTime; }
    public void setPlayTime(long playTime) { this.playTime = playTime; }
    
    // Tower save data inner class
    public static class TowerSaveData implements Serializable {
        private String towerType;
        private int level;
        private double x;
        private double y;
        
        public TowerSaveData(String towerType, int level, double x, double y) {
            this.towerType = towerType;
            this.level = level;
            this.x = x;
            this.y = y;
        }
        
        // Getters
        public String getTowerType() { return towerType; }
        public int getLevel() { return level; }
        public double getX() { return x; }
        public double getY() { return y; }
    }
}