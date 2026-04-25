package utils;

import entities.Enemy;
import enemies.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WaveManager {
    private Random random;
    private int waveDifficulty;
    
    public WaveManager() {
        this.random = new Random();
        this.waveDifficulty = 1;
    }
    
    public List<Enemy> generateWave(int waveNumber, Vector2D startPoint) {
        List<Enemy> wave = new ArrayList<>();
        int enemyCount = 5 + waveNumber * 2;
        
        for (int i = 0; i < enemyCount; i++) {
            // Stagger enemy spawns
            Vector2D spawnPoint = new Vector2D(
                startPoint.getX() - (i * 100),
                startPoint.getY()
            );
            
            int enemyType = random.nextInt(100);
            
            if (waveNumber > 4 && enemyType < 20) {
                // 20% chance for tank enemy after wave 4
                wave.add(new TankEnemy(spawnPoint));
            } else if (waveNumber > 2 && enemyType < 40) {
                // 40% chance for fast enemy after wave 2
                wave.add(new FastEnemy(spawnPoint));
            } else {
                // Basic enemy
                wave.add(new BasicEnemy(spawnPoint));
            }
        }
        
        waveDifficulty = waveNumber;
        return wave;
    }
    
    public int getWaveDifficulty() {
        return waveDifficulty;
    }
}