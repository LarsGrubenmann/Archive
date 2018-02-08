package musicinfo;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author Marc und Boney
 * @version 0.1.1 sortierte map (getValues() liefert sortierte folge)
 * 0.1 angelegt und bearbeitet
 */
public class Playlist implements Serializable {

    private final PlaylistID id;
    private final Map<Integer, Song> songs;

    public Playlist(PlaylistID playlistID) {
        this.id = playlistID;
        this.songs = new TreeMap<Integer, Song>();
    }

    public void addSong(int num, Song s) {
        songs.put(num, s);
    }

    public Song getSong(int num) {
        return songs.get(num);
    }

    public Map<Integer, Song> getSongTabel() {
        return songs;
    }

//    public Collection<Song> getSongs() {
//        return songs.values();
//    }

    public PlaylistID getID() {
        return id;
    }

    public int size() {
        return songs.size();
    }

    @Override
    public boolean equals(Object p) {
        if (p instanceof Playlist) {
            return id.equals(((Playlist) p).id);
        }
        return super.equals(p);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
}
