/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.controller;

import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Map;
import musicinfo.*;
import server.clientadministration.ClientInfo;

/**
 * @author manuel, fatih, peter
 * @version 0.1 angelegt und bearbeitet
 */

//ChangeLog: added songDone
//
//
public interface IServer extends Remote {

    public ClientInfo registerClient(InetAddress address, String name) throws RemoteException;

    public void removeClient(int clientID) throws RemoteException;

    public void synchronize(int provID, int clientID) throws RemoteException;

    public int leaveSynchronized(int clientID) throws RemoteException;

    public Playlist getPlaylist(PlaylistID pl) throws RemoteException;

    public Collection<PlaylistID> getPlaylistIDs() throws RemoteException;

    public void requestStream(int provID, PlaylistID plID, int trackID) throws RemoteException;

    public void pauseStream(int provID) throws RemoteException;

    public void stopStream(int provID) throws RemoteException;

    public void songDone(int clientID) throws RemoteException;

    public Map<Integer, Song> searchSongInPlaylist(String term, PlaylistID pl) throws RemoteException;
}
