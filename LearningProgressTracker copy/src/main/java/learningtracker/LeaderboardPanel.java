package learningtracker;

import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;

public class LeaderboardPanel extends JPanel {
    private JTable leaderboardTable;
    private DefaultTableModel tableModel;
    private JPanel chartPanel;
    private Object[][] leaderboardData;
    
    // Colors for the bar chart
    private static final Color BAR_COLOR = new Color(75, 192, 192);
    private static final Color BAR_HIGHLIGHT = new Color(54, 162, 235);
    private static final Color TEXT_COLOR = new Color(36, 41, 47);
    private static final Font TITLE_FONT = new Font("SF Pro Display", Font.BOLD, 20);
    private static final Font LABEL_FONT = new Font("SF Pro Text", Font.PLAIN, 14);

    public LeaderboardPanel() {
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        setBackground(new Color(246, 248, 250));
        
        setupLeaderboard();
    }

    // Public method to reload leaderboard data and update UI
    public void refreshLeaderboard() {
        Object[][] data = Database.getLeaderboardData();
        this.leaderboardData = data;

        // Update table model
        SwingUtilities.invokeLater(() -> {
            // Clear existing rows
            tableModel.setRowCount(0);
            if (data != null) {
                for (int i = 0; i < data.length; i++) {
                    Object[] row = data[i];
                    // Row: Rank, Username, Total Study Time
                    Object rank = i + 1;
                    String username = (String) row[0];
                    double totalHours = (Double) row[2];
                    String totalTime = String.format("%.2f hours", totalHours);
                    tableModel.addRow(new Object[] { rank, username, totalTime });
                }
            }
            repaint();
        });
    }

    private void setupLeaderboard() {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(new Color(246, 248, 250));
        
        // Add title
        JLabel titleLabel = new JLabel("Study Time Leaderboard");
        titleLabel.setFont(new Font("SF Pro Display", Font.BOLD, 32));
        titleLabel.setForeground(new Color(36, 41, 47));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Create the table model with column names
        String[] columnNames = {"Rank", "Username", "Total Study Time"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Create table and configure its appearance
        leaderboardTable = new JTable(tableModel);
        leaderboardTable.setFillsViewportHeight(true);
        leaderboardTable.setShowGrid(true);
        leaderboardTable.setGridColor(new Color(208, 215, 222));
        leaderboardTable.setFont(new Font("SF Pro Text", Font.PLAIN, 14));
        leaderboardTable.setRowHeight(40);
        leaderboardTable.getTableHeader().setFont(new Font("SF Pro Text", Font.BOLD, 14));
        
        // Add table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(leaderboardTable);
        mainPanel.add(scrollPane, BorderLayout.EAST);
        
        // Create bar chart panel
        chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                int padding = 50;
                
                // Draw title
                g2.setFont(TITLE_FONT);
                g2.setColor(TEXT_COLOR);
                String title = "Study Time Comparison";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(title, (width - fm.stringWidth(title)) / 2, padding);
                
                if (leaderboardData == null || leaderboardData.length == 0) {
                    g2.setFont(LABEL_FONT);
                    String msg = "No data available";
                    g2.drawString(msg, (width - fm.stringWidth(msg)) / 2, height / 2);
                    return;
                }
                
                // Find maximum study time for scaling
                double maxTime = 0;
                for (Object[] row : leaderboardData) {
                    double time = ((Number) row[2]).doubleValue();
                    maxTime = Math.max(maxTime, time);
                }
                
                // Calculate bar dimensions
                int barCount = Math.min(leaderboardData.length, 10); // Show top 10
                int barWidth = (width - 2 * padding) / barCount - 10;
                int baseY = height - padding;
                
                // Draw bars
                g2.setFont(LABEL_FONT);
                for (int i = 0; i < barCount; i++) {
                    String username = (String) leaderboardData[i][0];
                    double time = (Double) leaderboardData[i][2];
                    int barHeight = (int) ((height - 2 * padding) * (time / maxTime));
                    int x = padding + i * (barWidth + 10);
                    int y = baseY - barHeight;
                    
                    // Draw bar
                    g2.setColor(i == 0 ? BAR_HIGHLIGHT : BAR_COLOR);
                    g2.fillRoundRect(x, y, barWidth, barHeight, 10, 10);
                    
                    // Draw username
                    g2.setColor(TEXT_COLOR);
                    g2.rotate(-Math.PI / 4, x + barWidth/2, baseY + 5);
                    g2.drawString(username, x + barWidth/2, baseY + 5);
                    g2.rotate(Math.PI / 4, x + barWidth/2, baseY + 5);
                    
                    // Draw time
                    String timeStr = String.format("%.1fh", time);
                    g2.drawString(timeStr, x + barWidth/2 - fm.stringWidth(timeStr)/2, y - 5);
                }
            }
        };
        chartPanel.setPreferredSize(new Dimension(600, 400));
        mainPanel.add(chartPanel, BorderLayout.CENTER);
        
        // Add refresh button
        JButton refreshButton = new JButton("Refresh Leaderboard");
        refreshButton.addActionListener(e -> refreshLeaderboard());
        mainPanel.add(refreshButton, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Initial load of data
        refreshLeaderboard();
    }

    
}