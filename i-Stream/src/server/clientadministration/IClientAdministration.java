/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.clientadministration;

import java.io.IOException;
import java.net.InetAddress;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.util.Collection;
import musicinfo.*;

/**
 * @author manuel, fatih, peter
 * @version 0.1 angelegt und bearbeitet
 */
public interface IClientAdministration {

    public void clientSongDone(int clientID);

    public int leaveSynchronized(int clientID);

    public void pauseStream(int provID);

    public ClientInfo registerClient(InetAddress address, String name) throws RemoteException, AccessException;

    public void removeClient(int clientID);

    public void requestStream(int provID, PlaylistID plID, int trackID);

    public void stopStream(int provID);

    public void synchronize(int provID, int clientID);

    public void terminate();

    public void updatePlaylist(PlaylistID plID);

    public void updatePlaylists(Collection<PlaylistID> pls) throws RemoteException;

//    public void updateSynchronization(Collection<ClientInfo> clients) throws RemoteException;
}
