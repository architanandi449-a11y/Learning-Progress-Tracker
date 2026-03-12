package learningtracker;

import javax.swing.*;

/**
 * Main application class that serves as the entry point and handles initial setup.
 * This class initializes the database and launches the login screen.
 */
public class App {
    
    /**
     * Main entry point of the application.
     * Sets up database tables and launches the login interface.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        // Set up database tables
        Database.createTables();
        
        // Launch the login frame first
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                // Create and show login frame
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Called by LoginFrame upon successful login to launch the main application window.
     * 
     * @param userId The ID of the logged-in user
     * @param username The username of the logged-in user
     */
    public static void launchMainApp(int userId, String username) {
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("[DEBUG] App.launchMainApp called for user=" + username + " id=" + userId);
                System.out.println("[DEBUG] Call stack for launchMainApp:");
                for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
                    System.out.println("    at " + ste);
                }
                MainFrame mainFrame = new MainFrame(userId, username);
                mainFrame.setVisible(true); // Make visible after complete initialization
                mainFrame.toFront(); // Ensure window comes to front
                mainFrame.requestFocus(); // Give it focus
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Error launching main window: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}