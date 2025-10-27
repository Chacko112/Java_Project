package opps;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.sql.*;

// --- Room class ---
class Room {
    public int roomNumber;
    public String roomType;
    public String status;
    public double price;
    public String guestName;
    public String guestPhone;

    public Room(int roomNumber, String roomType, double price) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.price = price;
        this.status = "Available";
        this.guestName = "";
        this.guestPhone = "";
    }
}

// --- RoomPanelComponent ---
class RoomPanelComponent extends JPanel {
    public Room room;
    public JLabel roomLabel, statusLabel;
    public JButton actionButton, maintenanceButton;

    public RoomPanelComponent(Room room, ActionListener bookListener, ActionListener maintenanceListener) {
        this.room = room;
        setLayout(new BorderLayout(5,5));
        setPreferredSize(new Dimension(150,140));
        setBorder(BorderFactory.createLineBorder(Color.GRAY,2));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        roomLabel = new JLabel("Room " + room.roomNumber);
        roomLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JLabel typeLabel = new JLabel(room.roomType);
        typeLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        JLabel priceLabel = new JLabel("₹" + room.price + "/night");
        priceLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        statusLabel = new JLabel(room.status);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));

        infoPanel.add(roomLabel);
        infoPanel.add(typeLabel);
        infoPanel.add(priceLabel);
        infoPanel.add(statusLabel);

        JPanel buttonPanel = new JPanel(new GridLayout(2,1,2,2));
        actionButton = new JButton();
        actionButton.addActionListener(bookListener);
        maintenanceButton = new JButton("Maintenance");
        maintenanceButton.setFont(new Font("Arial", Font.PLAIN, 10));
        maintenanceButton.addActionListener(maintenanceListener);
        buttonPanel.add(actionButton);
        buttonPanel.add(maintenanceButton);

        add(infoPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        updateDisplay();
    }

    public void updateDisplay() {
        switch(room.status) {
            case "Available":
                setBackground(new Color(144,238,144));
                statusLabel.setForeground(new Color(0,128,0));
                actionButton.setText("Book Room");
                actionButton.setEnabled(true);
                maintenanceButton.setEnabled(true);
                break;
            case "Occupied":
                setBackground(new Color(255,182,193));
                statusLabel.setForeground(new Color(178,34,34));
                actionButton.setText("Check Out");
                actionButton.setEnabled(true);
                maintenanceButton.setEnabled(false);
                break;
            case "Maintenance":
                setBackground(new Color(255,255,153));
                statusLabel.setForeground(new Color(184,134,11));
                actionButton.setText("Unavailable");
                actionButton.setEnabled(false);
                maintenanceButton.setText("End Maintenance");
                maintenanceButton.setEnabled(true);
                break;
        }
        statusLabel.setText(room.status);
    }
}

// --- RoomDAO for DB integration ---
class RoomDAO {

    public static ArrayList<Room> getAllRooms() {
        ArrayList<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM Rooms";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Room room = new Room(
                        rs.getInt("room_number"),
                        rs.getString("room_type"),
                        rs.getDouble("price")
                );
                room.status = rs.getBoolean("is_available") ? "Available" : "Occupied";
                rooms.add(room);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading rooms from database!");
        }
        return rooms;
    }

    public static void addRoom(int roomNumber, String type, double price) {
        String sql = "INSERT INTO Rooms (room_number, room_type, price, is_available) VALUES (?, ?, ?, TRUE)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roomNumber);
            stmt.setString(2, type);
            stmt.setDouble(3, price);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error adding room! Maybe the room number already exists.");
        }
    }

    public static void removeRoom(int roomNumber) {
        String sql = "DELETE FROM Rooms WHERE room_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roomNumber);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error removing room!");
        }
    }

    public static void updateRoomStatus(int roomNumber, String status) {
        String sql = "UPDATE Rooms SET is_available = ? WHERE room_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, status.equalsIgnoreCase("Available"));
            stmt.setInt(2, roomNumber);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

// --- RoomBookingDialog for prefilled bookings ---
class RoomBookingDialog extends JDialog {

    private JTextField roomNumberField;
    private JTextField roomTypeField;
    private JTextField customerField;
    private boolean confirmed = false;
    private String bookingId;

    public RoomBookingDialog(Window parent, int roomNumber, String roomType) {
        super(parent, "Book Room", ModalityType.APPLICATION_MODAL);
        setLayout(new GridLayout(3,2,10,10));

        add(new JLabel("Room Number:"));
        roomNumberField = new JTextField(String.valueOf(roomNumber));
        roomNumberField.setEditable(false);
        add(roomNumberField);

        add(new JLabel("Room Type:"));
        roomTypeField = new JTextField(roomType);
        roomTypeField.setEditable(false);
        add(roomTypeField);

        add(new JLabel("Customer Name:"));
        customerField = new JTextField();
        add(customerField);

        JButton confirmBtn = new JButton("Confirm");
        confirmBtn.addActionListener(e -> {
            bookingId = "B" + System.currentTimeMillis();
            confirmed = true;
            dispose();
        });
        add(confirmBtn);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());
        add(cancelBtn);

        pack();
        setLocationRelativeTo(parent);
    }

    public boolean isConfirmed() { return confirmed; }
    public String getBookingId() { return bookingId; }
    public String getCustomerName() { return customerField.getText().trim(); }
}

