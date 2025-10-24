package opps;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class RoomsPanel extends JPanel {

    private JPanel roomsDisplayPanel;
    private ArrayList<RoomPanelComponent> roomPanels;
    private ArrayList<Room> rooms;
    private JLabel statsLabel;

    public RoomsPanel() {
        setLayout(new BorderLayout(10, 10));

        add(createHeaderPanel(), BorderLayout.NORTH);

        roomsDisplayPanel = new JPanel(new GridLayout(0, 4, 10, 10));
        roomsDisplayPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(roomsDisplayPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        add(createFooterPanel(), BorderLayout.SOUTH);

        loadRoomsFromDB();
        displayRooms();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(41, 128, 185));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("Hotel Rooms");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            loadRoomsFromDB();
            displayRooms();
        });

        JButton addRoomBtn = new JButton("Add Room");
        addRoomBtn.addActionListener(e -> {
            RoomManager.showAddRoomDialog(null);
            loadRoomsFromDB();
            displayRooms();
        });

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        rightPanel.add(refreshButton);
        rightPanel.add(addRoomBtn);

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);
        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(236, 240, 241));
        statsLabel = new JLabel();
        panel.add(statsLabel);
        return panel;
    }

    private void loadRoomsFromDB() {
        rooms = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT room_id, room_type, price, status FROM Room")) {

            while (rs.next()) {
                int id = rs.getInt("room_id");
                String type = rs.getString("room_type");
                double price = rs.getDouble("price");
                String status = rs.getString("status");
                rooms.add(new Room(id, type, price, status));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading rooms: " + e.getMessage());
        }
    }

    private void displayRooms() {
        roomsDisplayPanel.removeAll();
        roomPanels = new ArrayList<>();

        for (Room room : rooms) {
            RoomPanelComponent panel = new RoomPanelComponent(room);
            roomPanels.add(panel);
            roomsDisplayPanel.add(panel);
        }

        roomsDisplayPanel.revalidate();
        roomsDisplayPanel.repaint();
    }
}
