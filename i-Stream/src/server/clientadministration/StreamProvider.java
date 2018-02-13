package server.clientadministration;

import def.Constraints;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import musicinfo.Playlist;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import musicinfo.Song;

/**
 * @author manuel, fatih, peter, fe
 * @version 0.1 angelegt und bearbeitet
 */
public class StreamProvider extends Thread {

    /**
     * Thread status
     */
    private boolean isAlive;
    private boolean isStreaming;
    private final Semaphore loopInitTurnstile;
    private final Semaphore loopExitTurnstile;
    private int doneCounter = 0;
    /**
     * Momentane Musikquelle
     */
    private Playlist playlist;
    private int trackID;
    private final int providerID;
    private final Collection<ClientInfo> synchronizedClients;

    public StreamProvider(int provID) {
        isAlive = true;
        providerID = provID;
        synchronizedClients = new LinkedList<ClientInfo>();
        loopInitTurnstile = new Semaphore(0);
        loopExitTurnstile = new Semaphore(0);
        start();
    }

    @Override
    public void run() {
        byte[] buff = new byte[Constraints.STREAM_BUFER];
//        ByteBuffer buff = ByteBuffer.allocate(Constraints.STREAM_BUFER);
        int l;
        Song s;
        FileInputStream in;
        try {
            while (isAlive) {
                try {
                    loopInitTurnstile.acquire();
                    System.out.println("sp loop init");
                    s = playlist.getSong(trackID);
                    System.out.println(s);
                    in = new FileInputStream(s.getFile());

                    Collection<Socket> socks = new ArrayList<Socket>();
                    Collection<OutputStream> outs = new ArrayList<OutputStream>();

                    int i = 0;// TODO local multi connection
                    for (ClientInfo c : synchronizedClients) {
                        try {
                            // Verbindungsaufbau anfordern
                            c.facadeRef.remotePlay(s, trackID);
                            Socket sock = new Socket(c.address, Constraints.TCP_PORT + i++);
                            socks.add(sock);
                            outs.add(sock.getOutputStream());
                        } catch (RemoteException ex) {
                            Logger.getLogger(ClientManager.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    while (isStreaming && (l = in.read(buff)) != -1) {
                        for (OutputStream o : outs) {
                            o.write(buff, 0, l);
                            // TODO wenn client abgebrochen. welcher und remove
                        }
                    }
                    for (Socket o : socks) {
                        o.close();
                    }
                    trackID = (trackID + 1) % playlist.size();
                } catch (InterruptedException ie) {
                    // terminate
                }
                loopExitTurnstile.release();
                System.out.println("sp loop exit");
            } // while (isAlive)
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void addClient(ClientInfo ci) {
        stopStream();// TODO weiter oben?
        try {
            // TODO weiter oben?
            ci.setProviderID(providerID);
            ci.facadeRef.setProviderID(providerID);
        } catch (RemoteException ex) {
            Logger.getLogger(StreamProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
        synchronizedClients.add(ci);
        // Clientliste aller beteiligten Clients updaten
        for (ClientInfo cur : synchronizedClients) {
            System.out.println(cur + ", " + cur.facadeRef);
            try {
                cur.facadeRef.updateSynchronisation(synchronizedClients);
            } catch (RemoteException ex) {
                Logger.getLogger(ClientManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public synchronized void removeClient(ClientInfo ci) {
        stopStream();// TODO weiter oben?
        synchronizedClients.remove(ci);
        // Clientliste aller beteiligten Clients updaten
        for (ClientInfo cur : synchronizedClients) {
            try {
                cur.facadeRef.updateSynchronisation(synchronizedClients);
            } catch (RemoteException ex) {
                Logger.getLogger(ClientManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public synchronized boolean isSingle() {
        return synchronizedClients.size() == 1;
    }

    public synchronized void clientSongDone() {
        doneCounter++;
        if (doneCounter == synchronizedClients.size()) {
            loopInitTurnstile.release();
            doneCounter = 0;
        }
    }

    public synchronized void pauseStream() {
        // ignore, go on filling the buffers
        // sollte man client-seitig stop aufrufen  falls dessen buffer überfluten?
        //Für jeden client in synchronizedClients remotePause() aufrufen
        for (ClientInfo cur : synchronizedClients) {
            try {
                cur.facadeRef.remotePause();
            } catch (RemoteException ex) {
                Logger.getLogger(ClientManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public synchronized void stopStream() {
        if (isStreaming) {
            try {
                System.out.println("sp stop");
                isStreaming = false;
                //wait(100);
                loopExitTurnstile.acquire();
                for (ClientInfo c : synchronizedClients) {
                    try {
                        c.facadeRef.remoteStop();
                    } catch (RemoteException ex) {
                        // TODO remove client savely
                    }
                }
//            try {
//                System.out.println("sp stop wait");
//                loopExitTurnstile.acquire();
//                System.out.println("sp stop end");
//            } catch (InterruptedException ex) {
//                // Should not happen
//                Logger.getLogger(StreamProvider.class.getName()).log(Level.SEVERE, null, ex);
//            }
            } catch (InterruptedException ex) {
                Logger.getLogger(StreamProvider.class.getName()).log(Level.SEVERE, null, ex);
            }
//            try {
//                System.out.println("sp stop wait");
//                loopExitTurnstile.acquire();
//                System.out.println("sp stop end");
//            } catch (InterruptedException ex) {
//                // Should not happen
//                Logger.getLogger(StreamProvider.class.getName()).log(Level.SEVERE, null, ex);
//            }
        }
    }

    public synchronized void terminate() {
        isAlive = false;
        stopStream();
        interrupt();
    }

    public synchronized void requestStream(Playlist pl, int trackID) {
        stopStream();
        // TODO warten bis beendet
        this.playlist = pl;
        this.trackID = trackID;
        isStreaming = true;
        loopInitTurnstile.release();
    }//    /**
//     * XXX Test Method
//     * @param args
//     */
//    public static void main(String[] args) throws FileNotFoundException, IOException {
//
//        // Testfiles
//        Playlist pl = new Playlist();
//        File[] f = new File("." + File.separatorChar + "testfiles").listFiles();
//        for (int i = 0; i < f.length; i++) {
//            pl.addSong(i, new Song(i, f[i]));
//            System.out.println(i + ": " + f[i].getCanonicalPath());
//        }
//
//        // Provider
//        StreamProvider sp = new StreamProvider();
//        sp.start();
//
//        // Client hinzufügen
//        ClientInfo c = new ClientInfo(Inet4Address.getLocalHost(),
//                "loopback",
//                42,
//                null,
//                new Socket(Inet4Address.getLocalHost(), Constraints.TCP_PORT));
//        sp.addClient(c);
//
//        // Treiber
//        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//        String s;
//        while (!(s = br.readLine()).isEmpty()) {
//            if (s.equals("stop")) {
//                sp.stopStream();
//            } else {
//                try {
//                    int n = Integer.parseInt(s);
//                    sp.requestStream(pl, n);
//                } catch (NumberFormatException e) {
//                }
//            }
//        }
//        sp.terminate();
//    }
}
