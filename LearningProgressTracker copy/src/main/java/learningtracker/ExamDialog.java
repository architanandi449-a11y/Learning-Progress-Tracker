package learningtracker;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ExamDialog extends JDialog {
    private JComboBox<String> subjectCombo;
    private JSpinner scoreSpinner;
    private JTextArea notesArea;
    private boolean confirmed = false;

    public ExamDialog(JFrame parent, String[] subjects) {
        super(parent, "Add New Exam", true);
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

        // Score
        gbc.gridx = 0; gbc.gridy = 1;
        contentPanel.add(new JLabel("Score (0-100):"), gbc);
        gbc.gridx = 1;
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(0, 0, 100, 1);
        scoreSpinner = new JSpinner(spinnerModel);
        contentPanel.add(scoreSpinner, gbc);

        // Notes
        gbc.gridx = 0; gbc.gridy = 2;
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

    public Exam getExam() {
        if (!confirmed) return null;
        return new Exam(
            (String) subjectCombo.getSelectedItem(),
            LocalDateTime.now(),
            (Integer) scoreSpinner.getValue(),
            notesArea.getText()
        );
    }
}