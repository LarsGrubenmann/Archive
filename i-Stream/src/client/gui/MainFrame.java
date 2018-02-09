package client.gui;

import client.controller.CCListener;
import client.controller.Controller;
import com.sun.java.swing.plaf.motif.MotifProgressBarUI;
import def.Constraints;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
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
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ProgressBarUI;
import musicinfo.Playlist;
import musicinfo.PlaylistID;
import musicinfo.Song;
import server.clientadministration.ClientInfo;

/**
 *
 * @author Lars
 */
public class MainFrame extends JFrame {

    // Controlls
    private JButton jbMini;
    private JButton jbClose;
    private Container contentPane;
    private JLabel jlStatus;
    /**
     * Playpanel
     */
//    private JLabel jlTitle;
    private JButton jbPlay;
    private JButton jbStop;
    private JButton jbPrev;
    private JButton jbNext;
    private boolean playing;
    private int position;
    private JButton jbPause;
    private JProgressBar jpbProgress;
    private JProgressBar jpbVolume;
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
    protected final LoginView vLogin;
    protected final ClientView vClients;
    //protected final PlaylistView vPlaylists;
    //protected final PlaylistContentView vPlaylistContent;
    protected final PlaylistsView vPlayliist;
    private Playlist actualPl;

    public MainFrame(final Controller c) {
        super("iStream");
        setUndecorated(true);
        setIconImage(Constraints.getResourceIcon("frame-icon.png").getImage());
        // Setuo Views
        vLogin = new LoginView(c);
        vClients = new ClientView(c);
        //vPlaylists = new PlaylistView(c);
        vPlayliist = new PlaylistsView(c);
        //vPlaylistContent = new PlaylistContentView(this, c);
        // Init Container
        init(c);
        jsp.setLeftComponent(vClients);
        jsp.setRightComponent(vPlayliist);
        //jsp.setForeground(Color.WHITE);
        // setAsSideView(vClients);
        // setAsMainView(vPlaylists);// TODO remove

        // Controller - Gui Connection
        c.setListener(new CCListener() {

            public void addClient(final ClientInfo clientinfo) {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        vClients.addClientInfo(clientinfo);
                    }
                });
            }

            public void remotePause() {
//                setPauseState();
                playing = !playing;
                jbPlay.setVisible(!playing);
                jbPause.setVisible(playing);
                jpbProgress.setIndeterminate(!playing);
            }

