package learningtracker;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.github.lgooddatepicker.components.DateTimePicker;

public class GoalDialog extends JDialog {
    private JComboBox<String> subjectCombo;
    private JTextField descriptionField;
    private DateTimePicker deadlinePicker;
    private JTextArea notesArea;
    private boolean confirmed = false;

    public GoalDialog(JFrame parent, String[] subjects) {
        super(parent, "Add New Goal", true);
        setupDialog(subjects);
    }

    private void setupDialog(String[] subjects) {
        setLayout(new BorderLayout(10, 10));
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Subject Selection
        gbc.gridx = 0; gbc.gridy = 0;
        contentPanel.add(new JLabel("Subject:"), gbc);
        gbc.gridx = 1;
        subjectCombo = new JComboBox<>(subjects);
        contentPanel.add(subjectCombo, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 1;
        contentPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        descriptionField = new JTextField(20);
        contentPanel.add(descriptionField, gbc);

        // Deadline
        gbc.gridx = 0; gbc.gridy = 2;
        contentPanel.add(new JLabel("Deadline:"), gbc);
        gbc.gridx = 1;
        deadlinePicker = new DateTimePicker();
        contentPanel.add(deadlinePicker, gbc);

        // Notes
        gbc.gridx = 0; gbc.gridy = 3;
        contentPanel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1;
        notesArea = new JTextArea(4, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        contentPanel.add(new JScrollPane(notesArea), gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            if (descriptionField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter a description for the goal", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (deadlinePicker.getDateTimeStrict() == null) {
                JOptionPane.showMessageDialog(this, 
                    "Please select a deadline", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            confirmed = true;
            dispose();
        });
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(contentPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(getParent());
    }

    public Goal getGoal() {
        if (!confirmed) return null;
        return new Goal(
            (String) subjectCombo.getSelectedItem(),
            descriptionField.getText(),
            deadlinePicker.getDateTimeStrict(),
            notesArea.getText()
        );
    }
}