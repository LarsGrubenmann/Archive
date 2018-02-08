package musicinfo;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Marc und Boney
 * @version 0.1 angelegt und bearbeitet
 */
public class Song implements Serializable {

    private final int id;
    private final File file;// TODO transient
    private long duration;
    private String title;
    private String artist;
    private String album;
    private String year;
    private String genre;

    public Song(int id, File f) {
        this.id = id;
        this.file = f;
    }

    public String getArtist() {
        return artist;
    }

    public long getDuration() {
        return duration;
    }

    public File getFile() {
        return this.file;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getAlbum() {
        return this.album;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getYear() {
        return year;
    }

    public String getGenre() {
        return genre;
    }

    @Override
    public String toString() {
        return title;
    }

    @Override
    public boolean equals(Object s) {
        if (s instanceof Song) {
            return id == ((Song) s).id;
        }
        return super.equals(s);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.id;
        return hash;
    }
}
