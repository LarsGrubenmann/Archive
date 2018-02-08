package client.controller;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import musicinfo.Playlist;
import musicinfo.PlaylistID;
import musicinfo.Song;
import server.clientadministration.ClientInfo;

/**
 * <b>RemoteFascade für Anfragen an den Client Controller</b>
 *
 * @author Marc und Boney
 * @version 0.1 angelegt und bearbeitet
 */
public class RemoteFacade extends UnicastRemoteObject implements IClient {

    private final Controller controller;

    public RemoteFacade(final Controller c) throws RemoteException {
        super();
        controller = c;
    }

    /**
     * Funktionsaufruf wird an das Controller Objekt delegiert
     * @since 0.1
     * @see controller.addClient(ClientInfo clientinfo)
     * @param clientinfo Die ClientInfo des zu hinzuzufügenden Clients
     */
    public void addClient(ClientInfo clientinfo) {
        controller.addClient(clientinfo);
    }

    /**
     * Funktionsaufruf wird an das Controller Objekt delegiert
     * @since 0.1
     * @see controller.remotePause()
     */
    public void remotePause() {
        controller.remotePause();
    }

    /**
     * Funktionsaufruf wird an das Controller Objekt delegiert
     * @since 0.1
     * @see controller.remotePlay(Song song)
     * @param song Der abzuspielende Song
     */
    public void remotePlay(Song song, int pos) {
        controller.remotePlay(song, pos);
    }

    /**
     * Funktionsaufruf wird an das Controller Objekt delegiert
     * @since 0.1
     * @see controller.remoteStop()
     */
    public void remoteStop() {
        controller.remoteStop();
    }

    /**
     * Funktionsaufruf wird an das Controller Objekt delegiert
     * @since 0.1
     * @see Controller#removeClient(server.clientadministration.ClientInfo)
     * @param ClientID Der zu entfernende Client
     */
    public void removeClient(ClientInfo clientinfo) {
        controller.removeClient(clientinfo);
    }

    /**
     * Funktionsaufruf wird an das Controller Objekt delegiert
     * @since 0.1
     * @see controller.showError(String error)
     * @param error Fehlermeldung
     */
    public void showError(String error) {
        controller.showError(error);
    }

    /**
     * Funktionsaufruf wird an das Controller Objekt delegiert
     * @since 0.1
     * @see controller.updatePlaylist(Playlist playlist)
     * @param playlist Die zu aktualisierende Playlist
     */
    public void updatePlaylist(Playlist playlist) {
        controller.updatePlaylist(playlist);
    }

    /**
     * Funktionsaufruf wird an das Controller Objekt delegiert
     * @since 0.1
     * @see controller.updatePlaylists(Collection<PlaylistID> playlistIDs)
     * @param playlistIDs mehrere zu aktualisierende Playlists
     */
    public void updatePlaylists(Collection<PlaylistID> playlistIDs) {
        controller.updatePlaylists(playlistIDs);
    }

    /**
     * Funktionsaufruf wird an das Controller Objekt delegiert
     * @since 0.1
     * @see controller.updateSynchronisation(Collection<Integer> clientIDs) 
     * @param clientID der Client, der andere Clients zu seiner Synchronisierten Liste hinzugefügen will
     * @param clientIDs die Liste, die der Client zu seiner synchronisierten Liste hinzufügen will
     */
    // TODO Javadoc stimmt glaub ich noch nicht so ganz...
    public void updateSynchronisation(Collection<ClientInfo> clientIDs) {
        controller.updateSynchronisation(clientIDs);
    }

    public void setProviderID(int id) {
        controller.setProviderID(id);
    }
}
