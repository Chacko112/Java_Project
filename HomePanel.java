package opps;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HomePanel extends JPanel {

    private JLabel timeLabel;
    private JLabel dateLabel;

    public HomePanel(String name) {

        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);

        // --- Header Panel ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel welcomeLabel = new JLabel("Welcome, " + name + "! Here's today's overview.");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        welcomeLabel.setHorizontalAlignment(SwingConstants.LEFT);
        header.add(welcomeLabel, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);

        // --- Center Panel ---
        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        centerPanel.setBackground(Color.WHITE);

        // --- Left: Summary Cards ---
        JPanel summaryPanel = new JPanel(new GridLayout(2, 3, 6, 6));
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
        summaryPanel.setBackground(Color.WHITE);

        summaryPanel.add(createSummaryCard("Hotel Name", "Blue Moon"));
        summaryPanel.add(createSummaryCard("Total Rooms", "80"));
        summaryPanel.add(createSummaryCard("Available", "20"));
        summaryPanel.add(createSummaryCard("Bookings", "60"));
        summaryPanel.add(createSummaryCard("Payments", "$12,500"));
        summaryPanel.add(createSummaryCard("Pending", "5"));

        centerPanel.add(summaryPanel, BorderLayout.CENTER);

        // --- Right: Date & Time Panel ---
        JPanel dateTimePanel = new JPanel();
        dateTimePanel.setLayout(new BoxLayout(dateTimePanel, BoxLayout.Y_AXIS));
        dateTimePanel.setBackground(new Color(240, 243, 255));
        dateTimePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 220), 1),
                BorderFactory.createEmptyBorder(25, 20, 25, 20)
        ));
        dateTimePanel.setPreferredSize(new Dimension(260, 200));

        JLabel calendarIcon = new JLabel("📅", SwingConstants.CENTER);
        calendarIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50));
        calendarIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        dateLabel = new JLabel("", SwingConstants.CENTER);
        dateLabel.setFont(new Font("Arial", Font.BOLD, 18));
        dateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        timeLabel = new JLabel("", SwingConstants.CENTER);
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 22));
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        dateTimePanel.add(calendarIcon);
        dateTimePanel.add(Box.createVerticalStrut(12));
        dateTimePanel.add(dateLabel);
        dateTimePanel.add(Box.createVerticalStrut(6));
        dateTimePanel.add(timeLabel);

        // --- Timer to update date and time every second ---
        Timer timer = new Timer(1000, e -> updateDateTime());
        timer.start();
        updateDateTime();

        centerPanel.add(dateTimePanel, BorderLayout.EAST);

        add(centerPanel, BorderLayout.CENTER);
    }

    // --- Updates date and time labels ---
    private void updateDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMM yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a");
        Date now = new Date();
        dateLabel.setText(dateFormat.format(now));
        timeLabel.setText(timeFormat.format(now));
    }

    // --- Creates a summary card for dashboard ---
    private JPanel createSummaryCard(String title, String value) {
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(90, 50));
        card.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 230), 1, true));
        card.setBackground(new Color(247, 247, 255));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 11));

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 15));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }
}