            public void remotePlay(final Song song, final int pos) {
                //jbPlay.setIcon(Constraints.getResourceIcon("player_pause.png"));
                //jbPlay.setIcon(Constraints.getResourceIcon("pause_normal.png"));
                //jbPlay.setPressedIcon(Constraints.getResourceIcon("pause_down.png"));
                //jbPlay.setRolloverIcon(Constraints.getResourceIcon("pause_marked.png"));
                //jbPlay.setSelectedIcon(Constraints.getResourceIcon("pause_down.png"));
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        // TODO vereinen
                        jpbProgress.setString(song.getTitle() + " - " + song.getArtist());
                        jpbProgress.setIndeterminate(false);
                        // XXX geht momentan nur wenn der benutzer die finger von der liste lässt
                        vPlayliist.setSelect(pos);
                        //vPlaylistContent.setSelect();
//                        setTitle("iStream: " + jlTitle.getText());
                        playing = true;
                        position = pos;
                        jbPlay.setVisible(false);
                        jbPause.setVisible(true);
                    }
                });
            }

            public void remoteStop() {
//                setStopState();
                jbPlay.setVisible(true);
                jbPause.setVisible(false);
                playing = false;
                jpbProgress.setIndeterminate(false);
                jpbProgress.setValue(0);
            }

            public void removeClient(final ClientInfo clientinfo) {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        vClients.removeClientID(clientinfo);
                    }
                });
            }

            public void showError(String error) {
                JOptionPane.showMessageDialog(contentPane, error,
                        "iStream: error", JOptionPane.WARNING_MESSAGE);
            }

            public void updatePlaylist(final Playlist playlist) {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        actualPl = playlist;
                        vPlayliist.updatePlaylist(playlist.getSongTabel());
                        vPlayliist.repaint();
                        //vPlaylistContent.updatePlaylist(playlist.getSongTabel());
                        //setAsMainView(vPlaylistContent);
                        setConnectedState();// TODO nur erstes mal
                    }
                });
            }

            public void updatePlaylists(final Collection<PlaylistID> playlistIDs) {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        //vPlaylists.updatePlaylists(playlistIDs);
                        //setAsMainView(vPlaylists);
                        vPlayliist.updatePlaylists(playlistIDs);
                        jsp.setRightComponent(vPlayliist);
                        
                        jsp.repaint();
                    }
                });
            }

            public void updateSynchronisation(final Collection<ClientInfo> clients) {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        vClients.updateSynchronization(clients);
                    }
                });
            }

            public void requestLogin(String clientName, String serverName) {
                setDisconnectedState();
                vLogin.getJtfName().setText(clientName);
                vLogin.getJtfServer().setText(serverName);
                if (clientName.equals("") && serverName.equals("")) {
                    vLogin.getJbOK().setEnabled(false);
                }
                setAsMainView(vLogin);
            }

            public void updateProgress(int progress) {
                jpbProgress.setValue(progress);
            }
        });

        setDisconnectedState();
        setSize(1000, 400);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((int) (screen.getWidth() - getWidth()) / 2,
                (int) (screen.getHeight() - getHeight()) / 2);
        setVisible(true);
    }

    protected void setAsMainView(JPanel jp) {
        jsp.setRightComponent(jp);
    }

    protected void setAsSideView(JPanel jp) {
        jsp.setLeftComponent(jp);
    }

    private void setDisconnectedState() {
//        jlTitle.setText("-");
        jbPause.setEnabled(false);
        jbPlay.setEnabled(false);
        jbStop.setEnabled(false);
        jbPrev.setEnabled(false);
        jbNext.setEnabled(false);
    }

    private void setConnectedState() {
//        jlTitle.setText("-");
        jbPause.setEnabled(true);
        jbPlay.setEnabled(true);
        jbStop.setEnabled(true);
        jbPrev.setEnabled(true);
        jbNext.setEnabled(true);
    }

    private void init(final Controller c) {
        contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        // Logo, title, close
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        contentPane.add(initControllPanel(c), gbc);
        // PlayPanel
        gbc.gridy = 1;
        contentPane.add(initPlayPanel(c), gbc);
        // StatusPanel
        gbc.gridy = 3;
        contentPane.add(initStatusPanel(c), gbc);
        // Split
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        jsp = new JSplitPane();
        jsp.setDividerLocation(200);
        jsp.setContinuousLayout(true);
        jsp.setDividerSize(5);
        jsp.setOneTouchExpandable(true);
        contentPane.add(jsp, gbc);
    }

    /**
     * Erstellt die selbstgestrickte Fensterleiste mit dem Fentsertitel und dem
     * Button zum schließen des Fensters.
     * @param c     Der Controller auf dem XXX auch über action command und al?????
     * @return      das Controllpanel
     */
    private JPanel initControllPanel(final Controller c) {
        JPanel cp = new JPanel();
        cp.setBackground(Color.GRAY);
        cp.setPreferredSize(new Dimension(10, 22));
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
        cp.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        // Logo
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel jlFrameIcon = new JLabel(Constraints.getResourceIcon("frame-icon.png"));
        cp.add(jlFrameIcon, gbc);
        // Title
        JLabel jlFrameTitle = new JLabel("iStream - Music Streaming System");
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        cp.add(jlFrameTitle, gbc);
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
                c.terminate();
            }
        });
        cp.add(jbClose, gbc);
        return cp;
    }

    private JPanel initStatusPanel(final Controller c) {
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
        jlStatus = new JLabel("status");
        cp.add(jlStatus);
        return cp;
    }

    /**
     * Erstellt das Panel mit der Titel anzeige und den elementaren
     * Kontrollelementen eines Players.
     * @param c TODO nötig?
     * @return  das Playerpanel
     * @since   1.0
     */
    private JPanel initPlayPanel(final Controller c) {
        JPanel pp = new JPanel();
        pp.setPreferredSize(new Dimension(0, 30));
        pp.setBackground(Color.GRAY);
        pp.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        // Title
//        jlTitle = new JLabel("title");
//        jlTitle.setFont(new Font(Font.DIALOG, Font.BOLD, 16));
        gbc.gridx = 0;
//        gbc.gridy = 0;
//        gbc.gridwidth = 6;
//        gbc.fill = GridBagConstraints.HORIZONTAL;
//        gbc.weightx = 1;
//        gbc.insets = new Insets(5, 15, 5, 15);
//        pp.add(jlTitle, gbc);

        /**
         * Previous-Button
         */
      
        jbPrev = new JButton();
        jbPrev.setIcon(Constraints.getResourceIcon("prev_normal.png"));
        jbPrev.setDisabledIcon(Constraints.getResourceIcon("prev_mute.png"));
        jbPrev.setPressedIcon(Constraints.getResourceIcon("prev_down.png"));
        jbPrev.setRolloverIcon(Constraints.getResourceIcon("prev_marked.png"));
        jbPrev.setSelectedIcon(Constraints.getResourceIcon("prev_down"));
        jbPrev.setSize(new Dimension(30, 28));
        jbPrev.setMargin(new Insets(0, 0, 0, 0));
        jbPrev.setFocusPainted(false);
        jbPrev.setContentAreaFilled(false);

        jbPrev.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
//                if (playing || jpbProgress.isIndeterminate()){
                    c.stop();
                    if (position == -1) position = 0;
                    if (position == 0) position = actualPl.size();
                    c.play(position-1);
//                }else{
//                }
            }
        });
