package opps;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginPage extends JFrame implements ActionListener {

    public JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleBox;
    private JButton loginButton, resetButton;

    public LoginPage() {
        setTitle("Hotel Management System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 280);  // window size
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 245, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Hotel Management System");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridwidth = 1;

        JLabel roleLabel = new JLabel("Login as:");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(roleLabel, gbc);

        roleBox = new JComboBox<>(new String[]{"Admin", "Employee"});
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(roleBox, gbc);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(userLabel, gbc);

        usernameField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(usernameField, gbc);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(passLabel, gbc);

        passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(passwordField, gbc);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(245, 245, 245));
        loginButton = new JButton("Login");
        resetButton = new JButton("Reset");
        loginButton.addActionListener(this);
        resetButton.addActionListener(this);
        buttonPanel.add(loginButton);
        buttonPanel.add(resetButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        add(panel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            String selectedRole = (String) roleBox.getSelectedItem();
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            // Authenticate user from database
            String dbRole = UserAuthentication.authenticateUser(username, password);

            if (dbRole != null && dbRole.equalsIgnoreCase(selectedRole)) {
                JOptionPane.showMessageDialog(this, "Welcome, " + username + "!");
                
                if (dbRole.equalsIgnoreCase("Admin")) {
                    new AdminMainFrame(username).setVisible(true);
                } else if (dbRole.equalsIgnoreCase("Employee")) {
                    new EmployeeMainFrame(username).setVisible(true);
                }
                
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Credentials!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == resetButton) {
            usernameField.setText("");
            passwordField.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginPage().setVisible(true));
    }
}
