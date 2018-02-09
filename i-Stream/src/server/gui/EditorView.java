package server.gui;

import def.Constraints;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import musicinfo.Playlist;
import musicinfo.PlaylistID;
import musicinfo.Song;
import server.controller.ILocal;

/**
 *
 * @author Uzul
 */
public class EditorView extends JSplitPane {

    private final MainFrame main;
    private final ILocal local;
    private PlaylistID selectedPlaylist;
//    private Map<Integer, Song> Data;

    public PlaylistID getSelectedPlaylist() {
        return selectedPlaylist;
    }
    private Comparator compar;
    /**
     * Mid Panel
     */
    private JButton jbAdd;
    private JButton jbRemove;
    /**
     * Left Panel
     */
    private JPanel jpContent;
    private JTable jtContent;
    private DefaultTableModel dtmContent;
    private JTextField jtfSearchContent;
    private JButton jbSearchContent;
    private JButton jbRemovePlaylist;
    /**
     * Right Panel
     */
    private JPanel jpSongs;
    private JTable jtSongs;
    private TableRowSorter trsSongs;
    private DefaultTableModel dtmSongs;
    private JTextField jtfSearchSongs;
    private JButton jbAddFiles;
    private JButton jbSearchSongs;
    private JButton jbRemoveFile;

    public EditorView(final MainFrame mf, final ILocal il) {
        //Border b = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
        //setBorder(BorderFactory.createTitledBorder(b,"DataBase"));
        //setLayout(new GridBagLayout());
        setBackground(Color.GRAY);
        //GridBagConstraints gbc = new GridBagConstraints();
        main = mf;
        local = il;

        compar = new Comparator() {

            public int compare(Object o1, Object o2) {
                if (o1.toString().length() == o2.toString().length()) {
                    return o1.toString().compareTo(o2.toString());
                } else {
                    return o1.toString().length() - o2.toString().length();
                }
            }
        };
        setResizeWeight(0.5);
        setContinuousLayout(true);
        setDividerSize(5);

        JPanel jp = new JPanel();
        jp.setLayout(new BorderLayout());
        jp.add(initLeftPanel(), BorderLayout.CENTER);
        jp.add(initMidPanel(), BorderLayout.EAST);
        setLeftComponent(jp);
        setRightComponent(initRightPanel());

        // Panels
        //gbc.gridx = 1;
        //gbc.gridy = 0;
        //add(initMidPanel(), gbc);

        // Panels
//        gbc.gridx = 0;
//        gbc.fill = GridBagConstraints.BOTH;
//        gbc.weightx = 1;
//        gbc.weighty = 1;
//        add(initLeftPanel(), gbc);
        // Panels
//        gbc.gridx = 2;
//        add(initRightPanel(), gbc);
        updateContent(null);
    }

