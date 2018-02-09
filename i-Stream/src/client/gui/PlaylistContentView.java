package client.gui;

import client.controller.Controller;
import def.Constraints;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import musicinfo.Song;

/**
 *
 * @author
 */
public class PlaylistContentView extends JPanel {

    private JTable jtPlaylist;
    private DefaultTableModel dtmPlaylist;
    private TableRowSorter trsPlaylist;
    private JButton jbBack;
    private JTextField jtfSearch;
    private JButton jbSearch;
    private MainFrame mf;
    private Comparator compar;

    public PlaylistContentView(final MainFrame mf, final Controller c) {
        this.mf = mf;

        compar = new Comparator() {

            public int compare(Object o1, Object o2) {
                if (o1.toString().length() == o2.toString().length()) {
                    return o1.toString().compareTo(o2.toString());
                } else {
                    return o1.toString().length() - o2.toString().length();
                }
            }
        };

        setBackground(Constraints.GUI_YELLOW);
        setBorder(BorderFactory.createTitledBorder(Constraints.BORDER, "Playlists"));
        setLayout(new BorderLayout());

        // Controllpanel
        add(initControllPanel(c), BorderLayout.NORTH);

        dtmPlaylist = new DefaultTableModel(new String[]{"Track", "Title", "Artist", "Genre", "Duration"}, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jtPlaylist = new JTable(dtmPlaylist);
        trsPlaylist = new TableRowSorter(dtmPlaylist);
        trsPlaylist.setComparator(0, compar);
        trsPlaylist.setComparator(4, compar);
        jtPlaylist.setRowSorter(trsPlaylist);
        jtPlaylist.getColumnModel().getColumn(0).setPreferredWidth(10);
        jtPlaylist.getColumnModel().getColumn(3).setPreferredWidth(20);
        jtPlaylist.getColumnModel().getColumn(4).setPreferredWidth(20);
        jtPlaylist.getTableHeader().setPreferredSize(new Dimension(0, 20));
        jtPlaylist.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int track = getJtPlaylist().getSelectedRow();
                    if (track > -1) {
                        track = (Integer) jtPlaylist.getValueAt(track, 0);
                        c.play(track);
                    }
                }
            }
        });

        jtPlaylist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane jsp = new JScrollPane(jtPlaylist);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(jsp, BorderLayout.CENTER);
    }

    public JPanel initControllPanel(final Controller c) {
        JPanel jp = new JPanel();
        jp.setBackground(Constraints.GUI_YELLOW);
        jp.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        /**
         * Zurück zur Playlistauswahl
         */
        gbc.gridx = 0;
        gbc.gridy = 0;
        jbBack = new JButton("Back", Constraints.getResourceIcon("left.png"));
        jbBack.setBackground(Constraints.GUI_YELLOW);
        jbBack.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                //mf.setAsMainView(mf.vPlaylists);
            }
        });
        jp.add(jbBack, gbc);
        /**
         * Suche
         */
        ActionListener al = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                updatePlaylist(c.searchSongInPlaylist(jtfSearch.getText()));
            }
        };
        jbSearch = new JButton("Filter", Constraints.getResourceIcon("xmag.png"));
        jbSearch.setBackground(Constraints.GUI_YELLOW);
        jbSearch.addActionListener(al);
        gbc.gridx = 2;
        jp.add(jbSearch, gbc);
        /**
         * Suche
         */
        jtfSearch = new JTextField();
        jtfSearch.addActionListener(al);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        jp.add(jtfSearch, gbc);
        return jp;
    }

//    public void updatePlaylist(Collection<Song> songs) {
//        dtmPlaylist.setRowCount(0);
//        int i = -1;
//        for (Song s : songs) {
//            i++;
//            int d = (int) (s.getDuration() / 1e6);
//            dtmPlaylist.addRow(new Object[]{
//                        new Integer(i + 1),
//                        s,
//                        s.getArtist(),
//                        s.getGenre(),
//                        (d / 60) + ":" + ((d % 60) >= 10 ? (d % 60) : "0" + (d % 60))
//                    });
//        }
//    }

    // TODO ^^ merge
    public void updatePlaylist(Map<Integer, Song> songs) {
        dtmPlaylist.setRowCount(0);
        Song s;
        for (Integer k : songs.keySet()) {
            s = songs.get(k);
            int d = (int) (s.getDuration() / 1e6);
            dtmPlaylist.addRow(new Object[]{
                        k,
                        s,
                        s.getArtist(),
                        s.getGenre(),
                        (d / 60) + ":" + ((d % 60) >= 10 ? (d % 60) : "0" + (d % 60))
                    });
        }
    }

    public void upSelect(){
        int s = jtPlaylist.getSelectedRow();
        jtPlaylist.clearSelection();
        jtPlaylist.changeSelection(s+1, 0, true, false);
    }

    public JTable getJtPlaylist() {
        return jtPlaylist;
    }
}
