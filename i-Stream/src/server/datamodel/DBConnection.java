package server.datamodel;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import musicinfo.Playlist;
import musicinfo.PlaylistID;
import musicinfo.Song;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

/**
 * FINGER WEG ^^ bin noch dran^^ :-P
 *
 * @author Marc
 * TODO (a) Wie sollen Fehler bearbeitet werden? Einfach an showError() weiterleiten oder selber Fehler definieren?
 * Fehler in Pseudocode der Methode "getPlaylist". Hier wird nur nach den Songs aus der falschen Tabelle gefrgt
 * 
 * XXX (a) also ich würde alle technisch bedingten (db,...) aber keine von den sockets loggen, und natrülich auch ausgeben.
 *
 *
 * RemoveSong() oder RemoveSongFromPlaylist <---- eine unnütz!!!!
 * XXX net wirklich, oder willst du keine songs aus der datenbank löschen?
 *
 *
 * Es gibt hier ne rote linie bei 80 zeichen ungefähr hier --------------------><-
 * >ich hab auch nichts gegen lange zeilen. aber viel darüber hinweg schreiben würde ich nicht.
 *
 */
public class DBConnection {

    private static final Logger logger = Logger.getLogger(DBConnection.class.getName());
    // Database
    private Connection dbConn;
    // Gui Kommunikation
    private Collection<DMListener> listeners;
    // Prepared Statements
    private PreparedStatement addSong;
    private PreparedStatement countFiles;
    private PreparedStatement addSongToPlaylist;
    private PreparedStatement countSongsInPlaylist;
    private PreparedStatement getSongs;
    private PreparedStatement getPlaylistIdById;
    private PreparedStatement getPlaylistIDs;
    private PreparedStatement getPlaylistIdByName;
    private PreparedStatement removeSong;
    private PreparedStatement removeSongFromPlaylist;
    private PreparedStatement createNewPlaylist;
    private PreparedStatement getPlaylist;
    private PreparedStatement getSongInPlaylists;
    private PreparedStatement updateSong;
    private PreparedStatement removePlaylist;

    /**
     * Konstruktor des Objekts "Datenbankverbindung"
     */
    public DBConnection() {
        listeners = new LinkedList<DMListener>();
        connect();
    }

