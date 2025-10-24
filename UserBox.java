package opps;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UserBox extends JPanel {

    public UserBox(String managerName) {
        setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Manager name label
        JLabel nameLabel = new JLabel(managerName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setForeground(new Color(41, 128, 185));
        add(nameLabel);

        // Logout button
        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(new Color(231, 76, 60));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setFont(new Font("Arial", Font.PLAIN, 12));
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                        UserBox.this,
                        "Are you sure you want to logout?",
                        "Confirm Logout",
                        JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    // Close current window
                    SwingUtilities.getWindowAncestor(UserBox.this).dispose();
                    // Open login page
                    new LoginPage().setVisible(true);
                }
            }
        });

        add(logoutButton);
    }
}
