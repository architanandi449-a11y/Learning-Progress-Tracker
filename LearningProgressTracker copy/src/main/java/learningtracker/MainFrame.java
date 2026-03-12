package learningtracker;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private int userId;
    private String username;
    private JPanel mainPanel;

    public MainFrame(int userId, String username) {
        this.userId = userId;
        this.username = username;
        System.out.println("[DEBUG] MainFrame: constructor for user=" + username + " id=" + userId);
        setupFrame();
        setupComponents();
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                System.out.println("[DEBUG] MainFrame: window opened");
            }
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                System.out.println("[DEBUG] MainFrame: window closing");
            }
        });
    }

    private void setupFrame() {
        setTitle("Learning Progress Tracker - " + username);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 600));
        setPreferredSize(new Dimension(1024, 768));
        setLocationRelativeTo(null);
        
        // Set modern look
        getContentPane().setBackground(new Color(246, 248, 250));
        setLayout(new BorderLayout());
    }

    private void setupComponents() {
        // Menu bar with account actions
        JMenuBar menuBar = new JMenuBar();
        JMenu accountMenu = new JMenu("Account");
        JMenuItem logoutItem = new JMenuItem("Logout");
        JMenuItem resetItem = new JMenuItem("Reset All Data");

        logoutItem.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        });

        resetItem.addActionListener(e -> {
            // Ask for password confirmation
            JPasswordField pf = new JPasswordField();
            int okCxl = JOptionPane.showConfirmDialog(this, pf, "Enter your password to confirm reset:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (okCxl == JOptionPane.OK_OPTION) {
                String pwd = new String(pf.getPassword());
                int auth = Database.authenticateUser(username, pwd);
                if (auth == userId) {
                    int c = JOptionPane.showConfirmDialog(this, "This will delete ALL data for all users. Continue?", "Confirm Reset", JOptionPane.YES_NO_OPTION);
                    if (c == JOptionPane.YES_OPTION) {
                        Database.resetDatabase();
                        JOptionPane.showMessageDialog(this, "All data deleted. Application will restart.");
                        dispose();
                        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Password incorrect. Reset aborted.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        accountMenu.add(logoutItem);
        accountMenu.add(resetItem);
        menuBar.add(accountMenu);
        setJMenuBar(menuBar);

        // Create main panel with dashboard
        mainPanel = new DashboardPanel(userId, username);
        mainPanel.setOpaque(true);
        
        // Add to frame with some padding
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.setOpaque(true);
        contentPanel.setBackground(new Color(246, 248, 250));
        contentPanel.add(mainPanel, BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Pack and size the frame but don't make visible yet
        pack();
        setSize(getPreferredSize());
    }
}