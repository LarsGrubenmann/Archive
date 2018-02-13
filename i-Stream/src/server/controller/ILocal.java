/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package server.controller;

import java.io.File;
import java.util.*;
import musicinfo.*;

/**
 *
 * @author Uzul
 */
public interface ILocal {

    public void terminate();

    public void addFile(File file);

    public void addSongToPlaylist(PlaylistID pl, Song song);

    public Playlist createNewPlaylist(String name);

    public Collection<Song> getSongs();

    public Collection<PlaylistID> getPlaylistIDs();

    public Playlist getPlaylist(PlaylistID plid);

    public void removePlaylist(PlaylistID plid);

    public void removeSongFromPlaylist(PlaylistID plid, int track);

    public Collection<Song> searchSong(String term);
    
    public void showError(String s);

    public Map<Integer, Song> searchSongInPlaylist(PlaylistID plid, String term);

    public void updateTag(Song song);

    public void removeSong(Song song);
}
