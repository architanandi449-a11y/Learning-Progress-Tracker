package learningtracker;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Map;

public class SubjectsPanel extends JPanel {
    private int userId;
    private JTextField topicField;
    private JTextField durationField;
    private DefaultListModel<ProgressItem> historyListModel;
    private JList<ProgressItem> historyList;
    private Integer editingId = null;
    private JButton addButton;

    // References to other panels so we can trigger refreshes
    private StatsPanel statsPanelRef;
    private LeaderboardPanel leaderboardPanelRef;

    public SubjectsPanel(int userId, StatsPanel statsPanelRef, LeaderboardPanel leaderboardPanelRef) {
        this.userId = userId;
        this.statsPanelRef = statsPanelRef;
        this.leaderboardPanelRef = leaderboardPanelRef;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        setupPanel();
        loadHistory();
    }

    // Simple holder for progress items displayed in the list
    private static class ProgressItem {
        int id;
        String topic;
        int duration;

        ProgressItem(int id, String topic, int duration) {
            this.id = id;
            this.topic = topic;
            this.duration = duration;
        }

        @Override
        public String toString() {
            return String.format("%s - %d minutes", topic, duration);
        }
    }

    private void loadHistory() {
        historyListModel.clear();
        java.util.List<Object[]> rows = Database.getRecentProgress(userId);
        for (Object[] r : rows) {
            int id = (Integer) r[0];
            String topic = (String) r[1];
            int duration = (Integer) r[2];
            historyListModel.addElement(new ProgressItem(id, topic, duration));
        }
    }

    private void startEditSelected() {
        ProgressItem sel = historyList.getSelectedValue();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Please select an entry to edit", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
    editingId = sel.id;
    topicField.setText(sel.topic);
    durationField.setText(String.valueOf(sel.duration));
    if (addButton != null) addButton.setText("Save");
    }

    private void deleteSelected() {
        ProgressItem sel = historyList.getSelectedValue();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Please select an entry to delete", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected entry?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        boolean ok = Database.deleteProgress(sel.id);
        if (ok) {
            historyListModel.removeElement(sel);
            // Refresh other panels
            refreshStats();
            if (statsPanelRef != null) statsPanelRef.refreshData();
            if (leaderboardPanelRef != null) leaderboardPanelRef.refreshLeaderboard();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to delete entry", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupPanel() {
        // Input form at the top
        add(createInputFormPanel(), BorderLayout.NORTH);
        
        // History list in the center
        add(createHistoryListPanel(), BorderLayout.CENTER);
        
        // Statistics at the bottom
        add(createStatsPanel(), BorderLayout.SOUTH);
    }

    private JPanel createInputFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Topic field
        formPanel.add(new JLabel("Topic:"), gbc);
        topicField = new JTextField(20);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        formPanel.add(topicField, gbc);

        // Duration field
        gbc.gridwidth = 1;
        formPanel.add(new JLabel("Duration (minutes):"), gbc);
        durationField = new JTextField(10);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        formPanel.add(durationField, gbc);

    // Add button
    addButton = new JButton("Add Progress Entry");
    addButton.addActionListener(e -> addProgressEntry());
    formPanel.add(addButton, gbc);

        return formPanel;
    }

    private JPanel createHistoryListPanel() {
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBorder(BorderFactory.createTitledBorder("Recent Progress"));
        historyListModel = new DefaultListModel<>();
        historyList = new JList<>(historyListModel);
        historyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(historyList);

        // Add edit/delete buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");
        actions.add(editBtn);
        actions.add(deleteBtn);

        editBtn.addActionListener(e -> startEditSelected());
        deleteBtn.addActionListener(e -> deleteSelected());

        historyPanel.add(scrollPane, BorderLayout.CENTER);
        historyPanel.add(actions, BorderLayout.SOUTH);
        return historyPanel;
    }

    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Statistics"));

        // Get statistics from database
        Map<String, String> stats = Database.getStatistics(userId);

        // Add statistics labels
        for (Map.Entry<String, String> entry : stats.entrySet()) {
            JLabel statLabel = new JLabel(entry.getKey() + ": " + entry.getValue());
            statsPanel.add(statLabel);
        }

        return statsPanel;
    }

    private void addProgressEntry() {
        try {
            String topic = topicField.getText().trim();
            int duration = Integer.parseInt(durationField.getText().trim());

            if (topic.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter a topic", 
                    "Input Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (duration <= 0) {
                JOptionPane.showMessageDialog(this, 
                    "Duration must be greater than 0", 
                    "Input Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (editingId != null) {
                boolean ok = Database.updateProgress(editingId, topic, duration);
                if (ok) {
                    // update item in list model
                    for (int i = 0; i < historyListModel.size(); i++) {
                        ProgressItem it = historyListModel.get(i);
                        if (it.id == editingId) {
                            it.topic = topic;
                            it.duration = duration;
                            historyListModel.set(i, it);
                            break;
                        }
                    }
                    editingId = null;
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update entry", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                int newId = Database.addProgressReturnId(userId, topic, duration);
                if (newId > 0) {
                    ProgressItem item = new ProgressItem(newId, topic, duration);
                    historyListModel.add(0, item);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add entry", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            // Clear input fields
            topicField.setText("");
            durationField.setText("");
            if (addButton != null) addButton.setText("Add Progress Entry");

            // Refresh statistics and leaderboard
            refreshStats();
            if (statsPanelRef != null) statsPanelRef.refreshData();
            if (leaderboardPanelRef != null) leaderboardPanelRef.refreshLeaderboard();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a valid number for duration", 
                "Input Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshStats() {
        // Remove old stats panel
        Component[] components = getComponents();
        for (Component component : components) {
            if (component instanceof JPanel && 
                ((JPanel)component).getBorder() instanceof TitledBorder &&
                ((TitledBorder)((JPanel)component).getBorder()).getTitle().equals("Statistics")) {
                remove(component);
                break;
            }
        }

        // Add new stats panel
        add(createStatsPanel(), BorderLayout.SOUTH);
        revalidate();
        repaint();
    }
}