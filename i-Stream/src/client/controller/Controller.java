package client.controller;

import client.gui.MainFrame;
import client.musicdecoder.StreamReceiver;
import def.Constraints;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import musicinfo.Playlist;
import musicinfo.PlaylistID;
import musicinfo.Song;
import server.clientadministration.ClientInfo;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.util.Map;
import java.util.logging.FileHandler;
import javax.swing.UIManager;
import server.controller.IServer;

/**
 * Das RMI findet hier statt während Änderungen an der GUI an den CCListener weitergemeldet werden
 *
 * @author Marc und Boney
 * @version 0.1.1 configs
 * 0.1 angelegt und bearbeitet
 */
public class Controller {

    private static final Logger logger = Logger.getLogger(Controller.class.getName());
    private boolean scanning;
    // Config
    private String clientName;
    private String serverName;
    // RMI 
    private IServer conn;
    // Musik
    private StreamReceiver decoder;
    // GUI + Kommunikation
    private MainFrame gui;
    public CCListener listener;
    private PlaylistID selectedPlaylist;
    // TODO change added id
    // Dieser Client selbst???
    private InetAddress myAddress;
    private int myID;
    private int myProviderID;
//    public ClientInfo me;

    public Controller() {
        readConfig();
        gui = new MainFrame(this);
        decoder = new StreamReceiver(this);
        try {
            myAddress = InetAddress.getLocalHost();
        } catch (java.net.UnknownHostException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        // Login
        listener.requestLogin(clientName, serverName);
    }

    /**
     * Startet den Client
     * @since       0.1
     * @param args  Argumente
     */
    public static void main(String args[]) {
        // Setup logger root
        Logger root = Logger.getLogger("client");
        root.setLevel(Level.CONFIG);
        try {
            root.addHandler(new FileHandler("iStream-Client-%g.log", 25, 1, true));
        } catch (IOException iOException) {
            root.log(Level.SEVERE, null, iOException);
        } catch (SecurityException securityException) {
            root.log(Level.CONFIG, null, securityException);
        }
        // Switch L&F
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            root.log(Level.INFO, null, e);
        }
        // Start
        Controller c = new Controller();
    }

