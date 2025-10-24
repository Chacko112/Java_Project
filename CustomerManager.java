package opps;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class CustomerManager {

    public static void showAddCustomerDialog(JFrame parent) {
        JTextField nameField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField();

        Object[] message = {
                "Name:", nameField,
                "Phone:", phoneField,
                "Email:", emailField
        };

        int option = JOptionPane.showConfirmDialog(parent, message, "Add Customer", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();

            if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "All fields are required!");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO Customer (name, phone, email) VALUES (?, ?, ?)")) {
                ps.setString(1, name);
                ps.setString(2, phone);
                ps.setString(3, email);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(parent, "Customer added successfully!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(parent, "Error adding customer: " + e.getMessage());
            }
        }
    }
}
