package opps;

import javax.swing.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BookingManager {

    private DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // --- Show Add Booking Dialog ---
    public void showAddBookingDialog() {
        JTextField customerField = new JTextField();
        JTextField roomField = new JTextField();
        JTextField checkInField = new JTextField();
        JTextField checkOutField = new JTextField();

        Object[] fields = {
                "Customer Name:", customerField,
                "Room ID:", roomField,
                "Check-In Date (yyyy-MM-dd):", checkInField,
                "Check-Out Date (yyyy-MM-dd):", checkOutField
        };

        int option = JOptionPane.showConfirmDialog(null, fields, "Add Booking", JOptionPane.OK_CANCEL_OPTION);
        if(option == JOptionPane.OK_OPTION) {
            String customer = customerField.getText().trim();
            int roomId;
            LocalDate checkIn, checkOut;

            try {
                roomId = Integer.parseInt(roomField.getText().trim());
                checkIn = LocalDate.parse(checkInField.getText().trim(), dateFormat);
                checkOut = LocalDate.parse(checkOutField.getText().trim(), dateFormat);
            } catch(Exception e) {
                JOptionPane.showMessageDialog(null, "Invalid input!");
                return;
            }

            addBookingToDB(customer, roomId, checkIn, checkOut);
        }
    }

    private void addBookingToDB(String customer, int roomId, LocalDate checkIn, LocalDate checkOut) {
        try (Connection conn = DatabaseConnection.getConnection()) {

            // --- Insert customer if not exists ---
            int customerId = -1;
            PreparedStatement psCheck = conn.prepareStatement("SELECT customer_id FROM Customer WHERE name=?");
            psCheck.setString(1, customer);
            ResultSet rs = psCheck.executeQuery();
            if(rs.next()) customerId = rs.getInt("customer_id");
            else {
                PreparedStatement psInsertCust = conn.prepareStatement(
                        "INSERT INTO Customer (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
                psInsertCust.setString(1, customer);
                psInsertCust.executeUpdate();
                ResultSet generatedKeys = psInsertCust.getGeneratedKeys();
                if(generatedKeys.next()) customerId = generatedKeys.getInt(1);
            }

            // --- Insert booking ---
            PreparedStatement psBooking = conn.prepareStatement(
                    "INSERT INTO Booking (customer_id, room_id, check_in, check_out, booking_date, status) " +
                            "VALUES (?, ?, ?, ?, ?, ?)");
            psBooking.setInt(1, customerId);
            psBooking.setInt(2, roomId);
            psBooking.setDate(3, Date.valueOf(checkIn));
            psBooking.setDate(4, Date.valueOf(checkOut));
            psBooking.setDate(5, Date.valueOf(LocalDate.now()));
            psBooking.setString(6, "Booked");
            psBooking.executeUpdate();

            // --- Update room status to Occupied ---
            PreparedStatement psRoom = conn.prepareStatement("UPDATE Room SET status='Occupied' WHERE room_id=?");
            psRoom.setInt(1, roomId);
            psRoom.executeUpdate();

            JOptionPane.showMessageDialog(null, "Booking added successfully!");

        } catch(Exception e) {
            JOptionPane.showMessageDialog(null, "Error adding booking: " + e.getMessage());
        }
    }

    // --- Check-In ---
    public void showCheckInDialog() {
        String bookingId = JOptionPane.showInputDialog("Enter Booking ID to check in:");
        if(bookingId != null) checkInBooking(bookingId.trim());
    }

    private void checkInBooking(String bookingId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("UPDATE Booking SET status='CheckedIn' WHERE booking_id=?");
            ps.setString(1, bookingId);
            int rows = ps.executeUpdate();
            if(rows > 0) JOptionPane.showMessageDialog(null, "Checked in successfully!");
            else JOptionPane.showMessageDialog(null, "Booking ID not found!");
        } catch(Exception e) {
            JOptionPane.showMessageDialog(null, "Error during check-in: " + e.getMessage());
        }
    }

    // --- Check-Out ---
    public void showCheckOutDialog() {
        String bookingId = JOptionPane.showInputDialog("Enter Booking ID to check out:");
        if(bookingId != null) checkOutBooking(bookingId.trim());
    }

    private void checkOutBooking(String bookingId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // --- Update booking status ---
            PreparedStatement ps = conn.prepareStatement("UPDATE Booking SET status='CheckedOut' WHERE booking_id=?");
            ps.setString(1, bookingId);
            int rows = ps.executeUpdate();

            // --- Update room to Available ---
            PreparedStatement psRoom = conn.prepareStatement(
                    "UPDATE Room r JOIN Booking b ON r.room_id=b.room_id SET r.status='Available' WHERE b.booking_id=?");
            psRoom.setString(1, bookingId);
            psRoom.executeUpdate();

            if(rows > 0) JOptionPane.showMessageDialog(null, "Checked out successfully!");
            else JOptionPane.showMessageDialog(null, "Booking ID not found!");
        } catch(Exception e) {
            JOptionPane.showMessageDialog(null, "Error during check-out: " + e.getMessage());
        }
    }

    // --- Cancel Booking ---
    public void showCancelBookingDialog() {
        String bookingId = JOptionPane.showInputDialog("Enter Booking ID to cancel booking:");
        if(bookingId != null) cancelBooking(bookingId.trim());
    }

    private void cancelBooking(String bookingId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // --- Get room id before deleting ---
            int roomId = -1;
            PreparedStatement psGet = conn.prepareStatement("SELECT room_id FROM Booking WHERE booking_id=?");
            psGet.setString(1, bookingId);
            ResultSet rs = psGet.executeQuery();
            if(rs.next()) roomId = rs.getInt("room_id");

            // --- Delete booking ---
            PreparedStatement psDel = conn.prepareStatement("DELETE FROM Booking WHERE booking_id=?");
            psDel.setString(1, bookingId);
            int rows = psDel.executeUpdate();

            // --- Set room available ---
            if(roomId != -1) {
                PreparedStatement psRoom = conn.prepareStatement("UPDATE Room SET status='Available' WHERE room_id=?");
                psRoom.setInt(1, roomId);
                psRoom.executeUpdate();
            }

            if(rows > 0) JOptionPane.showMessageDialog(null, "Booking canceled successfully!");
            else JOptionPane.showMessageDialog(null, "Booking ID not found!");

        } catch(Exception e) {
            JOptionPane.showMessageDialog(null, "Error canceling booking: " + e.getMessage());
        }
    }
}