    public void selectPlaylist(PlaylistID plid) {
        try {
            selectedPlaylist = plid;
            listener.updatePlaylist(conn.getPlaylist(selectedPlaylist));
        } catch (RemoteException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public void play(int track) {
        try {
            conn.requestStream(myProviderID, selectedPlaylist, track);
        } catch (RemoteException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public void pause() {
        try {
            conn.pauseStream(myProviderID);
        } catch (RemoteException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public void changeSynchronization(int clientID, boolean sync) {
        try {
            if (sync) {
                conn.synchronize(myProviderID, clientID);

            } else {
                conn.leaveSynchronized(myID);
            }
        } catch (RemoteException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public void stop() {
        try {
            conn.stopStream(myProviderID);
        } catch (RemoteException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public Map<Integer, Song> searchSongInPlaylist(String term) {
        try {
            return conn.searchSongInPlaylist(term, selectedPlaylist);
        } catch (RemoteException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private InetAddress scann() {
        try {
            // Request UDP Echo
            final DatagramSocket dgs = new DatagramSocket(Constraints.TCP_PORT + 1);
            dgs.setSoTimeout(300);
            Thread t = new Thread() {

                @Override
                public void run() {
                    try {
                        DatagramPacket dgp = new DatagramPacket(new byte[0], 0,
                                InetAddress.getByAddress(new byte[]{(byte) 255, (byte) 255, (byte) 255, (byte) 255}),
                                Constraints.TCP_PORT);
                        while (!dgs.isClosed()) {
                            dgs.send(dgp);
                            System.out.println("sended request");
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ex) {
                                // ignore
                            }
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            t.start();
            // Wait for request
            DatagramPacket dgp = new DatagramPacket(new byte[16], 16);
            System.out.println("wait for answer");
            scanning = true;
            while (scanning) {
                try {
                    dgs.receive(dgp);
                    scanning = false;
                } catch (SocketTimeoutException ste) {
                    // ignore
                }
            }
            dgs.close();
            byte[] address = new byte[dgp.getLength()];
            System.arraycopy(dgp.getData(), 0, address, 0, address.length);
            // Own address
            myAddress = InetAddress.getByAddress(address);
            // Server address
            return dgp.getAddress();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void login(String name, String servname) {
        scann().getCanonicalHostName();
        System.setProperty("java.rmi.server.hostname", myAddress.getHostAddress());





        clientName = name;
        serverName = servname;
        System.out.println("client login " + myAddress.getHostAddress());
        System.out.println(serverName);
//        RemoteServer.setLog(System.out);
        try {
            LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
        } catch (RemoteException ex) {
            System.out.println("reg already running");
        }
        try {
            RemoteFacade remFa = new RemoteFacade(this);
//            System.out.println("1");
            try {
                Naming.rebind("rmi://" + myAddress.getHostAddress() + ":" + Registry.REGISTRY_PORT + "/" + Constraints.RMI_CLIENT_NAME, remFa);
//                System.out.println("1.5");
                conn = (IServer) Naming.lookup("rmi://" + servname + ":" + Registry.REGISTRY_PORT + "/" + Constraints.RMI_SERVER_NAME);
//                System.out.println("2");
            } catch (MalformedURLException ex) {
                Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (java.net.UnknownHostException ex) {
//                Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (RemoteException ex) {
            listener.showError("Connection failed!");
            logger.log(Level.SEVERE, null, ex);
        } catch (NotBoundException ex) {
            listener.showError("Server not running on \"" + servname + "\".");
            logger.log(Level.FINE, null, ex);
        }
        try {
//            System.out.println("3");
            // TODO change in int
            ClientInfo myInfo = conn.registerClient(myAddress, name);
            myID = myInfo.id;
            myProviderID = myInfo.getProviderID();
        } catch (RemoteException ex) {
            listener.showError("Register as new Client failed!");
            logger.log(Level.SEVERE, null, ex);
        }
        try {
            listener.updatePlaylists(conn.getPlaylistIDs());
        } catch (RemoteException ex) {
            listener.showError("Connection Refused!");
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private void readConfig() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File("." + File.separatorChar + "config")));
            clientName = reader.readLine();
            serverName = reader.readLine();
            reader.close();
        } catch (IOException ioe) {
            clientName = "";
            serverName = "";
        }
    }

    private void writeConfig() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("." + File.separatorChar + "config")));
            writer.write(clientName);
            writer.newLine();
            writer.write(serverName);
            writer.newLine();
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void songDone() {
        try {
            conn.songDone(myID);
        } catch (RemoteException ex) {
            logger.log(Level.SEVERE, null, ex);
            listener.showError(ex.toString());
        }
    }

    /**
     * Delegation an das CCListener Objekt
     * @param   clientInfo Die ClientInfo des zu hinzuzufügenden Clients
     * @see     listener.addClient(ClientInfo clientInfo)
     * @since   0.1
     */
    public void addClient(ClientInfo clientInfo) {
        listener.addClient(clientInfo);
    }

    /**
     * Delegation an das CCListener Objekt
     * 
     * @see listener.remotePause()
     * @since 0.1
     */
    public void remotePause() {
        decoder.pauseStream();
        listener.remotePause();
    }

    /**
     * Delegation  an das CCListener Objekt
     * @param   song Der abzuspielende Song
     * @see     listener.remotePlay(Song song)
     * @since   0.1
     */
    public void remotePlay(Song song, int pos) {
        decoder.startStream(song);
        listener.remotePlay(song, pos);
    }

    /**
     * Delegation an das CCListener Objekt
     * @see     listener.remoteStop()
     * @since   0.1
     */
    public void remoteStop() {
        decoder.stopStream();
        listener.remoteStop();
    }

    public void setGain(float value) {
        decoder.setGain(value);
    }

    /**
     * Delegation an das CCListener Objekt
     * @param   clientID Der zu entfernende Client
     * @see     listener.removeClient
     * @since   0.1
     */
    public void removeClient(ClientInfo clientinfo) {
        listener.removeClient(clientinfo);
    }

    /**
     * Der Listener zur Aufnahme von Änderungen, die die GUI betreffen, wird angemeldet
     * @param   listener Der anzumeldende Listener
     * @since   0.1
     */
    public void setListener(CCListener listener) {
        this.listener = listener;
    }

    /**
     * Der Fehler wird an den Listener weitergereicht
     * @param   error
     * @since   0.1
     */
    public void showError(String error) {
        listener.showError(error);
    }

    /**
     * terminate() beendet die Controller Komponente
     */
    public void terminate() {
        scanning = false;
        try {
            if (conn != null) {
                conn.removeClient(myID);
            }
        } catch (RemoteException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        decoder.terminate();
        gui.setVisible(false);
        gui.dispose();
        writeConfig();
        // XXX Not very nice
        System.exit(0);
    }

    /**
     * Delegation an das CCListener Objekt
     * @param   playlist Die zu aktualisierende Playlist
     * @see     listener.updatePlaylist(Playlist playlist)
     * @since   0.1
     */
    public void updatePlaylist(Playlist playlist) {
        // TODO nur id bekommen
        // überprüfen ob sie zur selectierten passt
        // und dann vom server holen und an die gui senden
        if (playlist.getID().equals(selectedPlaylist)) {
            listener.updatePlaylist(playlist);
        }
    }

    /**
     * Delegation an das CClistener Objekt
     * @param   playlistIDs mehrere zu aktualisierende Playlists
     * @see     listener.updatePlaylists(Collection<PlaylistID> playlistIDs)
     * @since   0.1
     */
    public void updatePlaylists(Collection<PlaylistID> playlistIDs) {
        listener.updatePlaylists(playlistIDs);
    }

    /**
     * Delegation an das CClistener Objekt
     * @param   clientIDs die Liste, die der Client zu seiner synchronisierten Liste hinzufügen will
     * @see     updateSynchronisation(Collection<Integer> clientIDs)
     * @since   0.1
     */
    public void updateSynchronisation(Collection<ClientInfo> clientIDs) {
        listener.updateSynchronisation(clientIDs);
    }

    public void setProviderID(int id) {
        myProviderID = id;
    }
}
