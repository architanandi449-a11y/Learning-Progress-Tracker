package learningtracker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private static final Color BACKGROUND_COLOR = new Color(246, 248, 250);
    private static final Color PRIMARY_COLOR = new Color(56, 97, 251);
    private static final Color TEXT_COLOR = new Color(36, 41, 47);
    private static final Font TITLE_FONT = new Font("SF Pro Display", Font.BOLD, 32);
    private static final Font SUBTITLE_FONT = new Font("SF Pro Display", Font.PLAIN, 16);
    private static final int CORNER_RADIUS = 12;

    public LoginFrame() {
        System.out.println("[DEBUG] LoginFrame: constructor");
        setupFrame();
        setupComponents();
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                System.out.println("[DEBUG] LoginFrame: window opened");
            }
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                System.out.println("[DEBUG] LoginFrame: window closed");
                System.out.println("[DEBUG] LoginFrame: windowClosed call stack:");
                for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
                    System.out.println("    at " + ste);
                }
            }
        });
    }

    private void setupFrame() {
        setTitle("Learning Progress Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        setUndecorated(true); // Remove window decorations
        setShape(new RoundRectangle2D.Double(0, 0, 500, 600, CORNER_RADIUS, CORNER_RADIUS));
    }

    private void setupComponents() {
        // Main panel with modern styling
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(BACKGROUND_COLOR);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS);
            }
        };
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));
        mainPanel.setBackground(BACKGROUND_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.anchor = GridBagConstraints.NORTH;

        // Welcome labels
        JLabel helloLabel = new JLabel("Hello!");
        helloLabel.setFont(TITLE_FONT);
        helloLabel.setForeground(TEXT_COLOR);
        mainPanel.add(helloLabel, gbc);

        JLabel welcomeLabel = new JLabel("We are really happy to see you again!");
        welcomeLabel.setFont(SUBTITLE_FONT);
        welcomeLabel.setForeground(new Color(87, 96, 106));
        mainPanel.add(welcomeLabel, gbc);

        // Add some vertical spacing
        gbc.insets = new Insets(30, 0, 10, 0);

        // Username field with modern styling
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("SF Pro Text", Font.PLAIN, 14));
        usernameLabel.setForeground(TEXT_COLOR);
        mainPanel.add(usernameLabel, gbc);

        usernameField = new JTextField(20) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        usernameField.setOpaque(false);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(208, 215, 222), 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        mainPanel.add(usernameField, gbc);

        // Password field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("SF Pro Text", Font.PLAIN, 14));
        passwordLabel.setForeground(TEXT_COLOR);
        mainPanel.add(passwordLabel, gbc);

        JPasswordField passwordField = new JPasswordField(20) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        passwordField.setOpaque(false);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(208, 215, 222), 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        mainPanel.add(passwordField, gbc);

        // Sign in button with modern styling
        JButton loginButton = new JButton("Sign in") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? PRIMARY_COLOR.darker() : PRIMARY_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("SF Pro Text", Font.BOLD, 14));
        loginButton.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        loginButton.setContentAreaFilled(false);
        loginButton.setFocusPainted(false);
        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter username and password", "Login Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int userId = Database.authenticateUser(username, password);
            if (userId > 0) {
                dispose();
                App.launchMainApp(userId, username);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password", "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        gbc.insets = new Insets(20, 0, 10, 0);
        mainPanel.add(loginButton, gbc);

        // Register button with subtle styling
        JButton registerButton = new JButton("Create an account") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                super.paintComponent(g);
            }
        };
        registerButton.setForeground(PRIMARY_COLOR);
        registerButton.setFont(new Font("SF Pro Text", Font.PLAIN, 14));
        registerButton.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        registerButton.setContentAreaFilled(false);
        registerButton.setFocusPainted(false);
        registerButton.addActionListener(e -> {
            dispose();
            new RegisterFrame().setVisible(true);
        });

        gbc.insets = new Insets(0, 0, 10, 0);
        mainPanel.add(registerButton, gbc);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void handleLogin() {
        // kept for compatibility; primary login is handled in the sign-in button action
    }
}