//        gbc.gridy = 1;
        gbc.gridwidth = 1;
        //gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.NONE;
        //gbc.weightx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        pp.add(jbPrev, gbc);


        /**
         * Play-Button
         */
        //jbPlay = new JButton(Constraints.getResourceIcon("player_play.png"));
        //jbPlay.setSelectedIcon(Constraints.getResourceIcon("player_paused.png"));
        //jbPlay = new JButton(Constraints.getResourceIcon("play_normal.png"));
        jbPlay = new JButton();
        jbPlay.setIcon(Constraints.getResourceIcon("play_normal.png"));
        jbPlay.setDisabledIcon(Constraints.getResourceIcon("play_mute.png"));
        jbPlay.setPressedIcon(Constraints.getResourceIcon("play_down.png"));
        jbPlay.setRolloverIcon(Constraints.getResourceIcon("play_marked.png"));
        jbPlay.setSelectedIcon(Constraints.getResourceIcon("play_down"));
        jbPlay.setSize(new Dimension(46, 28));
        jbPlay.setMargin(new Insets(0, 0, 0, 0));
        jbPlay.setFocusPainted(false);
        //jbPlay.validate();
        //jbPlay.repaint();
        jbPlay.setContentAreaFilled(false);

        ActionListener al = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (playing || jpbProgress.isIndeterminate()) {
                    c.pause();          
                }
                else {
                    //int track = vPlaylistContent.getJtPlaylist().getSelectedRow();
                    int num = vPlayliist.getJtPlaylist().getSelectedRow();
                    if (num > -1) {
                        //track = (Integer) vPlaylistContent.getJtPlaylist().getValueAt(track, 0);
                        //int track = (Integer) vPlayliist.getJtPlaylist().getValueAt(num, 0);
                        c.play(num);
                    } else {
                        c.play(0);
                    }
                }

            }
        };
        jbPlay.addActionListener(al);
//        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridx++;
        //gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.NONE;
        //gbc.weightx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        pp.add(jbPlay, gbc);
        /**
         * Pause-Button
         */
        jbPause = new JButton();
        //jbPause.setIcon(Constraints.getResourceIcon("player_pause.png"));
        jbPause.setIcon(Constraints.getResourceIcon("pause_normal.png"));
        jbPause.setPressedIcon(Constraints.getResourceIcon("pause_down.png"));
        jbPause.setDisabledIcon(Constraints.getResourceIcon("pause_mute.png"));
        jbPause.setRolloverIcon(Constraints.getResourceIcon("pause_marked.png"));
        jbPause.setSelectedIcon(Constraints.getResourceIcon("pause_down.png"));
        jbPause.setContentAreaFilled(false);
        jbPause.setFocusPainted(false);
        jbPause.setSize(new Dimension(46, 28));
        jbPause.setMargin(new Insets(0, 0, 0, 0));
        jbPause.setVisible(false);
        jbPause.addActionListener(al);
        //        jbPause.addActionListener(new ActionListener() {
