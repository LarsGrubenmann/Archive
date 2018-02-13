package server.controller;

import def.Constraints;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import musicinfo.*;
import server.clientadministration.ClientManager;
import server.clientadministration.IClientAdministration;
import server.datamodel.DMFacade;
import server.datamodel.IDataModel;
import server.gui.MainFrame;

/**
 *
 * @author Lars
 */
public class Controller {

    private MainFrame gui;
    private RemoteFacade remote;
    private ILocal local;
    private IDataModel dm;
    private IClientAdministration cm;

    // XXX Wenn wirklich nur das hier abläuft kann auch alles wieder in eine static methode
    public Controller() {

        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            // XXX genau das ist ja unser problem: getLocalHost liefert die
            // 127.0.0.1 da eh im hostname steht
            System.setProperty("java.rmi.server.hostname", ip);
            try {
                LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
            } catch (RemoteException remoteException) {
                System.out.println("reg already running");
            }
//            RemoteServer.setLog(System.out);

            dm = new DMFacade();
            cm = new ClientManager(dm);
            local = new LocalFacade(cm, dm);
            gui = new MainFrame(local, dm);
            remote = new RemoteFacade(cm, dm);
            try {
                Naming.rebind("rmi://" + InetAddress.getLocalHost().getCanonicalHostName() + ":" + Registry.REGISTRY_PORT + "/" + Constraints.RMI_SERVER_NAME, remote);
            } catch (UnknownHostException ex) {
                Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
            } catch (MalformedURLException ex) {
                Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (RemoteException re) {
            re.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            //System.out.println("no network access");
        }
    }

    public static void main(String[] args) {
        // Setup logger root
        Logger root = Logger.getLogger("server");
        root.setLevel(Level.CONFIG);
        try {
            root.addHandler(new FileHandler("iStream-Server-%g.log", 25, 1, true));
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
        // UDP - Echo Thread
        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    DatagramSocket dgs = new DatagramSocket(Constraints.TCP_PORT);
                    DatagramPacket dgp = new DatagramPacket(new byte[0], 0);
                    while (true) {
                        // Wait for request
                        dgs.receive(dgp);
                        InetAddress ia = dgp.getAddress();
                        System.out.println("request from " + ia);
                        byte[] address = ia.getAddress();
                        DatagramPacket dgP = new DatagramPacket(address, address.length, ia, Constraints.TCP_PORT + 1);
                        dgs.send(dgP);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        // Zoooombies
        t.setDaemon(true);
        t.start();

        // Start
        Controller ss = new Controller();
    }
}
