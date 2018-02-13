/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.controller;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import javax.swing.JOptionPane;
import musicinfo.Playlist;
import musicinfo.PlaylistID;
import musicinfo.Song;
import server.clientadministration.IClientAdministration;
import server.datamodel.IDataModel;

/**
 *
 * @author manu
 */
public class LocalFacade implements ILocal {

    private IClientAdministration cm; // XXX nur wegen dem terminieren???
    private IDataModel dataModel;

    public LocalFacade(IClientAdministration cm, IDataModel dm) {
        this.dataModel = dm;
        this.cm = cm;
    }

    public void terminate() {
        // TODO gui abschießen
        cm.terminate();
        dataModel.terminate();
        // XXX Not very nice
        System.exit(0);
    }

    public void addFile(File file) {
        dataModel.createSongFromFile(file);
    }

    public void addSongToPlaylist(PlaylistID pl, Song song) {
        dataModel.addSongToPlaylist(pl, song);
    }

    public Playlist createNewPlaylist(String name) {
        return dataModel.createNewPlaylist(name);
    }

    public Collection<Song> getSongs() {
        return dataModel.getSongs();
    }

    public Collection<Song> searchSong(String term) {
        return dataModel.searchSong(term);
    }

    public Map<Integer, Song> searchSongInPlaylist(PlaylistID plid, String term) {
        return dataModel.searchSongInPlaylist(plid, term);
    }

    public void updateTag(Song song) {
        dataModel.updateTag(song);
    }

    public Collection<PlaylistID> getPlaylistIDs() {
        return dataModel.getPlaylistIDs();
    }

    public Playlist getPlaylist(PlaylistID plid) {
        return dataModel.getPlaylist(plid);
    }

    public void showError(String error) {
        JOptionPane.showMessageDialog(null, error,
                "iStream: error", JOptionPane.WARNING_MESSAGE);
    }

    public void removePlaylist(PlaylistID plid) {
        dataModel.removePlaylist(plid);
    }

    public void removeSongFromPlaylist(PlaylistID plid, int track) {
        dataModel.removeSongFromPlaylist(plid, track);
    }

    public void removeSong(Song song) {
        dataModel.removeSong(song);
    }
//    public void showError(String s) {        
//        dataModel.showError(s);
//    }
}
