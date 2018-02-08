package client.musicdecoder;

import musicinfo.Song;

/**
 *
 * @author Lars, Marc und Boney
 * @version 0.1
 */
public interface IReceiver {

    public void pauseStream();

    public void startStream(Song song);

    public void stopStream();

    public void setGain(float value);

    public void terminate();
}
