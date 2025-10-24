package opps;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OperationsPanel extends JPanel {

    public JTable checkinTable, checkoutTable, bookingTable, roomAvailabilityTable;
    public JSpinner dateSpinner;
    public List<Booking> bookings = new ArrayList<>();
    public List<RoomInfo> rooms = new ArrayList<>();
    public DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public OperationsPanel() {
        setLayout(new BorderLayout(10, 10));

        // --- Top Filter Panel ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        filterPanel.add(new JLabel("Select Date:"));

        dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd-MM-yyyy"));
        dateSpinner.setValue(new java.util.Date());
        filterPanel.add(dateSpinner);

        JButton filterBtn = new JButton("Filter");
        filterBtn.addActionListener(e -> refreshTables());
        filterPanel.add(filterBtn);

        JButton addCustomerBtn = new JButton("Add Customer");
        addCustomerBtn.addActionListener(e -> CustomerManager.showAddCustomerDialog(null));
        filterPanel.add(addCustomerBtn);

        JButton addRoomBtn = new JButton("Add Room");
        addRoomBtn.addActionListener(e -> RoomManager.showAddRoomDialog(null));
        filterPanel.add(addRoomBtn);

        add(filterPanel, BorderLayout.NORTH);

        // --- Tables ---
        JPanel centerPanel = new JPanel(new GridLayout(2, 2, 10, 10));

        checkinTable = new JTable(new DefaultTableModel(new String[]{"Booking ID", "Customer", "Room", "Check-in Date"}, 0));
        checkoutTable = new JTable(new DefaultTableModel(new String[]{"Booking ID", "Customer", "Room", "Check-out Date"}, 0));
        bookingTable = new JTable(new DefaultTableModel(new String[]{"Booking ID", "Customer", "Room", "Check-in", "Check-out", "Status"}, 0));
        roomAvailabilityTable = new JTable(new DefaultTableModel(new String[]{"Room No", "Type", "Status", "Price"}, 0));

        centerPanel.add(createTitledPanel("Check-ins", new JScrollPane(checkinTable)));
        centerPanel.add(createTitledPanel("Check-outs", new JScrollPane(checkoutTable)));
        centerPanel.add(createTitledPanel("Bookings", new JScrollPane(bookingTable)));
        centerPanel.add(createTitledPanel("Room Availability", new JScrollPane(roomAvailabilityTable)));

        add(centerPanel, BorderLayout.CENTER);

        // --- Action Buttons ---
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton checkInBtn = new JButton("Check In");
        JButton checkOutBtn = new JButton("Check Out");
        JButton addBookingBtn = new JButton("Add Booking");
        JButton cancelBookingBtn = new JButton("Cancel Booking");

        addBookingBtn.addActionListener(e -> showAddBookingDialog());
        checkInBtn.addActionListener(e -> performCheckIn());
        checkOutBtn.addActionListener(e -> performCheckOut());
        cancelBookingBtn.addActionListener(e -> performCancelBooking());

        actionPanel.add(checkInBtn);
        actionPanel.add(checkOutBtn);
        actionPanel.add(addBookingBtn);
        actionPanel.add(cancelBookingBtn);
        add(actionPanel, BorderLayout.SOUTH);

        // Load initial data
        loadRoomsFromDB();
        loadBookingsFromDB();
        refreshTables();
    }

    private JPanel createTitledPanel(String title, JScrollPane scrollPane) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    // ------------------ Load from Database ------------------

    public void loadRoomsFromDB() {
        rooms.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT room_id, room_type, price, status FROM Room")) {

            while (rs.next()) {
                int roomId = rs.getInt("room_id");
                String type = rs.getString("room_type");
                double price = rs.getDouble("price");
                String status = rs.getString("status");
                rooms.add(new RoomInfo(roomId, type, price, status));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading rooms: " + e.getMessage());
        }
    }

    public void loadBookingsFromDB() {
        bookings.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT booking_id, customer_id, room_id, check_in, check_out, booking_date, status FROM Booking")) {

            while (rs.next()) {
                String bookingId = String.valueOf(rs.getInt("booking_id"));
                int roomId = rs.getInt("room_id");
                String status = rs.getString("status");

                // Get customer name
                int custId = rs.getInt("customer_id");
                String customer = "";
                try (PreparedStatement ps = conn.prepareStatement("SELECT name FROM Customer WHERE customer_id=?")) {
                    ps.setInt(1, custId);
                    ResultSet rsCust = ps.executeQuery();
                    if (rsCust.next()) customer = rsCust.getString("name");
                }

                LocalDate checkIn = rs.getDate("check_in").toLocalDate();
                LocalDate checkOut = rs.getDate("check_out").toLocalDate();
                LocalDate bookingDate = rs.getDate("booking_date").toLocalDate();

                bookings.add(new Booking(bookingId, customer, roomId, checkIn, checkOut, bookingDate, status));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading bookings: " + e.getMessage());
        }
    }

    // ------------------ Refresh Tables ------------------

    public void refreshTables() {
        java.util.Date selected = (java.util.Date) dateSpinner.getValue();
        LocalDate selectedDate = selected.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        ((DefaultTableModel) checkinTable.getModel()).setRowCount(0);
        ((DefaultTableModel) checkoutTable.getModel()).setRowCount(0);
        ((DefaultTableModel) bookingTable.getModel()).setRowCount(0);
        ((DefaultTableModel) roomAvailabilityTable.getModel()).setRowCount(0);

        for (Booking b : bookings) {
            if (b.checkInDate.equals(selectedDate))
                ((DefaultTableModel) checkinTable.getModel())
                        .addRow(new Object[]{b.bookingId, b.customer, b.room, b.checkInDate.format(dateFormat)});
            if (b.checkOutDate.equals(selectedDate))
                ((DefaultTableModel) checkoutTable.getModel())
                        .addRow(new Object[]{b.bookingId, b.customer, b.room, b.checkOutDate.format(dateFormat)});
            ((DefaultTableModel) bookingTable.getModel())
                    .addRow(new Object[]{b.bookingId, b.customer, b.room,
                            b.checkInDate.format(dateFormat),
                            b.checkOutDate.format(dateFormat),
                            b.status});
        }

        for (RoomInfo r : rooms) {
            ((DefaultTableModel) roomAvailabilityTable.getModel())
                    .addRow(new Object[]{r.roomNumber, r.type, r.status, r.price});
        }
    }

    // ------------------ Booking Operations ------------------

    private void showAddBookingDialog() {
        if (rooms.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No rooms available to book.");
            return;
        }

        JTextField customerIdField = new JTextField();
        JComboBox<Integer> roomCombo = new JComboBox<>();
        for (RoomInfo r : rooms) if (r.status.equals("Available")) roomCombo.addItem(r.roomNumber);

        JTextField checkInField = new JTextField("yyyy-mm-dd");
        JTextField checkOutField = new JTextField("yyyy-mm-dd");

        Object[] message = {
                "Customer ID:", customerIdField,
                "Room Number:", roomCombo,
                "Check-in Date:", checkInField,
                "Check-out Date:", checkOutField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add Booking", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                int customerId = Integer.parseInt(customerIdField.getText());
                int roomNumber = (Integer) roomCombo.getSelectedItem();
                LocalDate checkIn = LocalDate.parse(checkInField.getText());
                LocalDate checkOut = LocalDate.parse(checkOutField.getText());

                addBookingToDB(customerId, roomNumber, checkIn, checkOut);
                loadBookingsFromDB();
                loadRoomsFromDB();
                refreshTables();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Invalid input or foreign key error: " + e.getMessage());
            }
        }
    }

    private void addBookingToDB(int customerId, int roomNumber, LocalDate checkIn, LocalDate checkOut) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO Booking (customer_id, room_id, check_in, check_out, booking_date, status) VALUES (?, ?, ?, ?, CURDATE(), 'Booked')")) {
            ps.setInt(1, customerId);
            ps.setInt(2, roomNumber);
            ps.setDate(3, java.sql.Date.valueOf(checkIn));
            ps.setDate(4, java.sql.Date.valueOf(checkOut));
            ps.executeUpdate();

            // Update room status to Occupied
            try (PreparedStatement ps2 = conn.prepareStatement("UPDATE Room SET status='Occupied' WHERE room_id=?")) {
                ps2.setInt(1, roomNumber);
                ps2.executeUpdate();
            }
        }
    }

    private void performCheckIn() {
        int row = checkinTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking to check in!");
            return;
        }
        String bookingId = checkinTable.getValueAt(row, 0).toString();
        updateBookingStatus(bookingId, "CheckedIn");
    }

    private void performCheckOut() {
        int row = checkoutTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking to check out!");
            return;
        }
        String bookingId = checkoutTable.getValueAt(row, 0).toString();
        updateBookingStatus(bookingId, "CheckedOut");
    }

    private void performCancelBooking() {
        int row = bookingTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking to cancel!");
            return;
        }
        String bookingId = bookingTable.getValueAt(row, 0).toString();
        updateBookingStatus(bookingId, "Cancelled");
    }

    private void updateBookingStatus(String bookingId, String status) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE Booking SET status=? WHERE booking_id=?")) {
            ps.setString(1, status);
            ps.setInt(2, Integer.parseInt(bookingId));
            ps.executeUpdate();

            // Update room if CheckedOut or Cancelled
            if (status.equals("CheckedOut") || status.equals("Cancelled")) {
                try (PreparedStatement ps2 = conn.prepareStatement(
                        "UPDATE Room r JOIN Booking b ON r.room_id = b.room_id SET r.status='Available' WHERE b.booking_id=?")) {
                    ps2.setInt(1, Integer.parseInt(bookingId));
                    ps2.executeUpdate();
                }
            }

            loadBookingsFromDB();
            loadRoomsFromDB();
            refreshTables();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating booking: " + e.getMessage());
        }
    }

    // ------------------ Helper Classes ------------------

    public static class Booking {
        public String bookingId;
        public String customer;
        public int room;
        public LocalDate checkInDate, checkOutDate, bookingDate;
        public String status;

        public Booking(String id, String cust, int room, LocalDate checkIn, LocalDate checkOut, LocalDate bookingDate, String status) {
            this.bookingId = id;
            this.customer = cust;
            this.room = room;
            this.checkInDate = checkIn;
            this.checkOutDate = checkOut;
            this.bookingDate = bookingDate;
            this.status = status;
        }
    }

    public static class RoomInfo {
        public int roomNumber;
        public String type;
        public double price;
        public String status;

        public RoomInfo(int num, String type, double price, String status) {
            this.roomNumber = num;
            this.type = type;
            this.price = price;
            this.status = status;
        }
    }
}
