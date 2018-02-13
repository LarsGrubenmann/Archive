/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.clientadministration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import musicinfo.PlaylistID;
import client.controller.IClient;
import def.Constraints;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Set;
import server.datamodel.DMListener;
import server.datamodel.IDataModel;

/**
 * @author manuel, fatih, peter
 * @version 0.1 angelegt und bearbeitet
 */
public class ClientManager implements IClientAdministration {

    private final Logger logger = Logger.getLogger(ClientManager.class.getName());
    private final IDataModel datamodel; // TODO Datamodell oder die local fassade??????
    private final Map<Integer, ClientInfo> clients;
    private final Map<Integer, StreamProvider> streams;

    public ClientManager(IDataModel dm) {
        datamodel = dm;
        clients = new HashMap<Integer, ClientInfo>();
        streams = new HashMap<Integer, StreamProvider>();
        dm.addListener(new DMListener() {

            public void showError(String error) {
                // Don't let the clients know
            }

            public void updatePlaylists() {
                for (ClientInfo c : clients.values()) {
                    try {
                        // TODO auch wieder anders rum
                        // Client erfährt dass es ne änderung gibt und er entscheidet ob es die wert is
                        c.facadeRef.updatePlaylists(datamodel.getPlaylistIDs());
                    } catch (RemoteException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
            }

            public void updateSongs() {
                // Don't let the clients know
            }

            public void updatePlaylist(PlaylistID playlist) {
                for (ClientInfo c : clients.values()) {
                    try {
                        // TODO auch wieder anders rum (siehe kommentar auf clientseite)
                        // Client erfährt dass es ne änderung gibt und er entscheidet ob es die wert is
                        c.facadeRef.updatePlaylist(datamodel.getPlaylist(playlist));
                    } catch (RemoteException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }

//    public static void main(String args[]) {
//        try {
//            LocateRegistry.createRegistry(Constraints.RMI_PORT);
//            //DataModel dm = null;
//            ClientManager cm = new ClientManager(null);
//            RemoteFacade remFa = new RemoteFacade(cm, cm.datamodel);
//            IServer stub = (IServer) UnicastRemoteObject.exportObject(remFa, Constraints.RMI_PORT);
//            RemoteServer.setLog(System.out);
//
//            Registry regLocal = LocateRegistry.getRegistry();
//            regLocal.rebind("IServer", stub);
//
//            Registry regRemote = LocateRegistry.getRegistry();
//            IClient client = (IClient) regRemote.lookup("IClient");
//
//            conn = client;
//        } catch (Exception ex) {
//            logger.log(Level.SEVERE, null, ex);
//        }
//
//    }
    /*
     * clientSongDone zählt nur den doneCounter vom StreamProvider hoch. so habs ich zumindest verstanden, aber
     * mussen wir da nicht ne clientid oder ne provider id mitgeben damit wir wissen welchen counter wir hochzählen sollen?
     */
    public void clientSongDone(int clientID) {
        ClientInfo client = clients.get(clientID);
        streams.get(client.getProviderID()).clientSongDone();
    }

    public void requestStream(int provID, PlaylistID plID, int trackID) {
        System.out.println("ca request " + provID + " " + plID + " " + trackID);
        StreamProvider provider = streams.get(provID);
        provider.requestStream(datamodel.getPlaylist(plID), trackID);
    }

    public void pauseStream(int provID) {
        StreamProvider provider = streams.get(provID);
        provider.pauseStream();
    }

    public void stopStream(int provID) {
        StreamProvider provider = streams.get(provID);
        provider.stopStream();
    }

    /**
     * Funktionsaufruf wird an die ClientManager-Klasse delegiert
     * @since 0.1
     * @pre for
     * @param name
     */
    public ClientInfo registerClient(InetAddress address, String name) throws RemoteException, AccessException {
        System.out.println("cm register " + address);
        IClient ref = null;
//        System.out.println("1");

        try {
            Registry reg = LocateRegistry.getRegistry(address.getHostAddress());
            ref = (IClient) reg.lookup(Constraints.RMI_CLIENT_NAME);
        } catch (NotBoundException ex) {
            logger.log(Level.SEVERE, null, ex);
//        } catch (MalformedURLException ex) {
//            logger.log(Level.SEVERE, null, ex);
        }
        // Client

        InetAddress ip = null;
//        System.out.println("3");
        ip = address;
//        System.out.println("4");
        System.out.println("register " + ip.getCanonicalHostName());
        ClientInfo client = new ClientInfo(ip, name, getNewClientID(), ref);
//        System.out.println("5");

        // Create a new Streamprovider
        int spID = getNewStreamProviderID();
        StreamProvider s = new StreamProvider(spID);
//        System.out.println("7");
        s.addClient(client);
        streams.put(spID, s);
//        client.setProviderID(spID);

        for (ClientInfo cur : clients.values()) {
            // Dem neuen Client alle anderen Clients hinzufügen
            System.out.println(client + " add " + cur);
            client.facadeRef.addClient(cur);
            // Allen bestehenden Clients den neuen Client hinzufügen
            System.out.println(cur + " add " + client);
            cur.facadeRef.addClient(client);
        }
//        System.out.println("6");
        clients.put(client.id, client);
        System.out.println("cm register end " + client.id + " - " + client.getProviderID());
        return client;
    }

    public void removeClient(int clientID) {
        int streamid = leaveSynchronized(clientID);
        streams.remove(streamid).terminate();
        ClientInfo ex = clients.get(clientID);
        clients.remove(clientID);

        for (ClientInfo cur : clients.values()) {
            try {
                cur.facadeRef.removeClient(ex);
            } catch (RemoteException e) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }

    public void synchronize(int provID, int clientID) {
        if (provID == clients.get(clientID).getProviderID()) {
            return;
        }
        System.out.println("cm sync " + provID + " - " + clientID);
        // Client aus seiner alten Synchronisation lösen
        int spID = leaveSynchronized(clientID);
        // Neu zugeordneter StreamProvider (durch leaveSynchronized) wird nicht benötigt und kann beendet werden
        streams.get(spID).terminate();
        streams.remove(spID);
        // Client dem neuen StreamProvider zuordnen
        StreamProvider newProvider = streams.get(provID);
        newProvider.addClient(clients.get(clientID));
    }

    /*
     * Haben wir hier nicht gesagt dass man beim verlassen eines streamproviders einen neuen zugewiesen bekommt
     * und leaveSync die neue StreamProviderID zurück gibt?
     * XXX ja war glaub so
     */
    public int leaveSynchronized(int clientID) {
        System.out.println("cm leav sync " + clientID);
        ClientInfo client = clients.get(clientID);
        // Provider des Clients holen
        int providerID = client.getProviderID();
        System.out.println("cm leav sync prov " + providerID);
        StreamProvider prov = streams.get(providerID);
//        if (prov != null) {
        if (prov.isSingle()) {
            // Alles ist ok, der Client hat schon seinen eigenen Provider
            return providerID;
        }
        prov.removeClient(client);
//        }
        // Neue StreamProvider ID finden
        int newSpID = getNewStreamProviderID();
        // Neuen StreamProvider erstellen und ihn in streams einfügen.
        prov = new StreamProvider(newSpID);
        streams.put(newSpID, prov);
        // Die neue Provider ID des Clients speichern
//        client.setProviderID(newSpID);
        // Den Client in synchronizedClients des neuen Providers hinzufügen
        prov.addClient(client);
        return newSpID;
    }

    public void terminate() {
        // ConcurrentModificationException
        while (!clients.isEmpty()) {
            removeClient(clients.keySet().iterator().next());
        }
    }
    /*
     *XXX CHANGELOG:    Rückgabe von void in Playlist geändert
     *                  Übergabeparameter von Playlist in PlaylistID geändert!
     */

    public void updatePlaylist(PlaylistID plID) {
        datamodel.getPlaylist(plID);
        return;
    }
    /*
     *XXX CHANGELOG:    Rückgabe von void in Collection<Playlist> geändert
     *XXX Frage: Wozu Übergabeparameter? Wollen wir nicht sowieso alle Playlists updaten?
     */

    public void updatePlaylists(Collection<PlaylistID> pls) throws RemoteException {
        for (ClientInfo clientid : clients.values()) {
            clientid.facadeRef.updatePlaylists(pls);
        }
    }

//    public void updateSynchronization(Collection<ClientInfo> clients) throws RemoteException {
//        for (ClientInfo client : clients) {
//            client.facadeRef.updateSynchronisation(clients);
//            //TODO updateSynchronization parameter ändern!!!
//        }
//
//    }
    private int getNewStreamProviderID() {
        int i = 1;
        while (streams.containsKey(i)) {
            i++;
        }
        return i;
    }

    private int getNewClientID() {
        int i = 1;
        while (clients.containsKey(i)) {
            i++;
        }
        return i;
    }
}
