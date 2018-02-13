/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.controller;

import java.net.InetAddress;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import server.datamodel.*;
import java.util.Collection;
import java.util.Map;
import musicinfo.Playlist;
import musicinfo.PlaylistID;
import musicinfo.Song;
import server.clientadministration.ClientInfo;
import server.clientadministration.IClientAdministration;

/**
 * @author manuel, fatih, peter
 * @version 0.1 angelegt und bearbeitet
 */
public class RemoteFacade extends UnicastRemoteObject implements IServer {

    private IClientAdministration clientAdministration;
    private IDataModel datamodel;

//    public RemoteFacade() {
//
//        throw new UnsupportedOperationException("Not yet implemented");
//    }
    public RemoteFacade(IClientAdministration ca, IDataModel dm) throws RemoteException {
        super();
        this.clientAdministration = ca;
        this.datamodel = dm;
    }

    /**
     * Funktionsaufruf wird an die ClientManager-Klasse delegiert
     * @since 0.1
     * @see clientAdministration.leaveSynchronized(clientID)
     * @param clientID
     */
    public int leaveSynchronized(int clientID) {
        return clientAdministration.leaveSynchronized(clientID);
    }

    /**
     * Funktionsaufruf wird an die ClientManager-Klasse delegiert
     * @since 0.1
     * @see clientAdministration.pauseStream(provID)
     * @param provID
     */
    public void pauseStream(int provID) {
        clientAdministration.pauseStream(provID);
    }

    /**
     * Funktionsaufruf wird an die ClientManager-Klasse delegiert
     * @since 0.1
     * @see clientAdministration.registerClient(name)
     * @param name
     */
    public ClientInfo registerClient(InetAddress address, String name) throws RemoteException, AccessException {
        return clientAdministration.registerClient(address, name);
    }

    /**
     * Funktionsaufruf wird an die ClientManager-Klasse delegiert
     * @since 0.1
     * @see clientAdministration.removeClient(clientID)
     * @param clientID
     */
    public void removeClient(int clientID) {
        clientAdministration.removeClient(clientID);
    }

    /**
     * Funktionsaufruf wird an die ClientManager-Klasse delegiert
     * @since 0.1
     * @see clientAdministration.requestStream(provID, plID, trackID)
     * @param provID, plID, trackID
     */
    public void requestStream(int provID, PlaylistID plID, int trackID) {
        clientAdministration.requestStream(provID, plID, trackID);
    }

    /**
     * Funktionsaufruf wird an die ClientManager-Klasse delegiert
     * @since 0.1
     * @see clientAdministration.stopStream(provID)
     * @param provID
     */
    public void stopStream(int provID) {
        clientAdministration.stopStream(provID);
    }

    /**
     * Funktionsaufruf wird an die ClientManager-Klasse delegiert
     * @since 0.1
     * @see clientAdministration.synchronize(provID, clientID)
     * @param provID, clientID
     */
    public void synchronize(int provID, int clientID) {
        clientAdministration.synchronize(provID, clientID);
    }

    /**
     * Funktionsaufruf wird an die DBConnection-Klasse delegiert
     * @since 0.1
     * @see datamodel.getPlaylist(pl)
     * @param pl
     */
    public Playlist getPlaylist(PlaylistID pl) {
        return datamodel.getPlaylist(pl);
    }

    /**
     * Funktionsaufruf wird an die DBConnection-Klasse delegiert
     * @since 0.1
     * @see datamodel.getPlaylistIDs()
     * @param 
     */
    public Collection<PlaylistID> getPlaylistIDs() {
        return datamodel.getPlaylistIDs();
    }

    /**
     * Funktionsaufruf wird an die DBConnection-Klasse delegiert
     * @since 0.1
     * @see datamodel.searchSongInPlaylist(pl, term)
     * @param pl, term
     */
    public Map<Integer, Song> searchSongInPlaylist(String term, PlaylistID pl) {
        return datamodel.searchSongInPlaylist(pl, term);
    }

    public void songDone(int clientID) throws RemoteException {
        clientAdministration.clientSongDone(clientID);
    }
    
}