//
//            public void actionPerformed(ActionEvent e) {
//                c.pause();
//            }
//        });
        gbc.gridx++;
        pp.add(jbPause, gbc);
        /**
         * Stop-Button
         */
        //jbStop = new JButton(Constraints.getResourceIcon("player_stop.png"));
        jbStop = new JButton();
        jbStop.setIcon(Constraints.getResourceIcon("stop_normal.png"));
        jbStop.setDisabledIcon(Constraints.getResourceIcon("stop_mute.png"));
        jbStop.setPressedIcon(Constraints.getResourceIcon("stop_down.png"));
        jbStop.setRolloverIcon(Constraints.getResourceIcon("stop_marked.png"));
        jbStop.setSelectedIcon(Constraints.getResourceIcon("stop_down.png"));
        jbStop.setSize(new Dimension(46, 28));
        jbStop.setMargin(new Insets(0, 0, 0, 0));
        //jbStop.validate();
        jbStop.setContentAreaFilled(false);
        jbStop.setFocusPainted(false);
        jbStop.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                c.stop();
                jpbProgress.setString("");
                //jpbProgress.setIndeterminate(false);
                jbPlay.setVisible(true);
                jbPause.setVisible(false);
                //jbPause.setSelected(false);
            }
        });
        gbc.gridx++;
        pp.add(jbStop, gbc);


        /**
         * Previous-Button
         */

        jbNext = new JButton();
        jbNext.setIcon(Constraints.getResourceIcon("next_normal.png"));
        jbNext.setDisabledIcon(Constraints.getResourceIcon("next_mute.png"));
        jbNext.setPressedIcon(Constraints.getResourceIcon("next_down.png"));
        jbNext.setRolloverIcon(Constraints.getResourceIcon("next_marked.png"));
        jbNext.setSelectedIcon(Constraints.getResourceIcon("next_down"));
        jbNext.setSize(new Dimension(30, 28));
        jbNext.setMargin(new Insets(0, 0, 0, 0));
        jbNext.setFocusPainted(false);
        jbNext.setContentAreaFilled(false);

        jbNext.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
//                if (playing || jpbProgress.isIndeterminate()){
                    c.stop();
                    if (position == -1) position = 0;
                    c.play((position+1)%actualPl.size());
//                }else{
//                }
            }
        });
//        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridx++;
        //gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.NONE;
        //gbc.weightx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        pp.add(jbNext, gbc);


        /**
         * Progress
         */
        jpbProgress = new JProgressBar();
        jpbProgress.setUI(new MotifProgressBarUI());
        jpbProgress.setForeground(Constraints.GUI_GREEN);
        jpbProgress.setBackground(Color.DARK_GRAY);
        jpbProgress.setBorderPainted(false);
        jpbProgress.setFont(new Font(Font.DIALOG, Font.BOLD, 15));
        jpbProgress.setString("");
        jpbProgress.setStringPainted(true);
        //jpbProgress.set
        jpbProgress.setPreferredSize(new Dimension(300, 26));
        gbc.gridx++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(0, 10, 0, 10);
        pp.add(jpbProgress, gbc);
        /**
         * Volume
         */
        jpbVolume = new JProgressBar();// TODO init value from config?
        jpbVolume.setValue(70);// TODO from decoder <>
        jpbVolume.setPreferredSize(new Dimension(80, 26));
        jpbVolume.setSize(new Dimension(80,26));
        jpbVolume.setMinimumSize(new Dimension(30,26));
        jpbVolume.setForeground(Constraints.GUI_RED);
        jpbVolume.setBackground(Color.DARK_GRAY);
        jpbVolume.setUI(new MotifProgressBarUI());
        jpbVolume.setBorderPainted(false);
        jpbVolume.setStringPainted(true);
        jpbVolume.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                float value = e.getX() / (float) jpbVolume.getWidth() * 100;
                jpbVolume.setValue((int) value);
                c.setGain(value);
            }
        });
        jpbVolume.addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {
                float value = e.getX() / (float) jpbVolume.getWidth() * 100;
                jpbVolume.setValue((int) value);
                c.setGain(value);
            }
        });
        gbc.gridx++;
//        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.1;
        gbc.insets = new Insets(0, 0, 0, 10);
        pp.add(jpbVolume, gbc);
        return pp;
    }
}
