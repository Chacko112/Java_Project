package opps;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UserPanel extends JPanel {

    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton addButton, removeButton;

    public UserPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(Color.WHITE);

        // --- Table setup ---
        tableModel = new DefaultTableModel(new Object[]{"Username"}, 0);
        employeeTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(employeeTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Employees"));
        add(scrollPane, BorderLayout.CENTER);

        // --- Form panel for adding/removing employees ---
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Add New Employee"));
        formPanel.setBackground(Color.WHITE);

        formPanel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        formPanel.add(usernameField);

        formPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);

        addButton = new JButton("Add Employee");
        removeButton = new JButton("Remove Selected");
        formPanel.add(addButton);
        formPanel.add(removeButton);

        add(formPanel, BorderLayout.SOUTH);

        // --- Button actions ---
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword()).trim();

                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(UserPanel.this, "Please enter both username and password.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Add to table (can later add to database)
                tableModel.addRow(new Object[]{username});
                usernameField.setText("");
                passwordField.setText("");

                JOptionPane.showMessageDialog(UserPanel.this, "Employee added successfully!");
            }
        });

        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = employeeTable.getSelectedRow();
                if (selectedRow != -1) {
                    int confirm = JOptionPane.showConfirmDialog(UserPanel.this, "Are you sure you want to remove this employee?", "Confirm Remove", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        tableModel.removeRow(selectedRow);
                        JOptionPane.showMessageDialog(UserPanel.this, "Employee removed successfully!");
                    }
                } else {
                    JOptionPane.showMessageDialog(UserPanel.this, "Please select an employee to remove.", "No Selection", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
    }
}
