package learningtracker;

import javax.swing.*;
import java.awt.*;

public class DashboardPanel extends JPanel {
    private int userId;
    private String username;

    public DashboardPanel(int userId, String username) {
        this.userId = userId;
        this.username = username;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Add components to the dashboard
        setupDashboard();
    }

    private void setupDashboard() {
        setBackground(new Color(246, 248, 250));
        
        // Welcome panel at the top
        JPanel welcomePanel = new JPanel();
        welcomePanel.setBackground(new Color(246, 248, 250));
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel welcomeLabel = new JLabel("Welcome, " + username + "!");
        welcomeLabel.setFont(new Font("SF Pro Display", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(36, 41, 47));
        welcomePanel.add(welcomeLabel);
        
        // Add an actions panel with a button to open the leaderboard directly
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton openLeaderboardBtn = new JButton("Open Leaderboard");
        openLeaderboardBtn.addActionListener(e -> {
            // Open a separate leaderboard window
            SwingUtilities.invokeLater(() -> new LeaderboardFrame().setVisible(true));
        });
        actions.add(openLeaderboardBtn);
        welcomePanel.add(actions);
        add(welcomePanel, BorderLayout.NORTH);

        // Create tabs for different sections
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SF Pro Text", Font.PLAIN, 14));
        tabbedPane.setBackground(new Color(246, 248, 250));
        tabbedPane.setBorder(BorderFactory.createEmptyBorder());
        
    // Create panels and keep references so they can refresh each other
    StatsPanel statsPanel = new StatsPanel(userId);
    LeaderboardPanel leaderboardPanel = new LeaderboardPanel();
    SubjectsPanel subjectsPanel = new SubjectsPanel(userId, statsPanel, leaderboardPanel);

    // Add panels with their tooltips
    tabbedPane.addTab("Progress Entry", subjectsPanel);
    tabbedPane.setToolTipTextAt(0, "Track your study progress");

    tabbedPane.addTab("Statistics", statsPanel);
    tabbedPane.setToolTipTextAt(1, "View your statistics");

    tabbedPane.addTab("Leaderboard", leaderboardPanel);
    tabbedPane.setToolTipTextAt(2, "Compare with others");

        add(tabbedPane, BorderLayout.CENTER);
        
        // Make sure all components are visible
        setOpaque(true);
        tabbedPane.setOpaque(true);
        welcomePanel.setOpaque(true);
    }
}