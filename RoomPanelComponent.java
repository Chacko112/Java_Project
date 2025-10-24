package opps;

import javax.swing.*;
import java.awt.*;

public class RoomPanelComponent extends JPanel {
    private Room room;
    private JLabel label;

    public RoomPanelComponent(Room room) {
        this.room = room;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(Color.GRAY));
        setBackground(Color.LIGHT_GRAY);

        label = new JLabel("<html>Room: " + room.id + "<br>Type: " + room.type + "<br>Status: " + room.status + "<br>Price: " + room.price + "</html>");
        add(label, BorderLayout.CENTER);
    }
}
