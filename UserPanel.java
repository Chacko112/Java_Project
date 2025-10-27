package opps;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class UserPanel extends JPanel {

    private JTable userTable;
    private DefaultTableModel tableModel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleBox;
    private JButton addButton, removeButton;

    public UserPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(Color.WHITE);

        // Table setup
        tableModel = new DefaultTableModel(new Object[]{"User ID", "Username", "Role"}, 0);
        userTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(userTable);
        add(scrollPane, BorderLayout.CENTER);

        // Load users from DB
        loadUsers();

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Add New User"));
        formPanel.setBackground(Color.WHITE);

        formPanel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        formPanel.add(usernameField);

        formPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);

        formPanel.add(new JLabel("Role:"));
        roleBox = new JComboBox<>(new String[]{"Admin", "Employee"});
        formPanel.add(roleBox);

        addButton = new JButton("Add User");
        removeButton = new JButton("Remove Selected");
        formPanel.add(addButton);
        formPanel.add(removeButton);

        add(formPanel, BorderLayout.SOUTH);

        // Button actions
        addButton.addActionListener(e -> addUser());
        removeButton.addActionListener(e -> removeUser());
    }

    // Load all users from the database
    private void loadUsers() {
        tableModel.setRowCount(0); // clear table
        String sql = "SELECT user_id, username, role FROM Users";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("user_id"));
                row.add(rs.getString("username"));
                row.add(rs.getString("role"));
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading users from database!");
        }
    }

    // Add a new user to the database
    private void addUser() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String role = (String) roleBox.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password!");
            return;
        }

        String sql = "INSERT INTO Users (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "User added successfully!");
                usernameField.setText("");
                passwordField.setText("");
                loadUsers(); // refresh table
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding user! Maybe username already exists.");
        }
    }

    // Remove selected user from database
    private void removeUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to remove!");
            return;
        }

        int userId = (int) tableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this user?");
        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM Users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "User removed successfully!");
                loadUsers(); // refresh table
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error removing user!");
        }
    }
}
