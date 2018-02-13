package server.datamodel;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import musicinfo.Playlist;
import musicinfo.PlaylistID;
import musicinfo.Song;

/**
 * @author manuel, fatih, peter, marc
 * @version 0.2 angelegt, bearbeitet und ausimplementiert
 */
public class DMFacade implements IDataModel {

    private DBConnection dbConnection;
    private FileHandler filehandler;

    public DMFacade() {
        dbConnection = new DBConnection();
        filehandler = new FileHandler(dbConnection);
    }

    /**
     * Funktionsaufruf wird an das DBConnection Objekt delegiert
     * @since 0.2
     * @see dbconnection.terminate()
     *
     */
    public void terminate() {
        filehandler.terminate();
        dbConnection.terminate();
    }

    /**
     * Funktionsaufruf wird an das DBConnection Objekt delegiert
     * @since 0.2
     * @see dbconnection.addSongToPlaylist(plID, song);
     * @param plID
     * @param song
     */
    public void addSongToPlaylist(PlaylistID plID, Song song) {
        dbConnection.addSongToPlaylist(plID, song);
        return;
    }

    /**
     * Funktionsaufruf wird an das DBConnection Objekt delegiert
     * @since 0.2
     * @see dbconnection.createNewPlaylist(playlistname)
     * !!!!!!Habe hier name --> playliste geändert ,clyde,aber es zeigt mir noch einn fehler
     * @param playlistname
     *
     */
    public Playlist createNewPlaylist(String playlistname) {
        return dbConnection.createNewPlaylist(playlistname);
    }

    public void createSongFromFile(File file) {
        filehandler.createSongFromFile(file);
    }

    /**
     * Funktionsaufruf wird an das DBConnection Objekt delegiert
     * @since 0.2
     * @see dbconnection.getPlaylist(id)
     * @param id
     */
    public Playlist getPlaylist(PlaylistID id) {
        return dbConnection.getPlaylist(id);
    }

    /**
     * Funktionsaufruf wird an das DBConnection Objekt delegiert
     * @since 0.2
     * @see dbconnection.getPlaylistIDs()
     * @param
     */
    public Collection<PlaylistID> getPlaylistIDs() {
        return dbConnection.getPlaylistIDs();
    }

    /**
     * Funktionsaufruf wird an das DBConnection Objekt delegiert
     * @since 0.2
     * @see dbconnection.getSongs()
     *
     */
    public Collection<Song> getSongs() {
        return dbConnection.getSongs();
    }

    /**
     * Funktionsaufruf wird an das DBConnection Objekt delegiert
     * @since 0.2
     * @see dbconnection.removeSong(song)
     * @param song
     */
    public void removeSong(Song song) {
        dbConnection.removeSong(song);
    }

    /**
     * Funktionsaufruf wird an das DBConnection Objekt delegiert
     * @since 0.2
     * @see dbconnection.removeSongFromPlaylist(plID, song)
     * @param plID
     * @param song
     */
    public void removeSongFromPlaylist(PlaylistID plID, int track) {
        dbConnection.removeSongFromPlaylist(plID, track);
    }

    public void removePlaylist(PlaylistID plid) {
        dbConnection.removePlaylist(plid);
    }

    /**
     * Funktionsaufruf wird an das DBConnection Objekt delegiert
     * @since 0.2
     * @see dbconnection.searchSong(term)
     * @param term
     */
    public Collection<Song> searchSong(String term) {
        return dbConnection.searchSong(term);
    }

    /**
     * Funktionsaufruf wird an das DBConnection Objekt delegiert
     * @since 0.2
     * @see dbconnection.searchSongInPlaylist(plID, term)
     * @param plID
     * @param term
     */
    public Map<Integer, Song> searchSongInPlaylist(PlaylistID plID, String term) {
        return dbConnection.searchSongInPlaylist(plID, term);
    }

    public void addListener(DMListener listener) {
        dbConnection.addListener(listener);
    }

    /**
     * Funktionsaufruf wird an das FileHandler Objekt delegiert
     * @since 0.2
     * @see filehandler.updateTag(song)
     * @param song
     */
    public void updateTag(Song song) {
        filehandler.updateTag(song);
    }
}
