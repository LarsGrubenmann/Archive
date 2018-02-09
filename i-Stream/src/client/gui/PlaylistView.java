package client.gui;

import client.controller.Controller;
import def.Constraints;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import musicinfo.PlaylistID;

/**
 *
 * @author Lars
 */
public class PlaylistView extends JPanel {

    private JTable jtPlaylists;
    private DefaultTableModel dtmPlaylists;

    public PlaylistView(final Controller c) {
        this.setBackground(Constraints.GUI_GREEN);
        setBorder(BorderFactory.createTitledBorder(Constraints.BORDER, "Playlists"));
        setLayout(new BorderLayout());
        // Controllpanel
        add(initControllPanel(), BorderLayout.NORTH);
        // Table
        dtmPlaylists = new DefaultTableModel(new String[]{"Name", "Duration"}, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jtPlaylists = new JTable(dtmPlaylists);
        jtPlaylists.getColumnModel().getColumn(1).setPreferredWidth(20);
        jtPlaylists.getTableHeader().setPreferredSize(new Dimension(0, 20));
        jtPlaylists.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jtPlaylists.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                int row = jtPlaylists.getSelectedRow();
                if (e.getClickCount() == 2 && row > -1) {
                    c.selectPlaylist((PlaylistID) jtPlaylists.getValueAt(row, 0));
                }
            }
        });
        JScrollPane jsp = new JScrollPane(jtPlaylists);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(jsp, BorderLayout.CENTER);
    }

    public JTable getJtPlaylists() {
        return jtPlaylists;
    }

    public JPanel initControllPanel() {
        JPanel jp = new JPanel();
        jp.setBackground(Constraints.GUI_GREEN);
        return jp;
    }

    public void updatePlaylists(Collection<PlaylistID> playlists) {
        dtmPlaylists.setRowCount(0);
        int i = -1;
        for (PlaylistID p : playlists) {
            i++;
            int d = (int) (p.getDuration() / 1e6);
            dtmPlaylists.addRow(new Object[]{
                        p,
                        (d / 60) + ":" + ((d % 60) >= 10 ? (d % 60) : "0" + (d % 60))
                    });
        }
    }
}
