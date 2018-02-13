package server.datamodel;

import musicinfo.PlaylistID;

/**
 *
 * @author Marc
 */
public interface DMListener {

    public void showError(String error);

    public void updatePlaylists();

    public void updateSongs();

    public void updatePlaylist(PlaylistID playlist);

    // XXX added parameter: and why??
    //public void updateSongs(Map<Integer,Song> songs);

    // XXX changed parameter id -> playlist
    //public void updatePlaylist(Playlist playlist);
    
    // XXX hab die mal rausgenommen, was sollte die ursprünglich tun?
    //public void updatePlaylists();
}
