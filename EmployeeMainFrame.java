package opps;

import javax.swing.*;
import java.awt.*;

public class EmployeeMainFrame extends JFrame {

    private JTabbedPane tabbedPane;

    public EmployeeMainFrame(String empName) {
        setTitle("Employee Dashboard");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();

        // Create OperationsPanel first
        OperationsPanel operationsPanel = new OperationsPanel();

        // Add Tabs (employee has fewer privileges)
        tabbedPane.addTab("Home", new HomePanel(empName));
        tabbedPane.addTab("Operations", operationsPanel);
        
        tabbedPane.addTab("Rooms", new RoomsPanel());
        
        
        tabbedPane.addTab("Payments", new PaymentsPanel());
        // Reports and Users not available for employees
        add(tabbedPane);

        // ----- Header -----
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Employee Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        UserBox userbox = new UserBox(empName);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(userbox, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // ----- Load data from database -----
        operationsPanel.loadBookingsFromDB();
        tabbedPane.setSelectedIndex(0);
    }
}