    private JPanel initMidPanel() {
        JPanel jp = new JPanel();
        jp.setLayout(new GridLayout(2, 1));
        jp.setBackground(Constraints.GUI_YELLOW);
        jbAdd = new JButton(Constraints.getResourceIcon("left.png"));
        jbAdd.setRolloverIcon(Constraints.getResourceIcon("left_marked.png"));
        jbAdd.setContentAreaFilled(false);
        jbAdd.setPreferredSize(new Dimension(20, 0));
        jbAdd.setFocusPainted(false);
        jbAdd.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                for (int row : jtSongs.getSelectedRows()) {
                    local.addSongToPlaylist(selectedPlaylist, (Song) jtSongs.getValueAt(row, 1));
                }
//                int row = jtSongs.getSelectedRow();
//                if (row == -1) {
//                    local.showError("No song selected!");
//                } else {
//                    local.addSongToPlaylist(selectedPlaylist, (Song) jtSongs.getValueAt(row, 1));
//                }
            }
        });
        jp.add(jbAdd);
        jbRemove = new JButton(Constraints.getResourceIcon("right.png"));
        jbRemove.setRolloverIcon(Constraints.getResourceIcon("right_marked.png"));
        jbRemove.setContentAreaFilled(false);
        jbRemove.setPreferredSize(new Dimension(20, 0));
        jbRemove.setFocusPainted(false);
        jbRemove.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                // TODO die listener methoden nochmal mit ner collection implementieren????
                int row = jtContent.getSelectedRow();
                int[] rows = jtContent.getSelectedRows();
                for (int i = 0; i < rows.length; i++) {
                    local.removeSongFromPlaylist(selectedPlaylist, row);
                }
            }
        });
        jp.add(jbRemove);
        return jp;
    }

    private JPanel initLeftPanel() {
        jpContent = new JPanel();
        jpContent.setBorder(BorderFactory.createTitledBorder(Constraints.BORDER, "No Playlist loaded"));
        jpContent.setLayout(new BorderLayout());
        jpContent.setBackground(Constraints.GUI_YELLOW);
        JPanel jp = new JPanel();
        jp.setOpaque(false);
        jp.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        {
            jp.setOpaque(false);
            //jbRemovePlaylist = new JButton("Remove", Constraints.getResourceIcon("edit_remove.png"));
            jbRemovePlaylist = new JButton();
            jbRemovePlaylist.setIcon(Constraints.getResourceIcon("remove_normal.png"));
            jbRemovePlaylist.setDisabledIcon(Constraints.getResourceIcon("remove_mute.png"));
            jbRemovePlaylist.setPressedIcon(Constraints.getResourceIcon("remove_down.png"));
            jbRemovePlaylist.setRolloverIcon(Constraints.getResourceIcon("remove_marked.png"));
            jbRemovePlaylist.setSelectedIcon(Constraints.getResourceIcon("remove_down"));
            jbRemovePlaylist.setFocusPainted(false);
            jbRemovePlaylist.setContentAreaFilled(false);
            jbRemovePlaylist.setSize(new Dimension(56, 22));
            jbRemovePlaylist.setMargin(new Insets(0, 0, 0, 0));
            //jbRemovePlaylist.setBackground(Constraints.GUI_YELLOW);
            jbRemovePlaylist.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    //mf.setAsMainView(mf.vEditor);
//                    jtContent = new JTable(dtmContent);
//
//                    jtContent.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//                    JScrollPane jsp = new JScrollPane(jtContent);
//                    jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//                    jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                    local.removePlaylist(selectedPlaylist);
//                    repaint();
                }
            });
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 0;
            jp.add(jbRemovePlaylist, gbc);
            /**
             * Suche
             */
            ActionListener al = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    String term = jtfSearchContent.getText();
                    Map<Integer, Song> searchResult = local.searchSongInPlaylist(selectedPlaylist, term);
                    updatePlaylist(dtmContent, searchResult);
                }
            };
            //jbSearchContent = new JButton("Filter", Constraints.getResourceIcon("xmag.png"));
            //jbSearchContent.setBackground(Constraints.GUI_YELLOW);
            jbSearchContent = new JButton();
            jbSearchContent.setIcon(Constraints.getResourceIcon("filter_normal.png"));
            jbSearchContent.setDisabledIcon(Constraints.getResourceIcon("filter_mute.png"));
            jbSearchContent.setPressedIcon(Constraints.getResourceIcon("filter_down.png"));
            jbSearchContent.setRolloverIcon(Constraints.getResourceIcon("filter_markedy.png"));
            jbSearchContent.setSelectedIcon(Constraints.getResourceIcon("filter_down"));
            jbSearchContent.setFocusPainted(false);
            jbSearchContent.setContentAreaFilled(false);
            jbSearchContent.setSize(new Dimension(56, 22));
            jbSearchContent.setMargin(new Insets(0, 0, 0, 0));
            jbSearchContent.addActionListener(al);
            gbc.gridx = 2;
            jp.add(jbSearchContent, gbc);

            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;
            jtfSearchContent = new JTextField();
            jtfSearchContent.addActionListener(al);
            jp.add(jtfSearchContent, gbc);
        }
        jpContent.add(jp, BorderLayout.NORTH);

        dtmContent = new DefaultTableModel(new String[]{"Track", "Title", "Artist", "Genre", "Duration"}, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jtContent = new JTable(dtmContent);
        jtContent.getColumnModel().getColumn(0).setPreferredWidth(10);
        jtContent.getColumnModel().getColumn(3).setPreferredWidth(20);
        jtContent.getColumnModel().getColumn(4).setPreferredWidth(20);
        jtContent.setSelectionBackground(Constraints.GUI_YELLOW_SELECT);
        jtContent.getTableHeader().setPreferredSize(new Dimension(0, 20));
        jtContent.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        JScrollPane jsp = new JScrollPane(jtContent);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jpContent.add(jsp, BorderLayout.CENTER);
        return jpContent;
    }

    private JPanel initRightPanel() {
        jpSongs = new JPanel();
        jpSongs.setLayout(new BorderLayout());
        jpSongs.setBorder(BorderFactory.createTitledBorder(Constraints.BORDER, "DataBase"));
        jpSongs.setBackground(Constraints.GUI_RED);
        JPanel jp = new JPanel();
        jp.setOpaque(false);
        jp.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        {
            /**
             * Add
             */
            gbc.gridx = 0;
            gbc.gridy = 0;
            //JButton jbAddFiles = new JButton("Add...", Constraints.getResourceIcon("edit_add.png"));
            jbAddFiles = new JButton();
            jbAddFiles.setIcon(Constraints.getResourceIcon("add_normal.png"));
            jbAddFiles.setDisabledIcon(Constraints.getResourceIcon("add_mute.png"));
            jbAddFiles.setPressedIcon(Constraints.getResourceIcon("add_down.png"));
            jbAddFiles.setRolloverIcon(Constraints.getResourceIcon("add_marked.png"));
            jbAddFiles.setSelectedIcon(Constraints.getResourceIcon("add_down"));
            jbAddFiles.setFocusPainted(false);
            jbAddFiles.setContentAreaFilled(false);
            jbAddFiles.setSize(new Dimension(56, 22));
            jbAddFiles.setMargin(new Insets(0, 0, 0, 0));
            jbAddFiles.setToolTipText("Add Songs to my Music Library");
            //jbAddFiles.setBackground(Constraints.GUI_RED);
            jbAddFiles.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    main.chooseFile();
                }
            });
            jp.add(jbAddFiles, gbc);
            /**
             * Suche
             */
            jtfSearchSongs = new JTextField();
            ActionListener al = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    String term = jtfSearchSongs.getText();
                    // TODO direkt über datamodell?????????????
                    Collection<Song> searchResult = local.searchSong(term);
                    updateSongs(dtmSongs, searchResult);
                }
            };
            jtfSearchSongs.addActionListener(al);
            //jbSearchSongs = new JButton("Filter", Constraints.getResourceIcon("xmag.png"));
            //jbSearchSongs.setBackground(Constraints.GUI_RED);
            jbSearchSongs = new JButton();
            jbSearchSongs.setIcon(Constraints.getResourceIcon("filter_normal.png"));
            jbSearchSongs.setDisabledIcon(Constraints.getResourceIcon("filter_mute.png"));
            jbSearchSongs.setPressedIcon(Constraints.getResourceIcon("filter_down.png"));
            jbSearchSongs.setRolloverIcon(Constraints.getResourceIcon("filter_markedr.png"));
            jbSearchSongs.setSelectedIcon(Constraints.getResourceIcon("filter_down"));
            jbSearchSongs.setFocusPainted(false);
            jbSearchSongs.setContentAreaFilled(false);
            jbSearchSongs.setSize(new Dimension(56, 22));
            jbSearchSongs.setMargin(new Insets(0, 0, 0, 0));
            jbSearchSongs.addActionListener(al);
            gbc.gridx = 2;
            jp.add(jbSearchSongs, gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;
            jp.add(jtfSearchSongs, gbc);
            /**
             * Remove song
             */
            //JButton jbRemoveFile = new JButton("Delete", Constraints.getResourceIcon("edit_remove.png"));
            //jbRemoveFile.setBackground(Constraints.GUI_RED);
            jbRemoveFile = new JButton();
            jbRemoveFile.setIcon(Constraints.getResourceIcon("delete_normal.png"));
            jbRemoveFile.setDisabledIcon(Constraints.getResourceIcon("delete_mute.png"));
            jbRemoveFile.setPressedIcon(Constraints.getResourceIcon("delete_down.png"));
            jbRemoveFile.setRolloverIcon(Constraints.getResourceIcon("delete_marked.png"));
            jbRemoveFile.setSelectedIcon(Constraints.getResourceIcon("delete_down"));
            jbRemoveFile.setFocusPainted(false);
            jbRemoveFile.setContentAreaFilled(false);
            jbRemoveFile.setSize(new Dimension(56, 22));
            jbRemoveFile.setMargin(new Insets(0, 0, 0, 0));
            jbRemoveFile.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    for (int row : jtSongs.getSelectedRows()) {
                        local.removeSong((Song) jtSongs.getValueAt(row, 1));
                    }
                }
            });
            gbc.gridx = 3;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            jp.add(jbRemoveFile, gbc);
        }
        jpSongs.add(jp, BorderLayout.NORTH);

        dtmSongs = new DefaultTableModel(new String[]{"No.", "Title", "Artist", "Genre", "Duration"}, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 1 && column <= 3;
            }
        };
        jtSongs = new JTable(dtmSongs);
        trsSongs = new TableRowSorter(dtmSongs);
        trsSongs.setComparator(0, compar);
        trsSongs.setComparator(4, compar);
        jtSongs.setRowSorter(trsSongs);

        final JTextField f = new JTextField();
        DefaultCellEditor d = new DefaultCellEditor(f) {

            private Song s;

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                if (value instanceof Song) {
                    s = (Song) value;
                } else {
                    s = null;
                }
                return super.getTableCellEditorComponent(table, value, isSelected, row, column);
            }

            @Override
            public Object getCellEditorValue() {
                String newValue = (String) super.getCellEditorValue();
                if (s != null) {
                    s.setTitle(newValue);
                    return s;
                }
                return super.getCellEditorValue();
            }
        };
        d.addCellEditorListener(new CellEditorListener() {

            public void editingStopped(ChangeEvent e) {
                local.updateTag((Song) jtSongs.getValueAt(jtSongs.getSelectedRow(), 1));
            }

            public void editingCanceled(ChangeEvent e) {
            }
        });
        jtSongs.getColumnModel().getColumn(1).setCellEditor(d);
        jtSongs.getColumnModel().getColumn(2).setCellEditor(d);
        jtSongs.getColumnModel().getColumn(3).setCellEditor(d);

        jtSongs.getColumnModel().getColumn(0).setPreferredWidth(10);
        jtSongs.getColumnModel().getColumn(3).setPreferredWidth(20);
        jtSongs.getColumnModel().getColumn(4).setPreferredWidth(20);
        jtSongs.setSelectionBackground(Constraints.GUI_RED_SELECT);
        jtSongs.getTableHeader().setPreferredSize(new Dimension(0, 20));
        jtSongs.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        JScrollPane jsp = new JScrollPane(jtSongs);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jpSongs.add(jsp, BorderLayout.CENTER);
        return jpSongs;
    }

    public void updateSongs(Collection<Song> songs) {
        updateSongs(dtmSongs, songs);
    }

    private void updateSongs(DefaultTableModel tm, Collection<Song> songs) {
        tm.setRowCount(0);
        int i = -1;
        for (Song s : songs) {
            i++;
            int d = (int) (s.getDuration() / 1e6);
            tm.addRow(new Object[]{
                        String.valueOf(i + 1),
                        s,
                        s.getArtist(),//s.getFile(),//
                        s.getGenre(),
                        (d / 60) + ":" + ((d % 60) >= 10 ? (d % 60) : "0" + (d % 60))
                    });
        }
    }

    private void updatePlaylist(DefaultTableModel tm, Map<Integer, Song> songs) {
        tm.setRowCount(0);
        Song s;
        for (Integer k : songs.keySet()) {
            s = songs.get(k);
            int d = (int) (s.getDuration() / 1e6);
            tm.addRow(new Object[]{
                        k,
                        s,
                        s.getArtist(),
                        s.getGenre(),
                        (d / 60) + ":" + ((d % 60) >= 10 ? (d % 60) : "0" + (d % 60))
                    });
        }
    }

    public void updateContent(Playlist playlist) {
        if (playlist == null) {
            selectedPlaylist = null;
            // Update GUI
            ((TitledBorder) jpContent.getBorder()).setTitle("No Playlist loaded");
            jpContent.repaint();
            jbSearchContent.setEnabled(false);
            jtfSearchContent.setEnabled(false);
            jbRemovePlaylist.setEnabled(false);
            jbAdd.setEnabled(false);
            jbRemove.setEnabled(false);
            // Clear table
            dtmContent.setRowCount(0);
            return;
        } else {
            selectedPlaylist = playlist.getID();
            // Update GUI
            ((TitledBorder) jpContent.getBorder()).setTitle("Editing: " + selectedPlaylist.getName());
            jpContent.repaint();
            jbSearchContent.setEnabled(true);
            jtfSearchContent.setEnabled(true);
            jbRemovePlaylist.setEnabled(true);
            jbAdd.setEnabled(true);
            jbRemove.setEnabled(true);
            // Update Table
            updatePlaylist(dtmContent, playlist.getSongTabel());
        }
    }

    public JTable getJtSongs() {
        return jtSongs;
    }

    public JTable getJtPlaylist() {
        return jtContent;
    }
}
