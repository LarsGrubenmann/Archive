package client.controller;

import java.util.Collection;
import musicinfo.Playlist;
import musicinfo.PlaylistID;
import musicinfo.Song;
import server.clientadministration.ClientInfo;

/**
 *
 * @author Marc und Boney
 * @version 0.1 angelegt und bearbeitet
 */
public interface CCListener {

    public void requestLogin(String clientName, String serverName);

    public void addClient(ClientInfo clientinfo);

    public void removeClient(ClientInfo clientinfo);

    public void remotePause();

    public void remotePlay(Song song, int pos);

    public void remoteStop();

    public void updateProgress(int progress);

    public void showError(String error);

    public void updatePlaylist(Playlist playlist);

    public void updatePlaylists(Collection<PlaylistID> playlistIDs);

    public void updateSynchronisation(Collection<ClientInfo> clientIDs);
}
