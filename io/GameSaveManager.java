package io;

import game.GameState;
import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GameSaveManager {
    private static final String SAVE_FILE = "tower_defense_save.dat";
    private static final String HIGH_SCORES_FILE = "tower_defense_highscores.txt";
    private static final int MAX_HIGH_SCORES = 10;
    
    public static boolean saveFileExists() {
        return new File(SAVE_FILE).exists();
    }
    
    public static boolean saveGame(GameState gameState) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
             new GZIPOutputStream(
             new FileOutputStream(SAVE_FILE)))) {
            
            GameSave save = new GameSave();
            save.setWaveNumber(gameState.getWaveNumber());
            save.setPlayerGold(gameState.getPlayerGold());
            save.setPlayerHealth(gameState.getPlayerHealth());
            save.setScore(gameState.getScore());
            save.setPlayTime(System.currentTimeMillis() - gameState.getGameStartTime());
            
            // Save tower data
            gameState.getTowers().forEach(tower -> {
                save.addTower(new GameSave.TowerSaveData(
                    tower.getClass().getSimpleName(),
                    tower.getLevel(),
                    tower.getPosition().getX(),
                    tower.getPosition().getY()
                ));
            });
            
            oos.writeObject(save);
            System.out.println("Game saved successfully!");
            return true;
            
        } catch (IOException e) {
            System.err.println("Error saving game: " + e.getMessage());
            return false;
        }
    }
    
    public static GameSave loadGame() {
        File saveFile = new File(SAVE_FILE);
        if (!saveFile.exists()) {
            System.out.println("No save file found.");
            return null;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(
             new GZIPInputStream(
             new FileInputStream(SAVE_FILE)))) {
            
            GameSave save = (GameSave) ois.readObject();
            System.out.println("Game loaded successfully!");
            System.out.println("Wave: " + save.getWaveNumber() + 
                             ", Gold: " + save.getPlayerGold() +
                             ", Health: " + save.getPlayerHealth());
            return save;
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading game: " + e.getMessage());
            return null;
        }
    }
    
    // NEW: Save high score (persistent even after game closes)
    public static void saveHighScores(int newScore) {
        List<Integer> highScores = loadHighScores();
        highScores.add(newScore);
        
        // Sort in descending order
        highScores.sort((a, b) -> b - a);
        
        // Keep only top MAX_HIGH_SCORES
        if (highScores.size() > MAX_HIGH_SCORES) {
            highScores = highScores.subList(0, MAX_HIGH_SCORES);
        }
        
        // Save to text file for persistence
        try (PrintWriter writer = new PrintWriter(new FileWriter(HIGH_SCORES_FILE))) {
            for (Integer score : highScores) {
                writer.println(score);
            }
            System.out.println("High scores saved: " + highScores);
        } catch (IOException e) {
            System.err.println("Error saving high scores: " + e.getMessage());
        }
    }
    
    // NEW: Load high scores from persistent storage
    public static List<Integer> loadHighScores() {
        List<Integer> highScores = new ArrayList<>();
        File file = new File(HIGH_SCORES_FILE);
        
        if (!file.exists()) {
            // Create default high scores if file doesn't exist
            highScores.add(10000);
            highScores.add(7500);
            highScores.add(5000);
            highScores.add(3000);
            highScores.add(1000);
            return highScores;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(HIGH_SCORES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    highScores.add(Integer.parseInt(line.trim()));
                } catch (NumberFormatException e) {
                    System.err.println("Invalid score in file: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading high scores: " + e.getMessage());
        }
        
        // Sort in descending order
        highScores.sort((a, b) -> b - a);
        
        // Ensure we have at least MAX_HIGH_SCORES entries
        while (highScores.size() < MAX_HIGH_SCORES) {
            highScores.add(0);
        }
        
        return highScores;
    }
    
    // NEW: Save high scores list (for backward compatibility)
    public static void saveHighScores(List<Integer> highScores) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(HIGH_SCORES_FILE))) {
            for (Integer score : highScores) {
                writer.println(score);
            }
        } catch (IOException e) {
            System.err.println("Error saving high scores: " + e.getMessage());
        }
    }
    
    // NEW: Get highest score
    public static int getHighestScore() {
        List<Integer> highScores = loadHighScores();
        return highScores.isEmpty() ? 0 : highScores.get(0);
    }
    
    // NEW: Clear all saved data
    public static void clearAllSaves() {
        new File(SAVE_FILE).delete();
        new File(HIGH_SCORES_FILE).delete();
        System.out.println("All saved data cleared.");
    }
}