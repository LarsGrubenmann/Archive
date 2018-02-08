package client.controller;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import musicinfo.Playlist;
import musicinfo.PlaylistID;
import musicinfo.Song;
import server.clientadministration.ClientInfo;

/**
 *
 * @author Marc und Boney
 * @version 0.1 angelegt und bearbeitet
 * FIXME Aussagekräftigere Parameternamen
 */
public interface IClient extends Remote {

    public void addClient(ClientInfo clientinfo) throws RemoteException;

    public void setProviderID(int id) throws RemoteException;
    
    public void remotePause() throws RemoteException;

    public void remotePlay(Song song, int pos) throws RemoteException;

    public void remoteStop() throws RemoteException;

    public void removeClient(ClientInfo clientinfo) throws RemoteException;
    
    public void showError(String error) throws RemoteException;
    
    public void updatePlaylist(Playlist playlist) throws RemoteException;
    
    public void updatePlaylists(Collection<PlaylistID> playlistIDs) throws RemoteException;
    
    public void updateSynchronisation(Collection<ClientInfo> clients) throws RemoteException;

}
