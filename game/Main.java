package game;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        // Set system properties for better performance
        System.setProperty("sun.java2d.opengl", "True");
        System.setProperty("sun.java2d.d3d", "False");
        
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Run on EDT
        SwingUtilities.invokeLater(() -> {
            try {
                GameWindow window = new GameWindow();
                window.setVisible(true);
                
                // Display welcome message
                System.out.println("=======================================");
                System.out.println("    TOWER DEFENSE - ULTIMATE EDITION    ");
                System.out.println("=======================================");
                System.out.println("Features:");
                System.out.println("• Home Screen with animated background");
                System.out.println("• Play, Continue, Help, Exit options");
                System.out.println("• Persistent High Scores");
                System.out.println("• Save/Load game functionality");
                System.out.println("• ESC to return to menu");
                System.out.println("=======================================");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Error starting game: " + e.getMessage(),
                    "Startup Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}