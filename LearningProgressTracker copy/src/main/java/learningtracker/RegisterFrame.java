package learningtracker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class RegisterFrame extends JFrame {
    private static final Color BACKGROUND_COLOR = new Color(246, 248, 250);
    private static final Color PRIMARY_COLOR = new Color(56, 97, 251);
    private static final Color TEXT_COLOR = new Color(36, 41, 47);
    private static final Font TITLE_FONT = new Font("SF Pro Display", Font.BOLD, 32);
    private static final Font SUBTITLE_FONT = new Font("SF Pro Display", Font.PLAIN, 16);
    private static final int CORNER_RADIUS = 12;

    public RegisterFrame() {
        setupFrame();
        setupComponents();
    }

    private void setupFrame() {
        setTitle("Learning Progress Tracker - Register");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        setUndecorated(true);
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
        JLabel helloLabel = new JLabel("Create Account!");
        helloLabel.setFont(TITLE_FONT);
        helloLabel.setForeground(TEXT_COLOR);
        mainPanel.add(helloLabel, gbc);

        JLabel welcomeLabel = new JLabel("Start tracking your learning journey!");
        welcomeLabel.setFont(SUBTITLE_FONT);
        welcomeLabel.setForeground(new Color(87, 96, 106));
        mainPanel.add(welcomeLabel, gbc);

        // Add some vertical spacing
        gbc.insets = new Insets(30, 0, 10, 0);

        // Username field with modern styling
        JLabel usernameLabel = new JLabel("Choose a username");
        usernameLabel.setFont(new Font("SF Pro Text", Font.PLAIN, 14));
        usernameLabel.setForeground(TEXT_COLOR);
        mainPanel.add(usernameLabel, gbc);

        JTextField usernameField = new JTextField(20) {
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
        JLabel passwordLabel = new JLabel("Create a password");
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
        
        // Confirm Password field
        JLabel confirmPasswordLabel = new JLabel("Re-enter password");
        confirmPasswordLabel.setFont(new Font("SF Pro Text", Font.PLAIN, 14));
        confirmPasswordLabel.setForeground(TEXT_COLOR);
        mainPanel.add(confirmPasswordLabel, gbc);

        JPasswordField confirmPasswordField = new JPasswordField(20) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        confirmPasswordField.setOpaque(false);
        confirmPasswordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(208, 215, 222), 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        mainPanel.add(confirmPasswordField, gbc);

        // Create Account button with modern styling
        JButton createButton = new JButton("Create Account") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? PRIMARY_COLOR.darker() : PRIMARY_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        createButton.setForeground(Color.WHITE);
        createButton.setFont(new Font("SF Pro Text", Font.BOLD, 14));
        createButton.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        createButton.setContentAreaFilled(false);
        createButton.setFocusPainted(false);
        createButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            
            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please fill in all fields", 
                    "Registration Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, 
                    "Passwords do not match", 
                    "Registration Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int userId = Database.createUser(username, password);
            if (userId > 0) {
                JOptionPane.showMessageDialog(this, 
                    "Account created successfully! Please log in.", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                dispose();
                new LoginFrame().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to create account. Username may already exist.", 
                    "Registration Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        gbc.insets = new Insets(20, 0, 10, 0);
        mainPanel.add(createButton, gbc);

        // Back to login button
        JButton backButton = new JButton("Back to Login") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                super.paintComponent(g);
            }
        };
        backButton.setForeground(PRIMARY_COLOR);
        backButton.setFont(new Font("SF Pro Text", Font.PLAIN, 14));
        backButton.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        backButton.setContentAreaFilled(false);
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        gbc.insets = new Insets(0, 0, 10, 0);
        mainPanel.add(backButton, gbc);

        add(mainPanel, BorderLayout.CENTER);


    }
}
