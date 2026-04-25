package utils;

public class GameConstants {
    public static final int WINDOW_WIDTH = 1200;
    public static final int WINDOW_HEIGHT = 800;
    public static final int GRID_SIZE = 40;
    public static final int PATH_WIDTH = 40;
    
    // Player stats
    public static final int INITIAL_HEALTH = 100;
    public static final int INITIAL_GOLD = 500;
    
    // Tower costs
    public static final int BASIC_TOWER_COST = 100;
    public static final int SNIPER_TOWER_COST = 250;
    public static final int AOE_TOWER_COST = 300;
    
    // Game settings
    public static final int TOWER_UPGRADE_COST_MULTIPLIER = 2;
    public static final int WAVE_COMPLETION_BONUS = 100;
    public static final int ENEMY_KILL_BONUS = 10;
    
    // File paths
    public static final String SAVE_FILE = "tower_defense_save.dat";
    public static final String HIGH_SCORES_FILE = "tower_defense_highscores.dat";
    public static final String CONFIG_FILE = "tower_defense_config.ini";
    
    // Game balance
    public static final double TIME_BETWEEN_WAVES = 5.0; // seconds
    public static final int MAX_WAVES = 20;
    public static final int MAX_TOWERS = 20;
    
    // Enemy stats
    public static final int BASIC_ENEMY_HEALTH = 50;
    public static final int FAST_ENEMY_HEALTH = 30;
    public static final int TANK_ENEMY_HEALTH = 150;
    
    public static final double BASIC_ENEMY_SPEED = 1.0;
    public static final double FAST_ENEMY_SPEED = 2.0;
    public static final double TANK_ENEMY_SPEED = 0.5;
    
    // Tower stats
    public static final int BASIC_TOWER_DAMAGE = 10;
    public static final int SNIPER_TOWER_DAMAGE = 50;
    public static final int AOE_TOWER_DAMAGE = 25;
    
    public static final double BASIC_TOWER_RANGE = 150;
    public static final double SNIPER_TOWER_RANGE = 300;
    public static final double AOE_TOWER_RANGE = 120;
    
    public static final double BASIC_TOWER_FIRE_RATE = 1.0;
    public static final double SNIPER_TOWER_FIRE_RATE = 0.5;
    public static final double AOE_TOWER_FIRE_RATE = 0.8;
}