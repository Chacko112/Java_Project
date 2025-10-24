package opps;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PaymentsPanel extends JPanel {

    private JTabbedPane payTabs;
    private JTable payTable, pendingTable;
    private JTextField tBookingId, tAdditional;

    public PaymentsPanel() {
        setLayout(new BorderLayout());

        payTabs = new JTabbedPane();

        // --- BILL GENERATION TAB ---
        JPanel billPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lBookingId = new JLabel("Booking ID:");
        gbc.gridx = 0; gbc.gridy = 0;
        billPanel.add(lBookingId, gbc);
        tBookingId = new JTextField("",15);
        gbc.gridx = 1; gbc.gridy = 0;
        billPanel.add(tBookingId, gbc);

        JLabel lAdditional = new JLabel("Additional Payment:");
        gbc.gridx = 0; gbc.gridy = 1;
        billPanel.add(lAdditional, gbc);
        tAdditional = new JTextField("0",15);
        gbc.gridx = 1; gbc.gridy = 1;
        billPanel.add(tAdditional, gbc);

        JButton bGenerateBill = new JButton("Generate Bill");
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth=2;
        gbc.anchor = GridBagConstraints.CENTER;
        billPanel.add(bGenerateBill, gbc);

        bGenerateBill.addActionListener(e -> openBillFrame());
        payTabs.add("BILL GENERATION", billPanel);

        // --- PAYMENT HISTORY TAB ---
        JPanel historyPanel = new JPanel(new BorderLayout());
        String[] payCols = {"PAYMENT NO", "CUSTOMER NAME", "AMOUNT", "MODE", "STATUS", "DATE"};
        payTable = new JTable(new DefaultTableModel(payCols,0));
        historyPanel.add(new JScrollPane(payTable), BorderLayout.CENTER);
        payTabs.add("PAYMENT HISTORY", historyPanel);

        // --- PENDING PAYMENTS TAB ---
        JPanel pendingPanel = new JPanel(new BorderLayout());
        String[] pendingCols = {"CUSTOMER NAME", "AMOUNT", "MODE", "STATUS", "DATE"};
        pendingTable = new JTable(new DefaultTableModel(pendingCols,0));
        pendingPanel.add(new JScrollPane(pendingTable), BorderLayout.CENTER);
        payTabs.add("PENDING PAYMENTS", pendingPanel);

        add(payTabs, BorderLayout.CENTER);

        // --- Load data from database ---
        updatePayTable();
        updatePendingTable();
    }

    // --- Bill frame ---
    private void openBillFrame() {
        String bookingId = tBookingId.getText().trim();
        if (bookingId.isEmpty()) { JOptionPane.showMessageDialog(this,"Enter Booking ID"); return; }

        // Load booking info from DB
        String customerName="";
        double roomCharge=0;
        String checkIn="", checkOut="";
        int roomsCount=0;

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT b.booking_id, c.name, r.price, b.check_in, b.check_out " +
                            "FROM Booking b JOIN Customer c ON b.customer_id=c.customer_id " +
                            "JOIN Room r ON b.room_id=r.room_id WHERE b.booking_id=?");
            ps.setInt(1, Integer.parseInt(bookingId));
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                customerName = rs.getString("name");
                roomCharge += rs.getDouble("price");
                checkIn = rs.getDate("check_in").toString();
                checkOut = rs.getDate("check_out").toString();
                roomsCount++;
            }
        } catch(Exception ex) { JOptionPane.showMessageDialog(this,"Booking not found!"); return; }

        double additional=0;
        try { additional = Double.parseDouble(tAdditional.getText().trim()); } catch(Exception ex){}

        double totalAmount = roomCharge + additional;

        JFrame billFrame = new JFrame("Bill - "+bookingId);
        billFrame.setSize(400,400);
        billFrame.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        mainPanel.add(centerLabel("Customer Name: "+customerName));
        mainPanel.add(centerLabel("Booking ID: "+bookingId));
        mainPanel.add(centerLabel("Number of Rooms: "+roomsCount));
        mainPanel.add(centerLabel("Room Charge: "+roomCharge));
        mainPanel.add(centerLabel("Additional Payment: "+additional));
        mainPanel.add(centerLabel("Total Amount: "+totalAmount));
        mainPanel.add(centerLabel("Check-In: "+checkIn));
        mainPanel.add(centerLabel("Check-Out: "+checkOut));
        mainPanel.add(Box.createVerticalStrut(20));

        JButton bMakePayment = new JButton("Make Payment");
        bMakePayment.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(bMakePayment);

        final String finalCustomer = customerName;
        final String finalBookingId = bookingId;
        final double finalAmount = totalAmount;

        bMakePayment.addActionListener(ev -> {
            billFrame.dispose();
            openPaymentMethodFrame(finalCustomer, finalAmount, finalBookingId);
        });

        billFrame.add(mainPanel);
        billFrame.setVisible(true);
    }

    // --- Payment method frame ---
    private void openPaymentMethodFrame(String customerName, double amount, String bookingId){
        JFrame methodFrame = new JFrame("Select Payment Method");
        methodFrame.setSize(300,200);
        methodFrame.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        panel.add(centerLabel("Choose Payment Method:"));
        panel.add(Box.createVerticalStrut(20));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,10,0));
        JButton bCash = new JButton("CASH");
        JButton bCard = new JButton("CARD");
        JButton bUpi = new JButton("UPI/ONLINE");
        btnPanel.add(bCash); btnPanel.add(bCard); btnPanel.add(bUpi);
        panel.add(btnPanel);

        final String finalCustomer = customerName;
        final double finalAmount = amount;
        final String finalBookingId = bookingId;

        bCash.addActionListener(e -> { methodFrame.dispose(); openIndividualPaymentFrame(finalCustomer, finalAmount,"CASH", finalBookingId); });
        bCard.addActionListener(e -> { methodFrame.dispose(); openIndividualPaymentFrame(finalCustomer, finalAmount,"CARD", finalBookingId); });
        bUpi.addActionListener(e -> { methodFrame.dispose(); openIndividualPaymentFrame(finalCustomer, finalAmount,"UPI", finalBookingId); });

        methodFrame.add(panel);
        methodFrame.setVisible(true);
    }

    // --- Individual payment frame ---
    private void openIndividualPaymentFrame(String customerName, double amount, String mode, String bookingId){
        JFrame payFrame = new JFrame(mode+" Payment");
        payFrame.setSize(300,200);
        payFrame.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        panel.add(centerLabel("Make Payment ("+mode+")"));
        panel.add(Box.createVerticalStrut(20));

        JButton bMarkPaid = new JButton("Mark as Paid");
        bMarkPaid.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(bMarkPaid);

        final String finalCustomer = customerName;
        final double finalAmount = amount;
        final String finalBookingId = bookingId;
        final String finalMode = mode;

        bMarkPaid.addActionListener(e -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO Payment(booking_id, amount, payment_method, status, payment_date) VALUES(?,?,?,?,?)");
                ps.setInt(1, Integer.parseInt(finalBookingId));
                ps.setDouble(2, finalAmount);
                ps.setString(3, finalMode);
                ps.setString(4, "PAID");
                ps.setTimestamp(5, new java.sql.Timestamp(new Date().getTime()));
                ps.executeUpdate();
            } catch(Exception ex) { JOptionPane.showMessageDialog(this,"Error saving payment: "+ex.getMessage()); }

            JOptionPane.showMessageDialog(payFrame,"Payment Successful!");
            payFrame.dispose();
            updatePayTable();
            updatePendingTable();
        });

        payFrame.add(panel);
        payFrame.setVisible(true);
    }

    private JLabel centerLabel(String text){
        JLabel lbl = new JLabel(text);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }

    // --- Load Payment History from DB ---
    private void updatePayTable(){
        DefaultTableModel model = (DefaultTableModel) payTable.getModel();
        model.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm");
        try(Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT p.payment_id, b.booking_id, c.name AS customer_name, p.amount, p.payment_method, p.status, p.payment_date " +
                    "FROM Payment p " +
                    "JOIN Booking b ON p.booking_id = b.booking_id " +
                    "JOIN Customer c ON b.customer_id = c.customer_id " +
                    "ORDER BY p.payment_date DESC")) {
            while(rs.next()){
                int payNo = rs.getInt("payment_id");
                String customer = rs.getString("customer_name");
                double amt = rs.getDouble("amount");
                String mode = rs.getString("payment_method");
                String status = rs.getString("status");
                Date date = rs.getTimestamp("payment_date");
                model.addRow(new Object[]{payNo, customer, amt, mode, status, sdf.format(date)});
            }
        } catch(Exception ex) { JOptionPane.showMessageDialog(this,"Error loading payments: "+ex.getMessage()); }
    }

    // --- Load Pending Payments (bookings without payment) ---
    private void updatePendingTable(){
        DefaultTableModel model = (DefaultTableModel) pendingTable.getModel();
        model.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm");
        try(Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT b.booking_id, c.name AS customer_name, r.price, 'PENDING' AS payment_method, 'UNPAID' AS status, b.check_in " +
                            "FROM Booking b " +
                            "JOIN Customer c ON b.customer_id = c.customer_id " +
                            "JOIN Room r ON b.room_id = r.room_id " +
                            "WHERE b.booking_id NOT IN (SELECT booking_id FROM Payment)")) {
            while(rs.next()){
                String customer = rs.getString("customer_name");
                double amt = rs.getDouble("price");
                String mode = rs.getString("payment_method");
                String status = rs.getString("status");
                Date date = rs.getDate("check_in");
                model.addRow(new Object[]{customer, amt, mode, status, sdf.format(date)});
            }
        } catch(Exception ex){ JOptionPane.showMessageDialog(this,"Error loading pending payments: "+ex.getMessage()); }
    }
}
