package server.gui;

//import server.controller.Controller;
import def.Constraints;
import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import musicinfo.Playlist;
import musicinfo.PlaylistID;
import musicinfo.Song;
import server.controller.ILocal;
import server.datamodel.DMFacade;
import server.datamodel.DMListener;
import server.datamodel.IDataModel;

/**
 * Eigentlich steht jetzt alles drin, diesmal wirklich ...
 * suche ist implementiert gibt auf db seite aber glaub ich noch exception
 * ID3-Tags ändern ist noch kein listener drin, war da auch nicht sicher was für einer hinkommt
 * @author Lars
 */
public class MainFrame extends JFrame {

    // Controls
    private JButton jbMini;
    private JButton jbClose;
    private Container contentPane;
    private JLabel jlStatus;
    private ILocal ilocal;
    private IDataModel dmanager;
    /**
     * Dragging
     * Koordianten des ersten Klicks im bezug auf das JFrame
     */
    private int x;
    private int y;
    /**
     * Views
     */
    private JSplitPane jsp;
    protected final PlaylistView vPlaylists;
    protected final EditorView vEditor;
    protected final JFileChooser vBrowse;

    public MainFrame(final ILocal local, final IDataModel dbmanager) {
        super("iStream");
        setUndecorated(true);
        ilocal = local;
        dmanager = dbmanager;
        setIconImage(Constraints.getResourceIcon("frame-icon.png").getImage());

        // Setup Views
        vPlaylists = new PlaylistView(this, local);
        vEditor = new EditorView(this, local);
        //vPlaylistContent = new PlaylistContentView(this);
        vBrowse = new JFileChooser(new File("."));
        vBrowse.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        vBrowse.setFileFilter(Constraints.MP3_SWING_FILEFILTER);

        // Init Container
        init();
        setAsSideView(vPlaylists);
        setAsMainView(vEditor);
        dbmanager.addListener(new DMListener() {

            public void updatePlaylist(final PlaylistID playlist) {
                if (vEditor.getSelectedPlaylist().equals(playlist)) {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            vEditor.updateContent(dbmanager.getPlaylist(playlist));
                        }
                    });
                }
            }

            public void showError(String error) {
                JOptionPane.showMessageDialog(contentPane, error,
                        "iStream: error", JOptionPane.WARNING_MESSAGE);
            }

