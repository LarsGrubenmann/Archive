package server.datamodel;

import def.Constraints;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import musicinfo.Song;
import org.farng.mp3.MP3File;
import org.farng.mp3.TagConstant;
import org.farng.mp3.TagException;
import org.farng.mp3.TagOptionSingleton;
import org.farng.mp3.id3.ID3v1_1;
import org.tritonus.share.sampled.file.TAudioFileFormat;

/**
 *
 * @author Marc, Felix
 * @version 0.1 angelegt und bearbeitet
 */
public class FileHandler extends Thread {

    private static final Logger logger = Logger.getLogger(DBConnection.class.getName());
    private boolean isAlive;
    private Semaphore lock;
    private DBConnection dbConn;
    private Queue<File> files;

    public FileHandler(DBConnection dbConnection) {
        isAlive = true;
        lock = new Semaphore(0);
        files = new LinkedList<File>();
        dbConn = dbConnection;
        start();
    }

    /**
     * Funktionsaufruf terminate() wird an das DBConnection Objekt delegiert und
     * eigene Objekte werden geschlossen
     * 
     * @since 0.1
     * @see dbconnection.terminate()
     */
    public void terminate() {
        isAlive = false;
        interrupt();
    }

    /**
     * Endlosschleife 
     */
    @Override
    public void run() {
        while (isAlive) {
            try {
                lock.acquire();
                System.out.println("fh start adding");
                addFileToDB(files.remove());
                System.out.println("fh end");
            } catch (InterruptedException ie) {
                // terminate
            }
        }
    }

    private void addFileToDB(File file) {
        if (file.isDirectory()) {
            for (File sub : file.listFiles(Constraints.MP3_IO_FILEFILTER)) {
                addFileToDB(sub);
            }
            return;
        }
        AudioFileFormat baseFileFormat = null;
        try {
            baseFileFormat = AudioSystem.getAudioFileFormat(file);
            Map properties = ((TAudioFileFormat) baseFileFormat).properties();

            String title, artist, album, genre, year;

            // duration : [Long], duration in microseconds.
            long duration = (Long) properties.get("duration");
            // title : [String], Title of the stream.
            if (properties.containsKey("title")) {
                title = (String) properties.get("title");
            } else {
                title = file.getName();
            }
            // author : [String], Name of the artist of the stream.
            if (properties.containsKey("author")) {
                artist = (String) properties.get("author");
            } else {
                artist = "-";
            }
            // album : [String], Name of the album of the stream.
            if (properties.containsKey("album")) {
                album = (String) properties.get("album");
            } else {
                album = "-";
            }
            // date : [String], The date (year) of the recording or release of the stream.
            if (properties.containsKey("date")) {
                year = (String) properties.get("date");
            } else {
                year = "-";
            }
            // mp3.id3tag.genre : [String], ID3 tag (v1 or v2) genre.
            if (properties.containsKey("mp3.id3tag.genre")) {
                genre = (String) properties.get("mp3.id3tag.genre");
            } else {
                genre = "-";
            }
            dbConn.addSong(file.getAbsolutePath(), duration, title, artist, album, year, genre);
        } catch (UnsupportedAudioFileException uafe) {
            // Kein (passendes) Audiofile
            logger.log(Level.FINEST, null, uafe);
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, null, ioe);
        }
    }

    public void createSongFromFile(File file) {
        files.add(file);
        System.out.println("fh add");
        lock.release();
    }

    /**
     * Diese Funktion dient zum Aktualisieren der Tags eines Songs. Hier wird die
     * Datei mit den ID3 Tags verändert, während das Song Objekt an die dbConnection
     * weitergeleitet wird, wo die Datenbankeinträge auch geändert werden.
     *
     * @param song
     */
    protected void updateTag(Song song) {
        try {
            File musikdatei = song.getFile();

            /**
             * its meeee, mario^^
             */
            MP3File mp3file = new MP3File(musikdatei);
            ID3v1_1 tag = new ID3v1_1();

            // setup id3v1
            tag.setTitle(song.getTitle());
            if (song.getAlbum() != null) {
                tag.setAlbum(song.getAlbum());
                tag.setAlbumTitle(song.getAlbum());
            }
            if (song.getArtist() != null) {
                tag.setArtist(song.getArtist());
            }
            if (song.getGenre() != null) {
                tag.setSongGenre(song.getGenre());
            }
            if (song.getYear() != null) {
                tag.setYear(song.getYear());
                tag.setYearReleased(song.getYear());
            }

            mp3file.setID3v1Tag(tag);
            mp3file.save(TagConstant.MP3_FILE_SAVE_WRITE);
            dbConn.updateSong(song);
        } catch (IOException iOException) {
            logger.log(Level.SEVERE, null, iOException);
        } catch (TagException tagException) {
            logger.log(Level.WARNING, null, tagException);
        }
    }
}
