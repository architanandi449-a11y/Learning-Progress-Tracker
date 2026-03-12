package learningtracker;

import javax.swing.*;
import java.awt.*;

public class LeaderboardFrame extends JFrame {
    public LeaderboardFrame() {
        setTitle("Leaderboard");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        LeaderboardPanel panel = new LeaderboardPanel();
        add(panel, BorderLayout.CENTER);
    }
}