            public void updateSongs() {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        vEditor.updateSongs(dbmanager.getSongs());
                    }
                });
            }

            public void updatePlaylists() {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        vPlaylists.updatePlaylists(dbmanager.getPlaylistIDs());
                    }
                });
            }
        });

        vEditor.updateSongs(dbmanager.getSongs());
        vPlaylists.updatePlaylists(dbmanager.getPlaylistIDs());

        setSize(1000, 400);



        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(
                (int) (screen.getWidth() - getWidth()) / 2,
                (int) (screen.getHeight() - getHeight()) / 2);
        setVisible(
                true);
    }

    protected void setAsMainView(JComponent jp) {
        jsp.setRightComponent(jp);
    }

    protected void setAsSideView(JPanel jp) {
        jsp.setLeftComponent(jp);
    }

    private void init() {
        contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.setBackground(Color.GRAY);
        // Logo, title, close
        contentPane.add(initControllPanel(), BorderLayout.NORTH);
        // StatusPanel
        contentPane.add(initStatusPanel(), BorderLayout.SOUTH);
        // Split
        jsp = new JSplitPane();
        jsp.setBackground(Color.GRAY);
        jsp.setDividerLocation(200);
        jsp.setContinuousLayout(true);
        jsp.setDividerSize(5);
        jsp.setOneTouchExpandable(true);
        contentPane.add(jsp, BorderLayout.CENTER);
    }

    /**
     * Erstellt die selbstgestrickte Fensterleiste mit dem Fentsertitel und dem
     * Button zum schließen des Fensters.
     * @param c     Der Controller auf dem XXX auch über action command und al?????
     * @return      das Controllpanel
     */
    private JPanel initControllPanel() {
        JPanel cp = new JPanel();
        cp.setLayout(new GridBagLayout());
        cp.setPreferredSize(new Dimension(10, 22));
        cp.setBackground(Color.GRAY);
        cp.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                x = e.getXOnScreen() - getX();
                y = e.getYOnScreen() - getY();
            }
        });
        cp.addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {
                setLocation(e.getXOnScreen() - x, e.getYOnScreen() - y);
            }
        });
        GridBagConstraints gbc = new GridBagConstraints();
        // Logo, title
        gbc.gridx = 0;
        gbc.gridy = 0;
        cp.add(new JLabel(Constraints.getResourceIcon("frame-icon.png")), gbc);
        // Logo, title
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        cp.add(new JLabel("iStream - Music Streaming System"), gbc);

        // Minimize
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.insets = new Insets(0,0,0,0);
        jbMini = new JButton();
        jbMini.setIcon(Constraints.getResourceIcon("mini_normal.png"));
        jbMini.setRolloverIcon(Constraints.getResourceIcon("mini_marked.png"));
        jbMini.setPressedIcon(Constraints.getResourceIcon("mini_down.png"));
        jbMini.setMargin(new Insets(0,0,0,0));
        jbMini.setContentAreaFilled(false);
        jbMini.setFocusPainted(false);
        jbMini.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setExtendedState(JFrame.ICONIFIED);
            }
        });
        if (!Toolkit.getDefaultToolkit().isFrameStateSupported(JFrame.ICONIFIED)) {
            jbMini.setEnabled(false);
            jbMini.setForeground(Color.GRAY);
        }
        cp.add(jbMini, gbc);

        // Close
        gbc.gridx = 3;
        jbClose = new JButton();
        jbClose.setIcon(Constraints.getResourceIcon("close_normal.png"));
        jbClose.setRolloverIcon(Constraints.getResourceIcon("close_marked.png"));
        jbClose.setPressedIcon(Constraints.getResourceIcon("close_down.png"));
        jbClose.setMargin(new Insets(0,0,0,0));
        jbClose.setContentAreaFilled(false);
        jbClose.setFocusPainted(false);
        jbClose.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                ilocal.terminate();
                // TODO move to LocalFasade
                setVisible(false);
                dispose();
            }
        });
        cp.add(jbClose, gbc);
        return cp;
    }

    private JPanel initStatusPanel() {
        JPanel cp = new JPanel();
        cp.setLayout(new GridLayout());
        cp.setPreferredSize(new Dimension(10, 18));
        cp.setBackground(Color.GRAY);
        try {
            cp.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
        }catch (HeadlessException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        cp.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                x = getWidth() - e.getXOnScreen();
                y = getHeight() - e.getYOnScreen();
            }
        });
        cp.addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {
                int w = e.getXOnScreen() + x;
                int h = e.getYOnScreen() + y;
                if (w < 500) {
                    w = 500;
                }
                if (h < 300) {
                    h = 300;
                }
                setSize(w, h);
            }
        });
        try {
            // TODO status method
            InetAddress ia = InetAddress.getLocalHost();
            jlStatus = new JLabel(" Server running at " + ia.getHostAddress() + " (" + ia.getCanonicalHostName() + ")");
        } catch (UnknownHostException ex) {
            jlStatus = new JLabel(" status");
        }
        cp.add(jlStatus);
        return cp;
    }

//    public void addListener(DMListener listener) {
//        this.listener = listener;
//    }
    public void showError(String error) {
        JOptionPane.showMessageDialog(contentPane, error,
                "iStream: error", JOptionPane.WARNING_MESSAGE);
    }

    void startEditPlaylist(PlaylistID playlistID) {
        vEditor.updateContent(dmanager.getPlaylist(playlistID));
    }

    // XXX ohne aufruf und file zurückgeben?
    public void chooseFile() {
        if (vBrowse.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
//            System.out.println(vBrowse.getSelectedFile().getAbsolutePath());
            addFile(vBrowse.getSelectedFile());
        }
    }

    // TODO in  controller??
    public void addFile(File file) {//String path){
        //File file = new File(path);
        dmanager.createSongFromFile(file);
    }
}
