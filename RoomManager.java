package opps;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RoomManager {

    // Show dialog to add new room
    public static void showAddRoomDialog(JFrame parent) {
        JTextField typeField = new JTextField();
        JTextField priceField = new JTextField();

        Object[] message = {
                "Type (Single/Double/Suite):", typeField,
                "Price:", priceField
        };

        int option = JOptionPane.showConfirmDialog(parent, message, "Add Room", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String type = typeField.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim());

                if (type.isEmpty()) {
                    JOptionPane.showMessageDialog(parent, "Type is required!");
                    return;
                }

                int roomNumber = getNextRoomId(); // auto-generate unique room_id

                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                             "INSERT INTO Room (room_id, room_type, price, status) VALUES (?, ?, ?, 'Available')")) {
                    ps.setInt(1, roomNumber);
                    ps.setString(2, type);
                    ps.setDouble(3, price);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(parent, "Room added successfully! Room ID: " + roomNumber);
                }

            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(parent, "Invalid price!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(parent, "Error adding room: " + e.getMessage());
            }
        }
    }

    // Generate next unique room_id
    private static int getNextRoomId() throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT MAX(room_id) AS max_id FROM Room");
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("max_id") + 1;
            } else {
                return 1; // If no rooms exist yet
            }
        }
    }
}