    /**
     * Funktion zum Erstellen der Datenbankrelationen, falls diese noch nicht existieren
     */
    private void createSchema() throws SQLException {
        Statement statement = dbConn.createStatement();
        try {
            // XXX bei neu eingerichtetem postgre mag er das nicht (tables dropen dies noch gar nicht gibt)
            // yup. drum steht es ja auch in nem try und catch block. wenns schief geht
            // wird das löschen übersprungen und die Tabellen angelegt
            statement.executeUpdate("DROP TABLE playlistcontains;");
            statement.executeUpdate("DROP TABLE playlists;");
            statement.executeUpdate("DROP TABLE songs;");
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        statement.executeUpdate("CREATE TABLE playlists (" +
                "id       SERIAL        NOT NULL PRIMARY KEY," +
                "name     TEXT          NOT NULL UNIQUE," +
                "duration DECIMAL(16)   NOT NULL);");
        statement.executeUpdate("CREATE TABLE songs (" +
                "id       SERIAL        NOT NULL PRIMARY KEY," +
                "filepath TEXT          NOT NULL UNIQUE," +
                "duration DECIMAL(14)   NOT NULL," +
                "title    TEXT          NOT NULL," +
                "artist   TEXT            ," +
                "album    TEXT            ," +
                "year     TEXT            ," +
                "genre    TEXT             );");
        // TODO last modification???
        statement.executeUpdate("CREATE TABLE playlistcontains (" +
                "id       INTEGER       NOT NULL," +
                "pid      INTEGER       NOT NULL REFERENCES playlists," +
                "sid      INTEGER       NOT NULL REFERENCES songs," +
                "PRIMARY KEY (id, pid));"); // XXX primär schlüssel verändert (id, pid) jeder track nur einmal vergeben
    }

    /**
     * Funktion zum Beenden der DataModel Komponente
     */
    public void terminate() {
        try {
            dbConn.close();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * In dieser Funktion werden vordefinierte SQL Anfragen generiert, in die
     * später nur noch Werte eingesetzt werden müssen
     * 
     * @throws java.sql.SQLException wird bei einem SQL Fehler geworfen
     */
    private void prepareStatements() throws SQLException {
        addSong = dbConn.prepareStatement(
                "INSERT INTO songs(filepath, duration, title, artist, album, year, genre) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?);");
        countFiles = dbConn.prepareStatement(
                "SELECT COUNT(*) AS count " +
                "FROM songs " +
                "WHERE filepath = ?");
        addSongToPlaylist = dbConn.prepareStatement(
                "INSERT INTO playlistcontains(id, pid, sid) " +
                "VALUES (?, ?, ?);" +
                "UPDATE playlists " +
                "  SET duration = duration + ? " +
                "WHERE id = ?;");
        countSongsInPlaylist = dbConn.prepareStatement(
                "SELECT COUNT(*) AS count " +
                "FROM playlistcontains " +
                "WHERE pid = ?");
        createNewPlaylist = dbConn.prepareStatement(
                "INSERT INTO playlists(name, duration) " +
                "VALUES(?, 0);");
        getPlaylistIdByName = dbConn.prepareStatement(
                "SELECT id " +
                "FROM playlists " +
                "WHERE name = ?;");
        getPlaylistIdById = dbConn.prepareStatement(
                "SELECT * " +
                "FROM playlists " +
                "WHERE id = ?;");
        getSongs = dbConn.prepareStatement(
                "SELECT * " +
                "FROM songs;");
        getPlaylistIDs = dbConn.prepareStatement(
                "SELECT * " +
                "FROM playlists " +
                "ORDER BY name;");
        removeSong = dbConn.prepareStatement(
                "DELETE FROM songs WHERE id = ?;");
        removeSongFromPlaylist = dbConn.prepareStatement(
                "UPDATE playlists " +
                "  SET duration = duration - (SELECT duration " +
                "                             FROM songs AS s, playlistcontains AS c " +
                "                             WHERE s.id = c.sid " +
                "                               AND c.id = ?" +
                "                               AND c.pid = ?) " +
                "WHERE id = ?;" +
                "DELETE FROM playlistcontains " +
                "WHERE pid = ? " +
                "  AND id = ?;" +
                "UPDATE playlistcontains " +
                "  SET id = id - 1 " +
                "WHERE pid = ? " +
                "  AND id > ?;");
        getPlaylist = dbConn.prepareStatement(
                "SELECT c.id AS track, " +
                "       s.id AS id, " +
                "       filepath, " +
                "       duration, " +
                "       title, " +
                "       artist, " +
                "       album, " +
                "       year, " +
                "       genre " +
                "FROM songs AS s, playlistcontains AS c " +
                "WHERE s.id = c.sid " +
                "  AND c.pid = ?" +
                "ORDER BY track, filepath;");
        // TODO einheitliche reihenfolge???
        getSongInPlaylists = dbConn.prepareStatement(
                "SELECT * " +
                "FROM playlistcontains " +
                "WHERE sid = ? " +
                "ORDER BY id;");
        updateSong = dbConn.prepareStatement(
                "UPDATE songs SET " +
                "duration = ?, title = ?, artist = ?, album = ?, year = ?, genre = ? " +
                "WHERE id =?");
        removePlaylist = dbConn.prepareStatement(
                "DELETE FROM playlistcontains WHERE pid = ?; " +
                "DELETE FROM playlists WHERE id = ?;");
    }

    /**
     * Das PreparedStatement wird hier ausgestattet mit:
     * 
     * @param canonicalPath dem Dateipfad
     * @param duration der Dauer des Songs
     * @param title dem Titel des Songs
     * @param artist dem Interpreten des Songs
     * @param year dem Erscheinungsjahr des Songs
     * @param genre dem Genre des Songs.
     */
    public void addSong(String path, long duration, String title, String artist, String album, String year, String genre) {
        try {
            countFiles.setString(1, path);
            ResultSet rset = countFiles.executeQuery();
            rset.next();
            if (rset.getInt(1) == 0) {
                addSong.setString(1, path);
                addSong.setLong(2, duration);
                addSong.setString(3, title);
                addSong.setString(4, artist);
                addSong.setString(5, album);
                addSong.setString(6, year);
                addSong.setString(7, genre);
                addSong.executeUpdate();
                // TODO time limit or single update????
                for (DMListener l : listeners) {
                    l.updateSongs();
                }
            }
        } catch (SQLException sqle) {
            logger.log(Level.SEVERE, null, sqle);
        }
    }

    /**
     * updateSong() ändert die in der Datenbank vorhandene Songs und aktualisiert die
     * ID3 Tags.
     * @param song
     */
    public void updateSong(Song song) {
        try {
            updateSong.setLong(1, song.getDuration());
            updateSong.setString(2, song.getTitle());
            updateSong.setString(3, song.getArtist());
            updateSong.setString(4, song.getAlbum());
            updateSong.setString(5, song.getYear());
            updateSong.setString(6, song.getGenre());
            updateSong.setInt(7, song.getId());
            updateSong.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, null, e);
        }
    }

    /**
     * Ein Song wird in der Datenbank zu einer Playlist hinzugefügt.
     *
     * @param playlistID    Die ID der zu erweiternden Playlist.
     * @param song          Der einzufügende Song.
     */
    public void addSongToPlaylist(PlaylistID playlistID, Song song) {
        try {
            // Bisherige Anzahl an Songs in der Playlist ist die Tracknummer
            countSongsInPlaylist.setInt(1, playlistID.getId());
            ResultSet rset = countSongsInPlaylist.executeQuery();
            rset.next();
            int trackid = rset.getInt(1);
            // Song hinzufügen
            addSongToPlaylist.setInt(1, trackid);
            addSongToPlaylist.setInt(2, playlistID.getId());
            addSongToPlaylist.setInt(3, song.getId());
            addSongToPlaylist.setLong(4, song.getDuration());
            addSongToPlaylist.setInt(5, playlistID.getId());
            addSongToPlaylist.execute();
            // Und Update
            for (DMListener l : listeners) {
                l.updatePlaylists();
                l.updatePlaylist(playlistID);
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Zur Datenbank wird eine Verbindung hergestellt
     */
    private void connect() {
        try {
            // Load driver
            Class.forName("org.postgresql.Driver");
            // TODO password or other method
            dbConn = DriverManager.getConnection("jdbc:postgresql://localhost/postgres", "postgres", "12345REPLACETHIS");
        } catch (ClassNotFoundException classNotFoundException) {
            for (DMListener l : listeners) {
                l.showError("Database driver not found!");
            }
            logger.log(Level.SEVERE, null, classNotFoundException);
        } catch (SQLException sQLException) {
            for (DMListener l : listeners) {
                l.showError("Connection to database refused.");
            }
            logger.log(Level.SEVERE, null, sQLException);
        }
        try {
            prepareStatements();
        } catch (SQLException ex) {
            for (DMListener l : listeners) {
                l.showError("Database structure invalid.\n" +
                        "Trying to restore.");
            }
            try {
                createSchema();
            } catch (SQLException ex1) {
                logger.log(Level.SEVERE, null, ex1);
            }
        }
    }

    /**
     * Eine neuer Eintrag für eine Playlist wird in der Datenbank erstellt. Dabei wird
     * zum Erstellen einer neuen PlaylistID eine Anfrage gestellt, die uns ID und Name der
     * Playlist zurückliefert.
     * 
     * @param playlistname Gewünschter Name für die neue Playlist
     * @return  ein neu erzeugtes Objekt vom Typ Playlist wird zurückgegeben.
     *
     */    // XXX übergabe von playlistid auf playlist geändert, sieht jemand ein problem damit?
    // XXX wenn du einen sinn dahinter siehst, nein
    public Playlist createNewPlaylist(final String playlistname) {
        try {
            // Check if already exists
            getPlaylistIdByName.setString(1, playlistname);
            ResultSet rset = getPlaylistIdByName.executeQuery();
            if (rset.next()) {
                // If exists, return the existing XXX or null and error message?
//                return getPlaylist(new PlaylistID(rset.getInt(1), playlistname));
                for (DMListener l : listeners) {
                    l.showError("Playlist exists!");
                }
                return null;
            } else {
                // If not, create a new one
                createNewPlaylist.setString(1, playlistname);
                createNewPlaylist.executeUpdate();
                // Notfiy gui
                for (DMListener l : listeners) {
                    l.updatePlaylists();
                }
                // And return a clean playlist
                rset = getPlaylistIdByName.executeQuery();
                rset.next();
                PlaylistID plid = new PlaylistID(rset.getInt(1), playlistname, 0);
                Playlist pl = new Playlist(plid);
                return pl;
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Die an DBConnection delegierte Methode liest die entsprechende Playlist
     * samt Songs aus. Hierzu wird ein neues Objekt des Typs PlayList erstellt.
     * Dann wird eine Anfrage an die Datenbank gesendet, welche Objekte sich in der
     * Playliste befinden (sollen). Dann werden diese Songs als Instanzen des Typs Songs erzeugt
     * und der Playlist hinzugefügt
     *
     *
     * Durch diesen Ansatz erreichen
     * wir dynamische Speicherobjekte. Das heißt wir erzeugen erst Objekte,
     * wenn wir sie benötigen.
     * 
     * @param playlistID    PlaylistID der gewünschten Playliste
     * @return              Gibt die entsprechende PlayList aus
     */
    public Playlist getPlaylist(PlaylistID playlistID) {
        try {
            getPlaylist.setInt(1, playlistID.getId());
            ResultSet rset = getPlaylist.executeQuery();
            Playlist playlist = new Playlist(playlistID);
            Song s;
            while (rset.next()) {
                s = new Song(
                        rset.getInt("id"),
                        new File(rset.getString("filepath")));
                s.setDuration(rset.getLong("duration"));
                s.setTitle(rset.getString("title"));
                s.setArtist(rset.getString("artist"));
                s.setAlbum("album");
                s.setYear(rset.getString("year"));
                s.setGenre(rset.getString("genre"));
                playlist.addSong(rset.getInt("track"), s);
            }
            return playlist;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private PlaylistID getPlaylistID(int id) throws SQLException {
        // SELECT *
        // FROM playlists
        // WHERE id = ?;
        getPlaylistIdById.setInt(1, id);
        ResultSet rset = getPlaylistIdById.executeQuery();
        if (rset.next()) {
            return new PlaylistID(id, rset.getString("name"), rset.getLong("duration"));
        }
        return null;
    }

    /**
     * Liefert alle Playlistidentifier in der Datenbank.
     * @return die Collection mit <code>PlaylistID</code>s
     */
    public Collection<PlaylistID> getPlaylistIDs() {
        try {
            ArrayList ret = new ArrayList<PlaylistID>();
            PlaylistID plid;
            ResultSet rset = getPlaylistIDs.executeQuery();
            // Create PlaylistIDs
            while (rset.next()) {
                plid = new PlaylistID(
                        rset.getInt("id"),
                        rset.getString("name"),
                        rset.getLong("duration"));
                ret.add(plid);
            }
            return ret;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
            return null;
        }

    }

    /**
     * Liefert alle Songs in der Datenbank.
     * @return die Collection mit <code>Song</code>s
     */
    public Collection<Song> getSongs() {
        try {
            ResultSet rset = getSongs.executeQuery();
            return resultsToSongs(rset);
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * In dieser Methode wird der Song aus der Datenbank gelöscht. Hierzu wird der Eintrag
     * aus der Musikdatei Relation gelöscht und dann aus der PlaylistContains, so dass keine
     * Playlist den Song als Eintrag mehr hat.
     * 
     * @param song Der zu löschende Song
     */
    public void removeSong(Song song) {
        try {
            // SELECT *
            // FROM playlistcontains
            // WHERE sid = ?
            // ORDER BY id;
            getSongInPlaylists.setInt(1, song.getId());
            ResultSet rset = getSongInPlaylists.executeQuery();
            int pid;
            while (rset.next()) {
                pid = rset.getInt("pid");
                removeSongFromPlaylist(pid, rset.getInt("id"));
                for (DMListener l : listeners) {
                    l.updatePlaylist(getPlaylistID(pid));
                }
                // TODO id hat sich geändert TODO besserer weg
                rset = getSongInPlaylists.executeQuery();
            }
            // DELETE FROM songs WHERE id = ?;
            removeSong.setInt(1, song.getId());
            removeSong.executeUpdate();
            for (DMListener l : listeners) {
                l.updatePlaylists();
                l.updateSongs();
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 
     * @param playlistID    Identifier der zu ändernden Playlist
     * @param song          Zu löschender Song
     * @param track         Position in der Playlist
     */
    public void removeSongFromPlaylist(PlaylistID playlistID, int track) {
        try {
            removeSongFromPlaylist(playlistID.getId(), track);
            for (DMListener l : listeners) {
                l.updatePlaylists();
                l.updatePlaylist(playlistID);
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private void removeSongFromPlaylist(int pid, int track) throws SQLException {
        // UPDATE playlists
        //   SET duration = duration - (SELECT duration
        //                              FROM songs AS s, playlistcontains AS c
        //                              WHERE s.id = c.sid
        //                                AND c.id = ?
        //                                AND c.pid = ?)
        // WHERE id = ?;
        removeSongFromPlaylist.setInt(1, track);
        removeSongFromPlaylist.setInt(2, pid);
        removeSongFromPlaylist.setInt(3, pid);
        // DELETE FROM playlistcontains
        // WHERE pid = ?
        //   AND id = ?;
        removeSongFromPlaylist.setInt(4, pid);
        removeSongFromPlaylist.setInt(5, track);
        // UPDATE playlistcontains
        //   SET id = id - 1
        // WHERE pid = ?
        //   AND id > ?;
        removeSongFromPlaylist.setInt(6, pid);
        removeSongFromPlaylist.setInt(7, track);
        removeSongFromPlaylist.executeUpdate();
    }

    /**
     * Habe hier private in protected geändert (clyde)
     * @param searchTerm
     * @return
     */
    public Collection<Song> searchSong(String searchTerm) {
        try {
            searchTerm = searchTerm.trim().toLowerCase();
            String[] terms = searchTerm.split(" ");
            if (terms.length == 0) {
                System.out.println("db search song all");
                return getSongs();
            }

            System.out.println("db search song " + searchTerm);
            StringBuilder sb = new StringBuilder("SELECT * FROM songs " +
                    "WHERE LOWER(title)  LIKE '%" + terms[0] + "%' " +
                    "   OR LOWER(artist) LIKE '%" + terms[0] + "%' " +
                    "   OR LOWER(genre)  LIKE '%" + terms[0] + "%' " +
                    "   OR LOWER(year)   LIKE '%" + terms[0] + "%' ");
            String nextQry = "";
            for (int i = 1; i < terms.length; i++) {
                nextQry = "SELECT * FROM songs " +
                        "WHERE LOWER(title)  LIKE '%" + terms[i] + "%' " +
                        "   OR LOWER(artist) LIKE '%" + terms[i] + "%' " +
                        "   OR LOWER(genre)  LIKE '%" + terms[i] + "%' " +
                        "   OR LOWER(year)   LIKE '%" + terms[i] + "%' ";
                sb.append(" INTERSECT " + nextQry);
            }
            sb.append(";");
            Statement stmnt = dbConn.createStatement();
            ResultSet rset = stmnt.executeQuery(sb.toString());
            return resultsToSongs(rset);
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Habe hier private in protected geändert (clyde)
     * @param playlistID
     * @param string
     * @return
     */
    public Map<Integer, Song> searchSongInPlaylist(PlaylistID playlistID, String searchTerm) {
        try {
            searchTerm = searchTerm.trim().toLowerCase();
            String[] terms = searchTerm.split(" ");
            if (terms.length == 0) {
                System.out.println("db search songinplaylist all");
                return getPlaylist(playlistID).getSongTabel();
            }

            System.out.println("db search songinplaylist " + searchTerm);
            StringBuilder sb = new StringBuilder("SELECT DISTINCT c.id AS track, s.* " +
                    "FROM songs AS s, playlistcontains AS c " +
                    "WHERE s.id = c.sid " +
                    "  AND c.pid = " + playlistID.getId() + " ");
            for (int i = 0; i < terms.length; i++) {
                sb.append("AND ( " +
                        "      LOWER(title)  LIKE '%" + terms[i] + "%' " +
                        "   OR LOWER(artist) LIKE '%" + terms[i] + "%' " +
                        "   OR LOWER(genre)  LIKE '%" + terms[i] + "%' " +
                        "   OR LOWER(year)   LIKE '%" + terms[i] + "%' " +
                        "      ) ");
            }
            sb.append(";");
            Statement stmnt = dbConn.createStatement();
            ResultSet rset = stmnt.executeQuery(sb.toString());
            Song s;
            TreeMap ret = new TreeMap<Integer, Song>();
            while (rset.next()) {
                s = new Song(
                        rset.getInt("id"),
                        new File(rset.getString("filepath")));
                s.setDuration(rset.getLong("duration"));
                s.setTitle(rset.getString("title"));
                s.setArtist(rset.getString("artist"));
                s.setAlbum("album");
                s.setYear(rset.getString("year"));
                ret.put(rset.getInt("track"), s);
            }
            return ret;
        } catch (SQLException sqle) {
            logger.log(Level.SEVERE, null, sqle);
            return null;
        }
    }

    // TODO resultsToSongCollection() and resultsToTrackSongMap()
    private Collection<Song> resultsToSongs(ResultSet rset) throws SQLException {
        Song s;
        ArrayList ret = new ArrayList<Song>();
        while (rset.next()) {
            s = new Song(
                    rset.getInt("id"),
                    new File(rset.getString("filepath")));
            s.setDuration(rset.getLong("duration"));
            s.setTitle(rset.getString("title"));
            s.setArtist(rset.getString("artist"));
            s.setAlbum("album");
            s.setYear(rset.getString("year"));
            ret.add(s);
        }
        return ret;
    }

    public void addListener(DMListener listener) {
        listeners.add(listener);
    }

    public void removePlaylist(PlaylistID playlistid) {
        try {
            // DELETE FROM playlistcontains WHERE pid = ?;
            // DELETE FROM playlists WHERE id = ?;
            removePlaylist.setInt(1, playlistid.getId());
            removePlaylist.setInt(2, playlistid.getId());
            removePlaylist.executeUpdate();
            for (DMListener l : listeners) {
                l.updatePlaylist(null); // TODO nicht mit einer null lösen???
                l.updatePlaylists();
            }
        } catch (SQLException sqle) {
            logger.log(Level.SEVERE, null, sqle);
        }
    }

    public static void main(String[] args) throws SQLException {
        DBConnection db = new DBConnection();
        db.createSchema();
//        db.addSong("test", 1, "test", "test", "test", "test", "genretest");
//        Playlist eins = db.createNewPlaylist("Playlist 1");
//        Playlist zwei = db.createNewPlaylist("Playlist 2");
//        Playlist drei = db.createNewPlaylist("Playlist 3");
//        Playlist vier = db.createNewPlaylist("Playlist 4");
    }
}
