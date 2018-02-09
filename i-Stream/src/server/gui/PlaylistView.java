package server.gui;

import def.Constraints;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.table.DefaultTableModel;
import musicinfo.PlaylistID;
import server.controller.ILocal;

/**
 *
 * @author
 */
public class PlaylistView extends JPanel {

    private JTable jtPlaylists;
    private DefaultTableModel dtmPlaylists;
    private JButton jbNew;
    private JTextField jtfPlName;
    private final MainFrame mainf;
    private final ILocal local;

    public PlaylistView(final MainFrame mf, final ILocal il) {
        mainf = mf;
        local = il;
        setBorder(BorderFactory.createTitledBorder(Constraints.BORDER, "Playlists"));
        //setBorder(BorderFactory.);
        setLayout(new BorderLayout());
        setBackground(Constraints.GUI_GREEN);

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
        jtPlaylists.setSelectionBackground(Constraints.GUI_GREEN_SELECT);
        jtPlaylists.getTableHeader().setPreferredSize(new Dimension(0, 20));
        jtPlaylists.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jtPlaylists.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                int row = jtPlaylists.getSelectedRow();
                if (row > -1) {
                    mf.startEditPlaylist((PlaylistID) jtPlaylists.getValueAt(row, 0));
                }
            }
        });
        JScrollPane jsp = new JScrollPane(jtPlaylists);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(jsp, BorderLayout.CENTER);
    }

    public JTable gettlPlaylists() {
        return jtPlaylists;
    }

    public JPanel initControllPanel() {
        JPanel jp = new JPanel();
        jp.setLayout(new GridBagLayout());
        jp.setBackground(Constraints.GUI_GREEN);
        GridBagConstraints gbc = new GridBagConstraints();
        /**
         * New Playlist-Name
         */
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        jtfPlName = new JTextField();
        CaretListener cl = new CaretListener() {

            public void caretUpdate(CaretEvent e) {
                if (jtfPlName.hasFocus() && !jtfPlName.getText().equals("")) {
                    jbNew.setEnabled(true);
                } else {
                    jbNew.setEnabled(false);
                }
            }
        };
        ActionListener al = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (!jtfPlName.getText().equals("")) {
                    String plName = jtfPlName.getText();
                    local.createNewPlaylist(plName);
                    jtfPlName.setText("");
                    jbNew.setEnabled(false);
                }
            }
        };
        jtfPlName.setColumns(24);
        jtfPlName.addCaretListener(cl);
        jtfPlName.addActionListener(al);
        jp.add(jtfPlName, gbc);
        /**
         * New-Button
         */
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        //jbNew = new JButton("New", Constraints.getResourceIcon("edit_add.png"));
        jbNew = new JButton();
        jbNew.setIcon(Constraints.getResourceIcon("new_normal.png"));
        jbNew.setDisabledIcon(Constraints.getResourceIcon("new_mute.png"));
        jbNew.setPressedIcon(Constraints.getResourceIcon("new_down.png"));
        jbNew.setRolloverIcon(Constraints.getResourceIcon("new_marked.png"));
        jbNew.setSelectedIcon(Constraints.getResourceIcon("new_down"));
        jbNew.setFocusPainted(false);
        jbNew.setContentAreaFilled(false);
        //jbNew.setBackground(Constraints.GUI_GREEN);
        jbNew.setSize(new Dimension(55, 22));
        jbNew.setMargin(new Insets(0, 0, 0, 0));
        jbNew.setEnabled(false);
        jbNew.addActionListener(al);
        jp.add(jbNew, gbc);
        return jp;
    }

    public void updatePlaylists(Collection<PlaylistID> playlists) {
        int row = jtPlaylists.getSelectedRow();
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
        if (row >= jtPlaylists.getRowCount()) {
            row = jtPlaylists.getRowCount() - 1;
        }
        if (row > -1) {
            jtPlaylists.setRowSelectionInterval(row, row);
            mainf.startEditPlaylist((PlaylistID) jtPlaylists.getValueAt(row, 0));
        }
    }
}