// --- RoomsPanel ---
public class RoomsPanel extends JPanel {

    public ArrayList<Room> rooms;
    public ArrayList<RoomPanelComponent> roomPanels;
    public JPanel roomsDisplayPanel;
    public JLabel statsLabel;
    public OperationsPanel operationsPanelReference;

    private JButton addRoomButton, removeRoomButton;

    public RoomsPanel(OperationsPanel opsPanel) {
        this.operationsPanelReference = opsPanel;
        setLayout(new BorderLayout(10,10));

        // Rooms display panel first
        roomsDisplayPanel = new JPanel(new GridLayout(0,4,10,10));
        roomsDisplayPanel.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(roomsDisplayPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        add(scrollPane, BorderLayout.CENTER);

        // Load rooms first
        loadRooms();

        // Header and footer
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    private void loadRooms() {
        rooms = RoomDAO.getAllRooms();
        if (rooms == null) rooms = new ArrayList<>();
        displayRooms();
    }

    private void displayRooms() {
        if (roomsDisplayPanel == null) return;
        roomsDisplayPanel.removeAll();
        roomPanels = new ArrayList<>();

        for (Room room : rooms) {
            RoomPanelComponent panel = new RoomPanelComponent(room,
                    new RoomActionListener(room),
                    new MaintenanceActionListener(room));
            roomPanels.add(panel);
            roomsDisplayPanel.add(panel);
        }

        roomsDisplayPanel.revalidate();
        roomsDisplayPanel.repaint();

        if (statsLabel != null) updateStats();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(41,128,185));
        panel.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        JLabel titleLabel = new JLabel("Hotel Rooms");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,5,0));
        buttonPanel.setBackground(new Color(41,128,185));

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadRooms());

        addRoomButton = new JButton("Add Room");
        removeRoomButton = new JButton("Remove Room");
        addRoomButton.addActionListener(e -> showAddRoomDialog());
        removeRoomButton.addActionListener(e -> removeSelectedRoom());

        buttonPanel.add(refreshButton);
        buttonPanel.add(addRoomButton);
        buttonPanel.add(removeRoomButton);

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(buttonPanel, BorderLayout.EAST);
        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(236,240,241));
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        statsLabel = new JLabel();
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        updateStats();
        panel.add(statsLabel);
        return panel;
    }

    private void updateStats() {
        if (rooms == null) return;
        int available=0, occupied=0, maintenance=0;
        double totalRevenue=0;
        for (Room r: rooms){
            switch(r.status){
                case "Available": available++; break;
                case "Occupied": occupied++; totalRevenue+=r.price; break;
                case "Maintenance": maintenance++; break;
            }
        }
        int total = rooms.size();
        double occupancy = total > 0 ? (occupied*100.0)/total : 0;
        statsLabel.setText(String.format(
                "Total Rooms: %d | Available: %d | Occupied: %d | Maintenance: %d | Occupancy: %.1f%% | Daily Revenue: ₹%.2f",
                total, available, occupied, maintenance, occupancy, totalRevenue));
    }

    // --- Room Actions ---
    class RoomActionListener implements ActionListener {
        private Room room;
        public RoomActionListener(Room room){this.room=room;}
        public void actionPerformed(ActionEvent e){
            if (room.status.equals("Available")) bookRoom(room);
            else if (room.status.equals("Occupied")) checkoutRoom(room);
        }
    }

    class MaintenanceActionListener implements ActionListener {
        private Room room;
        public MaintenanceActionListener(Room room){this.room=room;}
        public void actionPerformed(ActionEvent e){
            if (room.status.equals("Maintenance")) endMaintenance(room);
            else if (room.status.equals("Available")) startMaintenance(room);
        }
    }

    // --- Booking ---
    private void bookRoom(Room room) {
        if (operationsPanelReference != null) {
            RoomBookingDialog dialog = new RoomBookingDialog(
                    SwingUtilities.getWindowAncestor(this),
                    room.roomNumber,
                    room.roomType
            );
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                OperationsPanel.Booking newBooking = new OperationsPanel.Booking(
                        dialog.getBookingId(),
                        room.roomNumber,
                        dialog.getCustomerName()
                );
                operationsPanelReference.bookings.add(newBooking);

                room.status = "Occupied";
                RoomDAO.updateRoomStatus(room.roomNumber, "Occupied");
                updateRoomDisplay(room);
                updateStats();
            }
        }
    }

    // --- Checkout / Maintenance / Add/Remove rooms ---
    private void checkoutRoom(Room room){
        String bookingId = null;
        for (OperationsPanel.Booking b: operationsPanelReference.bookings){
            if (b.room == room.roomNumber && room.status.equals("Occupied")){
                bookingId = b.bookingId; break;
            }
        }
        if (bookingId != null){
            int result = JOptionPane.showConfirmDialog(this,
                    "Checkout room " + room.roomNumber + "?",
                    "Confirm Checkout", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION){
                BookingManager.checkOut(operationsPanelReference.bookings, bookingId);
                room.status="Available";
                RoomDAO.updateRoomStatus(room.roomNumber, "Available");
                room.guestName="";
                room.guestPhone="";
                updateRoomDisplay(room);
                updateStats();
            }
        }
    }

    private void startMaintenance(Room room){
        room.status="Maintenance";
        RoomDAO.updateRoomStatus(room.roomNumber, "Maintenance");
        updateRoomDisplay(room);
        updateStats();
    }

    private void endMaintenance(Room room){
        room.status="Available";
        RoomDAO.updateRoomStatus(room.roomNumber, "Available");
        updateRoomDisplay(room);
        updateStats();
    }

    private void updateRoomDisplay(Room room){
        for (RoomPanelComponent panel: roomPanels){
            if (panel.room==room){
                panel.updateDisplay(); break;
            }
        }
    }

    // --- Add / Remove Room ---
    private void showAddRoomDialog() {
        JTextField roomNumberField = new JTextField();
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Single","Double","Suite"});
        JTextField priceField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(3,2,5,5));
        panel.add(new JLabel("Room Number:")); panel.add(roomNumberField);
        panel.add(new JLabel("Room Type:")); panel.add(typeBox);
        panel.add(new JLabel("Price:")); panel.add(priceField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Room", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int roomNumber = Integer.parseInt(roomNumberField.getText().trim());
                String type = (String) typeBox.getSelectedItem();
                double price = Double.parseDouble(priceField.getText().trim());
                RoomDAO.addRoom(roomNumber, type, price);
                loadRooms();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Enter valid numeric values for room number and price!");
            }
        }
    }

    private void removeSelectedRoom() {
        String roomNumStr = JOptionPane.showInputDialog(this, "Enter Room Number to remove:");
        if (roomNumStr == null) return;
        try {
            int roomNumber = Integer.parseInt(roomNumStr.trim());
            Room toRemove = null;
            for (Room r: rooms) { if (r.roomNumber==roomNumber) {toRemove=r; break;} }
            if (toRemove==null) { JOptionPane.showMessageDialog(this,"Room not found!"); return; }
            int confirm = JOptionPane.showConfirmDialog(this,"Are you sure to remove Room " + roomNumber + "?",
                    "Confirm Remove", JOptionPane.YES_NO_OPTION);
            if (confirm!=JOptionPane.YES_OPTION) return;

            RoomDAO.removeRoom(roomNumber);
            loadRooms();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,"Enter a valid room number!");
        }
    }
}
