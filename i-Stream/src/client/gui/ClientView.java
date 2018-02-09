package client.gui;

import client.controller.Controller;
import def.Constraints;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import server.clientadministration.ClientInfo;

/**
 *
 * @author Lars
 */
public class ClientView extends JPanel {

    private boolean userCanged;
    private final Controller controller;
    private Map<ClientInfo, JCheckBox> infos;

    public ClientView(Controller c) {
        controller = c;
        setBackground(Constraints.GUI_BLUE);
        setBorder(BorderFactory.createTitledBorder(Constraints.BORDER, "Clients"));
        setPreferredSize(new Dimension(150, 100));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JLabel jl = new JLabel("sync");
        jl.setFont(new Font(Font.DIALOG, Font.ITALIC, 10));
        add(jl);
        infos = new HashMap<ClientInfo, JCheckBox>();
        userCanged = true;
    }

    public void addClientInfo(final ClientInfo c) {
        System.out.println("cv add client " + c);
        JCheckBox syn = new JCheckBox(c.name);
        // Kein ItemListener sonst feuert der auch bei remote Änderungen
        syn.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                boolean selected = !((JCheckBox) e.getSource()).isSelected();
                if (selected) {
                    controller.changeSynchronization(c.id, true);
                } else {
                    controller.changeSynchronization(c.id, false);
                }
            }
        });
        syn.setBackground(Constraints.GUI_BLUE);
        infos.put(c, syn);
        add(infos.get(c));
        getTopLevelAncestor().setVisible(true);
    }

    public void removeClientID(ClientInfo c) {
//        if (c != null) {
        remove(infos.remove(c));
//        }
        repaint();
        setVisible(true);
    }

    public void updateSynchronization(Collection<ClientInfo> clients) {
        System.out.println("cv update sync " + Arrays.toString(clients.toArray()));
        for (ClientInfo c : infos.keySet()) {
            if (clients.contains(c)) {
                infos.get(c).setSelected(true);
            } else {
                infos.get(c).setSelected(false);
            }
        }
        getTopLevelAncestor().setVisible(true);
    }
}
