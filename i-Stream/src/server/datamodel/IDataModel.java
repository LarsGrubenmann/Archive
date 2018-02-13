/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.datamodel;

import java.io.File;
import musicinfo.*;
import java.util.Collection;
import java.util.Map;

/**
 * @author manuel, fatih, peter
 * @version 0.1 angelegt und bearbeitet
 */
public interface IDataModel {

    //public void showError(String s);

    public void terminate();

    public void createSongFromFile(File file);

    public void addSongToPlaylist(PlaylistID plID, Song song);

    public Playlist createNewPlaylist(String name);

    public Collection<Song> getSongs();

    public Collection<PlaylistID> getPlaylistIDs();

    public Playlist getPlaylist(PlaylistID id);

    public void removeSong(Song song);

    public void removeSongFromPlaylist(PlaylistID plID, int track);

    public void removePlaylist(PlaylistID plid);

    public Collection<Song> searchSong(String term);

    public Map<Integer, Song> searchSongInPlaylist(PlaylistID plID, String term);

    public void updateTag(Song song);

    public void addListener(DMListener listener);
}
