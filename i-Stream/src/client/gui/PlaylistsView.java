/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client.gui;

import client.controller.Controller;
import def.Constraints;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import musicinfo.PlaylistID;
import musicinfo.Song;

/**
 *
 * @author Lars
 */
public class PlaylistsView extends JSplitPane {
    //private final MainFrame main;

    private JPanel jpPlaylists;
    private JPanel jpContent;
    private JTable jtPlaylist;
    private DefaultTableModel dtmPlaylist;
    private TableRowSorter trsPlaylist;
    private JTextField jtfSearch;
    private JButton jbSearch;
    private Comparator compar;
    private JTable jtPlaylists;
    private DefaultTableModel dtmPlaylists;

    public PlaylistsView(final Controller c) {
        setResizeWeight(0.1);
        setContinuousLayout(true);
        setDividerSize(5);

        setLeftComponent(initLeftPanel(c));
        setRightComponent(initRightPanel(c));
    //updateContent(null);

    }

    private JPanel initLeftPanel(final Controller c) {
        jpPlaylists = new JPanel();
        jpPlaylists.setBackground(Constraints.GUI_GREEN);
        jpPlaylists.setPreferredSize(new Dimension(200, 100));
        jpPlaylists.setBorder(BorderFactory.createTitledBorder(Constraints.BORDER, "Playlists"));
        jpPlaylists.setLayout(new BorderLayout());
        // Controllpanel
        JPanel jp = new JPanel();
        jp.setBackground(Constraints.GUI_GREEN);
        jp.setPreferredSize(new Dimension(0, 26));
        jpPlaylists.add(jp, BorderLayout.NORTH);
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
        jtPlaylists.setSelectionBackground(Constraints.GUI_GREEN_SELECT);
        jtPlaylists.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jtPlaylists.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                int row = jtPlaylists.getSelectedRow();
                if (row > -1) {//e.getClickCount() == 2 &&
                    c.selectPlaylist((PlaylistID) jtPlaylists.getValueAt(row, 0));
                }
            }
        });
        JScrollPane jsp = new JScrollPane(jtPlaylists);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jpPlaylists.add(jsp, BorderLayout.CENTER);
        return jpPlaylists;
    }

    private Component initRightPanel(final Controller c) {
        jpContent = new JPanel();

        compar = new Comparator() {

            public int compare(Object o1, Object o2) {
                if (o1.toString().length() == o2.toString().length()) {
                    return o1.toString().compareTo(o2.toString());
                } else {
                    return o1.toString().length() - o2.toString().length();
                }
            }
        };

        jpContent.setBackground(Constraints.GUI_YELLOW);
        jpContent.setBorder(BorderFactory.createTitledBorder(Constraints.BORDER, "Content"));
        jpContent.setLayout(new BorderLayout());
        jpContent.setPreferredSize(new Dimension(500, 100));

        // Controllpanel
        jpContent.add(initControllPanel(c), BorderLayout.NORTH);

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
        jtPlaylist.setSelectionBackground(Constraints.GUI_YELLOW_SELECT);
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
                        track = (Integer) jtPlaylist.getValueAt(track, 0) - 1;
                        c.play(track);
                    }
                }
            }
        });

        jtPlaylist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane jsp = new JScrollPane(jtPlaylist);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jpContent.add(jsp, BorderLayout.CENTER);
        return jpContent;
    }

    public JPanel initControllPanel(final Controller c) {
        JPanel jp = new JPanel();
        jp.setBackground(Constraints.GUI_YELLOW);
        jp.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        /**
         * Suche
         */
        ActionListener al = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                updatePlaylist(c.searchSongInPlaylist(jtfSearch.getText()));
            }
        };
        //jbSearch = new JButton("Filter", Constraints.getResourceIcon("xmag.png"));
        //jbSearch.setBackground(Constraints.GUI_YELLOW);
        jbSearch = new JButton();
        jbSearch.setIcon(Constraints.getResourceIcon("filter_normal.png"));
        jbSearch.setDisabledIcon(Constraints.getResourceIcon("filter_mute.png"));
        jbSearch.setPressedIcon(Constraints.getResourceIcon("filter_down.png"));
        jbSearch.setRolloverIcon(Constraints.getResourceIcon("filter_markedr.png"));
        jbSearch.setSelectedIcon(Constraints.getResourceIcon("filter_down"));
        jbSearch.setFocusPainted(false);
        jbSearch.setContentAreaFilled(false);
        jbSearch.setSize(new Dimension(56, 22));
        jbSearch.setMargin(new Insets(0, 0, 0, 0));

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

    public JTable getJtPlaylists() {
        return jtPlaylists;
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
        if (row >= dtmPlaylists.getRowCount()) {
            row = dtmPlaylists.getRowCount() - 1;
        }
        if (row > -1) {
            jtPlaylists.setRowSelectionInterval(row, row);
        }
    }

    public void updatePlaylist(Map<Integer, Song> songs) {
        dtmPlaylist.setRowCount(0);
        Song s;
        for (Integer k : songs.keySet()) {
            s = songs.get(k);
            int d = (int) (s.getDuration() / 1e6);
            dtmPlaylist.addRow(new Object[]{
                        k + 1,
                        s,
                        s.getArtist(),
                        s.getGenre(),
                        (d / 60) + ":" + ((d % 60) >= 10 ? (d % 60) : "0" + (d % 60))
                    });
        }
    }

    public void setSelect(int pos) {
        jtPlaylist.clearSelection();
        jtPlaylist.changeSelection(pos, 0, true, false);
    //jtPlaylist.changeSelection((Integer)jtPlaylist.getValueAt(trackid, 0) ,0, true, false);
    }

    public JTable getJtPlaylist() {
        return jtPlaylist;
    }
}
