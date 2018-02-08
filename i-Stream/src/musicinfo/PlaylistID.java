package musicinfo;

import java.io.Serializable;

/**
 *
 * @author Marc und Boney
 * @version 0.1 angelegt und bearbeitet
 */
public class PlaylistID implements Serializable {

    private final int id;
    private final String name;
    private long duration;

    public PlaylistID(int aInt, String string, long duration) {
        this.id = aInt;
        this.name = string;
        this.duration = duration;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getDuration() {
        return this.duration;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PlaylistID) {
            return id == ((PlaylistID) obj).id;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + this.id;
        return hash;
    }

    @Override
    public String toString() {
        return name;
    }
}
