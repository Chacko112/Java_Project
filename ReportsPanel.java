package opps;

import javax.swing.*;
import java.awt.*;

public class ReportsPanel extends JPanel {

    private JLabel weeklyCustomerLabel, weeklyElectricityLabel, weeklyOtherLabel;
    private JLabel monthlyRevenueLabel, monthlyExpenseLabel;

    public ReportsPanel() {
        setLayout(new BorderLayout());

        JTabbedPane reportTabs = new JTabbedPane();

        // --- Weekly Report Tab ---
        JPanel weeklyPanel = new JPanel();
        weeklyPanel.setLayout(new BoxLayout(weeklyPanel, BoxLayout.Y_AXIS));
        weeklyPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lWeeklyTitle = new JLabel("WEEKLY REPORT");
        lWeeklyTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lWeeklyTitle.setFont(new Font("Arial", Font.BOLD, 16));
        weeklyPanel.add(lWeeklyTitle);
        weeklyPanel.add(Box.createVerticalStrut(20));

        weeklyCustomerLabel = centerLabel("CUSTOMER - 62.5%");
        weeklyElectricityLabel = centerLabel("ELECTRICITY - 25%");
        weeklyOtherLabel = centerLabel("OTHER EXPENSE - 12.5%");

        weeklyPanel.add(weeklyCustomerLabel);
        weeklyPanel.add(weeklyElectricityLabel);
        weeklyPanel.add(weeklyOtherLabel);

        reportTabs.addTab("WEEKLY", weeklyPanel);

        // --- Monthly Report Tab ---
        JPanel monthlyPanel = new JPanel();
        monthlyPanel.setLayout(new BoxLayout(monthlyPanel, BoxLayout.Y_AXIS));
        monthlyPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lMonthlyTitle = new JLabel("MONTHLY REPORT");
        lMonthlyTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lMonthlyTitle.setFont(new Font("Arial", Font.BOLD, 16));
        monthlyPanel.add(lMonthlyTitle);
        monthlyPanel.add(Box.createVerticalStrut(20));

        monthlyRevenueLabel = centerLabel("CUSTOMER, ELECTRICITY, WATER BILL");
        monthlyExpenseLabel = centerLabel("SALARY, MAINTENANCE, SECURITY");

        monthlyPanel.add(monthlyRevenueLabel);
        monthlyPanel.add(monthlyExpenseLabel);

        reportTabs.addTab("MONTHLY", monthlyPanel);

        add(reportTabs, BorderLayout.CENTER);
    }

    // --- Helper method to center labels ---
    private JLabel centerLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setFont(new Font("Arial", Font.PLAIN, 14));
        return lbl;
    }

    // --- Methods to update report labels dynamically (optional for DB integration) ---
    public void setWeeklyReport(double customerPercent, double electricityPercent, double otherPercent) {
        weeklyCustomerLabel.setText("CUSTOMER - " + customerPercent + "%");
        weeklyElectricityLabel.setText("ELECTRICITY - " + electricityPercent + "%");
        weeklyOtherLabel.setText("OTHER EXPENSE - " + otherPercent + "%");
    }

    public void setMonthlyReport(String revenueDetails, String expenseDetails) {
        monthlyRevenueLabel.setText(revenueDetails);
        monthlyExpenseLabel.setText(expenseDetails);
    }
}
