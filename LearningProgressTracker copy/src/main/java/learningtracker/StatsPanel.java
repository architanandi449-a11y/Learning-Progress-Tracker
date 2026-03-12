package learningtracker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StatsPanel extends JPanel {
    private Map<String, Integer> data;
    private JLabel timeLabel;
    private JLabel dateLabel;
    private JPanel chartPanel;
    private JPanel legendPanel;
    private Timer timer;
    private int totalMinutes = 0;
    // index of currently hovered slice (-1 when none)
    private int hoveredIndex = -1;
    // In-memory enhanced data models (kept separate from existing DB-driven subject minutes)
    private List<Lesson> lessons = new ArrayList<>();
    private List<Exam> exams = new ArrayList<>();
    private List<Goal> goals = new ArrayList<>();
    private JTabbedPane detailsTabs;

    // Modern Color Palette
    private static final Color BACKGROUND_COLOR = new Color(246, 248, 250);
    private static final Color[] CHART_COLORS = {
        new Color(75, 192, 192),  // Teal
        new Color(255, 99, 132),  // Coral
        new Color(255, 205, 86),  // Yellow
        new Color(54, 162, 235),  // Blue
        new Color(153, 102, 255), // Purple
        new Color(255, 159, 64)   // Orange
    };
    private static final Color TEXT_COLOR = new Color(36, 41, 47);
    private static final Font TITLE_FONT = new Font("SF Pro Display", Font.BOLD, 20);
    private static final Font LABEL_FONT = new Font("SF Pro Text", Font.PLAIN, 14);
        private static final String[] MOTIVATIONAL_QUOTES = {
            "The expert in anything was once a beginner. - Helen Hayes",
            "Success is not final, failure is not fatal: it is the courage to continue that counts. - Winston Churchill",
            "Your time is limited, don't waste it living someone else's life. - Steve Jobs",
            "The future depends on what you do today. - Mahatma Gandhi",
            "The only way to do great work is to love what you do. - Steve Jobs",
            "Everything you've ever wanted is on the other side of fear. - George Addair",
            "Success is walking from failure to failure with no loss of enthusiasm. - Winston Churchill",
            "The harder you work for something, the greater you'll feel when you achieve it.",
            "Education is not preparation for life; education is life itself. - John Dewey",
            "Learning is not attained by chance, it must be sought for with ardor. - Abigail Adams"
        };

    private int userId;

    public StatsPanel(int userId) {
        this.userId = userId;
        this.data = new HashMap<>();
        setupPanel();
        startTimer();
        refreshData();
    }

    public void refreshData() {
        this.data = Database.getSubjectData(userId);
        if (chartPanel != null) {
            chartPanel.repaint();
        }
        if (legendPanel != null) {
            rebuildLegend();
        }
        if (detailsTabs != null) {
            rebuildDetailsTabs();
        }
    }

    private void setupPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(40, 40, 40, 40));

        // Main title
        JLabel titleLabel = new JLabel("Study Statistics");
        titleLabel.setFont(new Font("SF Pro Display", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Create top container to stack title and topPanel (prevents overwriting BorderLayout.NORTH)
        JPanel northContainer = new JPanel();
        northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.Y_AXIS));
        northContainer.setBackground(BACKGROUND_COLOR);
        northContainer.add(titleLabel);

        // Create pie chart panel with improved visuals
        chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                // Make the pie chart take a larger portion of the available area
                int size = (int) (Math.min(width, height) * 0.78);
                int x = (width - size) / 2;
                int y = (height - size) / 2;
                // If there's no data, draw a friendly empty state centered
                if (data == null || data.isEmpty()) {
                    g2.setFont(TITLE_FONT);
                    g2.setColor(TEXT_COLOR);
                    String msg = "No study data available yet";
                    FontMetrics fmMsg = g2.getFontMetrics();
                    g2.drawString(msg, (width - fmMsg.stringWidth(msg)) / 2, height / 2);
                    return;
                }
                
                // Calculate total time
                int total = data.values().stream().mapToInt(Integer::intValue).sum();
                
                // Draw pie chart
                double currentAngle = 0;
                int i = 0;
                for (Map.Entry<String, Integer> entry : data.entrySet()) {
                    double sliceAngle = 360.0 * entry.getValue() / total;
                    // compute mid angle for this slice
                    double midAngle = Math.toRadians(currentAngle + sliceAngle / 2);

                    // if hovered, draw slice slightly popped out
                    boolean isHovered = (i == hoveredIndex);
                    int offset = isHovered ? 12 : 0;
                    int dxOff = (int) (Math.cos(midAngle) * offset);
                    int dyOff = (int) (Math.sin(midAngle) * offset);

                    g2.setColor(CHART_COLORS[i % CHART_COLORS.length]);
                    g2.fillArc(x + dxOff, y + dyOff, size, size, (int) currentAngle, (int) sliceAngle);
                    
                    // Labels are shown in the right-side legend; do not draw subject labels on the pie
                    
                    currentAngle += sliceAngle;
                    i++;
                }
            }
        };
        // add mouse handling for hover detection + tooltips
        chartPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int idx = findSliceIndexAtPoint(e.getPoint());
                if (idx != hoveredIndex) {
                    hoveredIndex = idx;
                    if (hoveredIndex >= 0) {
                        String name = (String) data.keySet().toArray()[hoveredIndex];
                        int minutes = (int) data.values().toArray()[hoveredIndex];
                        double total = data.values().stream().mapToInt(Integer::intValue).sum();
                        double pct = (minutes * 100.0) / total;
                        chartPanel.setToolTipText(String.format("%s (%.1f%%) — %s", name, pct, formatDuration(minutes)));
                    } else {
                        chartPanel.setToolTipText(null);
                    }
                    chartPanel.repaint();
                }
            }
        });
        chartPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                if (hoveredIndex != -1) {
                    hoveredIndex = -1;
                    chartPanel.setToolTipText(null);
                    chartPanel.repaint();
                }
            }
        });
        chartPanel.setPreferredSize(new Dimension(800, 600));
        chartPanel.setBackground(BACKGROUND_COLOR);
        
    // Legend panel on the right - will contain a grid of legend entries (2 columns)
    legendPanel = new JPanel(new BorderLayout());
    legendPanel.setBackground(BACKGROUND_COLOR);
    legendPanel.setBorder(new EmptyBorder(20, 16, 20, 16));
    // Slightly wider so longer subject names can be shown side-by-side
    legendPanel.setPreferredSize(new Dimension(340, chartPanel.getPreferredSize().height));
        
        // Top panel with time and total stats
        JPanel topPanel = createTopPanel();
        northContainer.add(topPanel);

    // Center container to hold chart and legend (chart center, legend east)
    JPanel centerContainer = new JPanel(new BorderLayout());
    centerContainer.setBackground(BACKGROUND_COLOR);
    centerContainer.add(chartPanel, BorderLayout.CENTER);
    centerContainer.add(legendPanel, BorderLayout.EAST);

        // Add panels to the main panel
        add(northContainer, BorderLayout.NORTH);
        add(centerContainer, BorderLayout.CENTER);

    // Details tabs (summary, lessons, goals, exams, recommendations)
    detailsTabs = new JTabbedPane();
    detailsTabs.setBackground(BACKGROUND_COLOR);
    detailsTabs.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
    // Reasonable default height so it doesn't collapse
    detailsTabs.setPreferredSize(new Dimension(0, 220));
    rebuildDetailsTabs();
    add(detailsTabs, BorderLayout.SOUTH);

        // Build initial legend
        rebuildLegend();
    }

    // Rebuild legend UI based on `data`
    private void rebuildLegend() {
        legendPanel.removeAll();
        if (data == null || data.isEmpty()) {
            JLabel empty = new JLabel("No data");
            empty.setFont(LABEL_FONT);
            empty.setForeground(TEXT_COLOR);
            legendPanel.add(empty, BorderLayout.CENTER);
            legendPanel.revalidate();
            legendPanel.repaint();
            return;
        }

        int total = data.values().stream().mapToInt(Integer::intValue).sum();
        // Grid container for side-by-side legend entries (2 columns)
        JPanel grid = new JPanel(new GridLayout(0, 2, 12, 10));
        grid.setBackground(BACKGROUND_COLOR);
        grid.setOpaque(false);

        int i = 0;
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
            item.setBackground(BACKGROUND_COLOR);
            item.setOpaque(false);

            // Larger swatch for clearer identification
            JPanel swatch = new JPanel();
            swatch.setBackground(CHART_COLORS[i % CHART_COLORS.length]);
            swatch.setPreferredSize(new Dimension(20, 20));
            swatch.setBorder(BorderFactory.createLineBorder(new Color(200,200,200), 1));

            // Info panel with subject + percent (bold) and duration (smaller) side-by-side
            JPanel info = new JPanel();
            info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
            info.setOpaque(false);
            double pct = (entry.getValue() * 100.0) / total;
            JLabel title = new JLabel(String.format("%s  (%.1f%%)", entry.getKey(), pct));
            title.setFont(new Font(LABEL_FONT.getName(), Font.BOLD, 13));
            title.setForeground(TEXT_COLOR.darker());
            JLabel dur = new JLabel(formatDuration(entry.getValue()));
            dur.setFont(new Font(LABEL_FONT.getName(), Font.PLAIN, 12));
            dur.setForeground(new Color(110, 116, 123));
            info.add(title);
            info.add(dur);

            item.add(swatch);
            item.add(info);

            grid.add(item);
            i++;
        }

        // Wrap grid in a panel so it can be centered vertically
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BACKGROUND_COLOR);
        wrapper.setOpaque(false);
        wrapper.add(grid, BorderLayout.NORTH);

        legendPanel.add(wrapper, BorderLayout.CENTER);
        legendPanel.revalidate();
        legendPanel.repaint();
    }

    // --- Details / enhanced features (lessons, goals, exams, recommendations) ---
    private void rebuildDetailsTabs() {
        if (detailsTabs == null) return;
        detailsTabs.removeAll();
        detailsTabs.addTab("Summary", buildSummaryTab());
        detailsTabs.addTab("Lessons", buildLessonsTab());
        detailsTabs.addTab("Goals", buildGoalsTab());
        detailsTabs.addTab("Exams", buildExamsTab());
        detailsTabs.addTab("Recommendations", buildRecommendationsTab());
        detailsTabs.revalidate();
        detailsTabs.repaint();
    }

    private JPanel buildSummaryTab() {
        JPanel p = new JPanel(new BorderLayout(10,10));
        p.setBackground(BACKGROUND_COLOR);

        int totalSubjects = data == null ? 0 : data.size();
        int totalLessons = lessons.size();
        double totalDuration = lessons.stream().mapToInt(Lesson::getDuration).sum();
        double avgExamScore = exams.isEmpty() ? 0 : exams.stream().mapToDouble(Exam::getScore).average().orElse(0);

        JPanel top = new JPanel(new GridLayout(1,3,10,10));
        top.setBackground(BACKGROUND_COLOR);
        top.add(createInfoBox("Total Subjects", String.valueOf(totalSubjects)));
        top.add(createInfoBox("Total Lessons", String.valueOf(totalLessons)));
        top.add(createInfoBox("Average Score", String.format("%.1f%%", avgExamScore)));
        p.add(top, BorderLayout.NORTH);

        // Show total study duration
        int overallProgress = (int)(totalDuration / 60.0); // Convert minutes to hours
        JPanel prog = new JPanel(new BorderLayout(8,8));
        prog.setBackground(BACKGROUND_COLOR);
        JLabel progLabel = new JLabel("Overall Progress");
        progLabel.setFont(LABEL_FONT);
        JProgressBar progressBar = new JProgressBar(0,100);
        progressBar.setValue(overallProgress);
        progressBar.setStringPainted(true);
        prog.add(progLabel, BorderLayout.WEST);
        prog.add(progressBar, BorderLayout.CENTER);
        p.add(prog, BorderLayout.CENTER);

        // Quick stats at bottom
        JPanel bottom = new JPanel(new GridLayout(1,2,10,10));
        bottom.setBackground(BACKGROUND_COLOR);
        bottom.add(new JLabel("Lessons: " + lessons.size()));
        bottom.add(new JLabel("Exams: " + exams.size()));
        p.add(bottom, BorderLayout.SOUTH);

        return p;
    }

    private JPanel createInfoBox(String title, String value) {
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(BACKGROUND_COLOR);
        JLabel t = new JLabel(title);
        t.setFont(LABEL_FONT);
        JLabel v = new JLabel(value);
        v.setFont(TITLE_FONT);
        v.setHorizontalAlignment(SwingConstants.CENTER);
        box.add(t, BorderLayout.NORTH);
        box.add(v, BorderLayout.CENTER);
        box.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        return box;
    }

    private JPanel buildLessonsTab() {
        JPanel p = new JPanel(new BorderLayout(6,6));
        p.setBackground(BACKGROUND_COLOR);

        String[] cols = new String[] {"Subject", "Duration (min)", "Date", "Notes"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Lesson l : lessons) {
            model.addRow(new Object[]{
                l.getSubject(),
                l.getDuration(),
                l.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                l.getNotes()
            });
        }
        JTable table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);
        p.add(sp, BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controls.setBackground(BACKGROUND_COLOR);
        JButton add = new JButton("Add Lesson");
        add.addActionListener(e -> {
            LessonDialog dlg = new LessonDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                data.keySet().toArray(new String[0])
            );
            dlg.setVisible(true);
            Lesson newLesson = dlg.getLesson();
            if (newLesson != null) {
                lessons.add(newLesson);
                rebuildDetailsTabs();
            }
        });
        controls.add(add);
        p.add(controls, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildGoalsTab() {
        JPanel p = new JPanel(new BorderLayout(6,6));
        p.setBackground(BACKGROUND_COLOR);

        DefaultListModel<String> lm = new DefaultListModel<>();
        for (Goal g : goals) {
            String deadlineStr = g.getDeadline().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            lm.addElement(g.getSubject() + " - " + g.getDescription() + " (Due: " + deadlineStr + ")");
        }
        JList<String> list = new JList<>(lm);
        p.add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        ctrl.setBackground(BACKGROUND_COLOR);
        JButton add = new JButton("Add Goal");
        add.addActionListener(e -> {
            GoalDialog gd = new GoalDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                data.keySet().toArray(new String[0])
            );
            gd.setVisible(true);
            Goal newGoal = gd.getGoal();
            if (newGoal != null) {
                goals.add(newGoal);
                rebuildDetailsTabs();
            }
        });
        ctrl.add(add);
        p.add(ctrl, BorderLayout.SOUTH);

        // show completion status of first goal if any
        if (!goals.isEmpty()) {
            Goal g = goals.get(0);
            JProgressBar gb = new JProgressBar(0, 1);
            gb.setValue(g.isCompleted() ? 1 : 0);
            gb.setStringPainted(true);
            gb.setString(g.isCompleted() ? "Completed" : "In Progress");
            p.add(gb, BorderLayout.NORTH);
        }

        return p;
    }

    private JPanel buildExamsTab() {
        JPanel p = new JPanel(new BorderLayout(6,6));
        p.setBackground(BACKGROUND_COLOR);

        String[] cols = new String[] {"Subject", "Score", "Date", "Notes"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Exam ex : exams) {
            String level = ex.getScore() >= 85 ? "Excellent" : 
                          ex.getScore() >= 70 ? "Good" : 
                          ex.getScore() >= 50 ? "Average" : 
                          "Needs Improvement";
            model.addRow(new Object[]{
                ex.getSubject(),
                ex.getScore(),
                ex.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                ex.getNotes() + " (" + level + ")"
            });
        }
        JTable table = new JTable(model);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        ctrl.setBackground(BACKGROUND_COLOR);
        JButton add = new JButton("Add Exam");
        add.addActionListener(e -> {
            ExamDialog ed = new ExamDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                data.keySet().toArray(new String[0])
            );
            ed.setVisible(true);
            Exam newExam = ed.getExam();
            if (newExam != null) {
                exams.add(newExam);
                rebuildDetailsTabs();
            }
        });
        ctrl.add(add);
        p.add(ctrl, BorderLayout.SOUTH);

        return p;
    }

    private JPanel buildRecommendationsTab() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BACKGROUND_COLOR);

        DefaultListModel<String> lm = new DefaultListModel<>();
        // Simple recommendation: find subjects with low average minutes or low exam percent
        int total = data.values().stream().mapToInt(Integer::intValue).sum();
        for (Map.Entry<String, Integer> e : data.entrySet()) {
            double share = total==0?0:(e.getValue()*100.0/total);
            if (share < 10) lm.addElement("Focus more on " + e.getKey() + " (low study time)");
        }
        for (Exam ex : exams) {
            if (ex.getScore() < 60) {
                lm.addElement("Review topics in " + ex.getSubject() + " (low test score)");
            }
        }
        
        // Add recommendations based on goals
        for (Goal g : goals) {
            if (!g.isCompleted() && g.getDeadline().isBefore(LocalDateTime.now())) {
                lm.addElement("Overdue goal in " + g.getSubject() + ": " + g.getDescription());
            }
        }
        
        if (lm.isEmpty()) lm.addElement("All subjects look balanced — keep going!");
        JList<String> list = new JList<>(lm);
        p.add(new JScrollPane(list), BorderLayout.CENTER);
        return p;
    }

    // Determine which slice (index) is at the given point, or -1
    private int findSliceIndexAtPoint(Point p) {
        if (data == null || data.isEmpty()) return -1;
        int width = chartPanel.getWidth();
        int height = chartPanel.getHeight();
        int size = Math.min(width, height) - 40;
        int x = (width - size) / 2;
        int y = (height - size) / 2;
        int centerX = x + size / 2;
        int centerY = y + size / 2;

        int dx = p.x - centerX;
        int dy = p.y - centerY;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist > size / 2.0) return -1; // outside pie

        // compute angle from positive x-axis, counter-clockwise
        double angle = Math.toDegrees(Math.atan2(dy, dx));
        // Note: because screen y increases downward, atan2(dy,dx) gives angle measured clockwise; adjust
        // Use negative dy to get counter-clockwise from positive x
        angle = Math.toDegrees(Math.atan2(-dy, dx));
        if (angle < 0) angle += 360;

        double currentAngle = 0;
        int i = 0;
        int total = data.values().stream().mapToInt(Integer::intValue).sum();
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            double sliceAngle = 360.0 * entry.getValue() / total;
            if (angle >= currentAngle && angle < currentAngle + sliceAngle) {
                return i;
            }
            currentAngle += sliceAngle;
            i++;
        }
        return -1;
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(225, 228, 232)),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
    // Clock panel (time + date stacked for clarity)
    JPanel clockPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    clockPanel.setBackground(BACKGROUND_COLOR);

    JPanel timeBox = new JPanel();
    timeBox.setLayout(new BoxLayout(timeBox, BoxLayout.Y_AXIS));
    timeBox.setOpaque(false);
    timeLabel = new JLabel();
    timeLabel.setFont(new Font(LABEL_FONT.getName(), Font.BOLD, 14));
    timeLabel.setForeground(TEXT_COLOR);
    dateLabel = new JLabel();
    dateLabel.setFont(new Font(LABEL_FONT.getName(), Font.PLAIN, 12));
    dateLabel.setForeground(new Color(120, 126, 133));
    timeBox.add(timeLabel);
    timeBox.add(dateLabel);
    clockPanel.add(timeBox);

    panel.add(clockPanel, BorderLayout.WEST);

        // Total time panel
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statsPanel.setBackground(BACKGROUND_COLOR);
        
        JLabel totalTimeLabel = new JLabel("Total Study Time: " + formatDuration(totalMinutes));
        totalTimeLabel.setFont(LABEL_FONT);
        totalTimeLabel.setForeground(TEXT_COLOR);
        statsPanel.add(totalTimeLabel);
        
        panel.add(statsPanel, BorderLayout.EAST);
        
        return panel;
    }



    private void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateClock();
            }
        }, 0, 1000);
    }

    private void updateClock() {
        if (timeLabel != null) {
            SwingUtilities.invokeLater(() -> {
                LocalDateTime now = LocalDateTime.now();
                timeLabel.setText(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                if (dateLabel != null) {
                    dateLabel.setText(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                }
            });
        }
    }

    public void setData(Map<String, Integer> newData) {
        this.data = newData;
        this.totalMinutes = newData.values().stream().mapToInt(Integer::intValue).sum();
        if (chartPanel != null) {
            chartPanel.repaint();
        }
    }

    private void drawPieChart(Graphics2D g2d, int centerX, int centerY, int radius) {
        double total = data.values().stream().mapToDouble(Integer::doubleValue).sum();
        if (total == 0) return;

        double currentAngle = 0;
        int colorIndex = 0;

        // Draw pie slices
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            double sliceAngle = (entry.getValue() / total) * 360;
            
            // Draw slice with rounded corners
                    // Labels are shown in the right-side legend; do not draw subject labels on the pie
            Arc2D.Double arc = new Arc2D.Double(
                centerX - radius, centerY - radius,
                radius * 2, radius * 2,
                currentAngle, sliceAngle,
                Arc2D.PIE
            );
            g2d.fill(arc);
            
            // Draw label
            double midAngle = Math.toRadians(currentAngle + (sliceAngle / 2));
            int labelX = (int) (centerX + (radius * 1.2 * Math.cos(midAngle)));
            int labelY = (int) (centerY + (radius * 1.2 * Math.sin(midAngle)));
            
            g2d.setColor(TEXT_COLOR);
            g2d.setFont(LABEL_FONT);
            String label = String.format("%s (%d%%)", entry.getKey(), 
                (int) ((entry.getValue() / total) * 100));
            g2d.drawString(label, labelX, labelY);
            
            currentAngle += sliceAngle;
            colorIndex++;
        }
    }

    private void drawBarChart(Graphics2D g2d, int x, int y, int width, int height) {
        if (data.isEmpty()) return;

        int padding = 40;
        int barSpacing = 10;
        int availableWidth = width - (2 * padding);
        int availableHeight = height - (2 * padding);
        
        int barWidth = Math.max(30, (availableWidth - (data.size() - 1) * barSpacing) / data.size());
        
        // Find maximum value for scaling
        int maxValue = data.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        if (maxValue == 0) return;

        int colorIndex = 0;
        int currentX = x + padding;
        
        // Draw bars
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            int barHeight = (int) ((entry.getValue().doubleValue() / maxValue) * availableHeight);
            
            // Draw bar with rounded corners
            g2d.setColor(CHART_COLORS[colorIndex % CHART_COLORS.length]);
            g2d.fillRoundRect(currentX, y + height - padding - barHeight,
                            barWidth, barHeight, 10, 10);
            
            // Draw value on top of bar
            g2d.setColor(TEXT_COLOR);
            g2d.setFont(LABEL_FONT);
            String value = formatDuration(entry.getValue());
            FontMetrics fm = g2d.getFontMetrics();
            int valueWidth = fm.stringWidth(value);
            g2d.drawString(value, currentX + (barWidth - valueWidth) / 2,
                          y + height - padding - barHeight - 5);
            
            // Draw label below bar
            String label = entry.getKey();
            int labelWidth = fm.stringWidth(label);
            g2d.drawString(label, currentX + (barWidth - labelWidth) / 2,
                          y + height - padding + 20);
            
            currentX += barWidth + barSpacing;
            colorIndex++;
        }
    }

    private void drawEmptyState(Graphics2D g2d) {
        g2d.setColor(TEXT_COLOR);
        g2d.setFont(TITLE_FONT);
        String message = "No study data available yet";
        FontMetrics fm = g2d.getFontMetrics();
        int messageWidth = fm.stringWidth(message);
        g2d.drawString(message, (getWidth() - messageWidth) / 2, getHeight() / 2);
    }

    private String formatDuration(int minutes) {
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;
        return String.format("%dh %02dm", hours, remainingMinutes);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}

