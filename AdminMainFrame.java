package opps;

import javax.swing.*;
import java.awt.*;

public class AdminMainFrame extends JFrame {

    private JTabbedPane tabbedPane;

    public AdminMainFrame(String managerName) {
        setTitle("Manager Dashboard");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();

        // Create OperationsPanel first (this will manage bookings)
        OperationsPanel operationsPanel = new OperationsPanel();

        // Add Tabs
        tabbedPane.addTab("Home", new HomePanel(managerName));
        tabbedPane.addTab("Operations", operationsPanel);
        
        tabbedPane.addTab("Rooms", new RoomsPanel());

        // Rooms linked to operations panel
        tabbedPane.addTab("Payments", new PaymentsPanel());
        tabbedPane.addTab("Reports", new ReportsPanel());
        tabbedPane.addTab("Users", new UserPanel());

        add(tabbedPane);

        // ----- Header -----
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Manager Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        UserBox userbox = new UserBox(managerName); // small profile box

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(userbox, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // ----- Load data from database -----
        operationsPanel.loadBookingsFromDB();
        tabbedPane.setSelectedIndex(0);
    }
}
