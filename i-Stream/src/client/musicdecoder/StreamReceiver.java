package client.musicdecoder;

import client.controller.Controller;
import def.Constraints;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.SocketTimeoutException;
import java.util.concurrent.Semaphore;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import musicinfo.Song;

/**
 *
 * @author Lars, Felix, Boney
 * @version 0.2
 */
public class StreamReceiver extends Thread implements IReceiver {

    /**
     * Thread status
     */
    private boolean isAlive;
    private boolean isPlaying;
    private boolean noreinit;
    private Semaphore loopInitTurnstile;
    private Semaphore loopExitTurnstile;
    /**
     * Audio
     */
    private long totalMilliSeconds;
    private Socket sock;
    private InputStream sin;
    private AudioInputStream ain;
    private SourceDataLine line;
    private FloatControl gain;
    /**
     * 
     */
    private Controller controller;

    public StreamReceiver(Controller con) {
        isAlive = true;
        controller = con;
        loopInitTurnstile = new Semaphore(0);
        loopExitTurnstile = new Semaphore(0);
        start();
    }

    public synchronized void terminate() {
        isAlive = false;
        stopStream();
        interrupt();
        System.out.println("sr terminated");
    }

    private AudioInputStream formatedStream(InputStream in) throws UnsupportedAudioFileException, IOException {
        AudioInputStream audioin = AudioSystem.getAudioInputStream(in);
        AudioFormat baseFormat = audioin.getFormat();
        // TODO read doc
        AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getSampleRate(),
                false);
        return AudioSystem.getAudioInputStream(decodedFormat, audioin);
    }

    private SourceDataLine getLine(AudioInputStream din) throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, din.getFormat(), din.getFormat().getFrameSize());
        SourceDataLine res = (SourceDataLine) AudioSystem.getLine(info);
        res.open(din.getFormat());
        return res;
    }

    @Override
    public void run() {
        try {
            // Puffer
            byte[] data = new byte[Constraints.STREAM_BUFER];
            int l;
            // Verbingungsaufbau

            int temp = 0;
            File t = new File("temp");
            if (t.exists()) {
                temp++;
                t.delete();
            } else {
                t.createNewFile();
            }


            ServerSocket srvrsock = new ServerSocket(Constraints.TCP_PORT + temp);
            srvrsock.setSoTimeout(300);

            while (isAlive) {

                try {
                    loopInitTurnstile.acquire();
                    System.out.println("sr init loop");

                    if (!noreinit) {

                        while (isAlive && sock == null) {
                            try {
                                sock = srvrsock.accept();
                                sin = sock.getInputStream();

                                System.out.println("sr get format");
                                ain = formatedStream(sin);
                                line = getLine(ain);
                                // Get volume control
                                if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                                    gain = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
                                }
                            } catch (SocketTimeoutException ste) {
                                // Noooomal
                            }
                        }
                    }
                    noreinit = false;

                    line.start();
                    // TODO sicher abschießen
//                    new Thread() {
//
//                        @Override
//                        public void run() {
//                            while (line.isOpen() && (noreinit || isPlaying)) {
//                                // TODO send to gui
//                                System.out.println(line.getMicrosecondPosition() / (totalMilliSeconds / 100));
//
//                                try {
//                                    Thread.sleep(100);
//                                } catch (InterruptedException ex) {
//                                    // ignore
//                                }
//                            }
//                            System.out.println("stopped");
//                        }
//                    }.start();
                    try {
                        while (isPlaying && (l = ain.read(data)) != -1) {
                            line.write(data, 0, l);
                            controller.listener.updateProgress((int) (line.getMicrosecondPosition() / (totalMilliSeconds / 100)));
                        }
                        if (isPlaying) {
                            controller.songDone();
                        }
                    } catch (IOException iOException) {
                        System.out.println("closed");
                    }

                    // frage: doneCounter implementiert, stelle richtig?
//                    if (!isPaused) {
//                        controller.songDone();
//                    }
                } catch (InterruptedException ie) {
                    System.out.println("sr interrrpt");
                    // terminate
                }
                loopExitTurnstile.release();
                System.out.println("sr exit loop");
            }
            srvrsock.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void setGain(float value) {
        if (gain != null) {
            int b = 5; // XXX modulator auf den volume-regler,
            //     relativ random, darf verstellt werden nach gutdünken ...
            value = (float) ((Math.log((value * (b - 1) / 100) + 1) / Math.log(b)) * 100);

            value = value * 100 / (gain.getMaximum() - gain.getMinimum()) + gain.getMinimum();
            value = value > gain.getMaximum() ? gain.getMaximum() : value;
            value = value < gain.getMinimum() ? gain.getMinimum() : value;
            gain.setValue((float) value);
        }
    }

    /**
     * verändert Status von run(), auf pausiert.
     */
    public synchronized void pauseStream() {
        if (!noreinit) {
            if (isPlaying) {
                System.out.println("sr pause");
                isPlaying = false;
                noreinit = true;
                try {
                    loopExitTurnstile.acquire();
                } catch (InterruptedException ex) {
                    Logger.getLogger(StreamReceiver.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println("sr pause end");
            }
            line.stop();
        } else {
            isPlaying = true;
            loopInitTurnstile.release();
        }
    }

    /**
     * verändert Status von run(), auf abspielend (falls er davor pausiert war.).
     */
    public synchronized void startStream(Song song) {
        //if (!isPlaying) {
        isPlaying = true;
        noreinit = false;
        totalMilliSeconds = song.getDuration();
        sock = null;
        loopInitTurnstile.release();
        //}
    }

    /**
     * verändert Status von run(), auf gestoppt.
     */
    public synchronized void stopStream() {
        if (isPlaying || noreinit) {
            isPlaying = false;
            System.out.println("sr stop stream");
            // interrupts the line.write() call?????
//        line.drain();
            if (!noreinit) {
                try {
                    System.out.println("sr stop stream wait");
                    loopExitTurnstile.acquire();
                } catch (InterruptedException ex) {
                    Logger.getLogger(StreamReceiver.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            noreinit = false;

            line.flush();
            line.drain();
            line.close();

            try {
                int l;
                while ((l = sin.available()) > 0) {
                    sin.skip(l);
                }
//                wait(100);
                ain.close();
                sock.close();
                sock = null;
            } catch (IOException ex) {
                Logger.getLogger(StreamReceiver.class.getName()).log(Level.SEVERE, null, ex);
            }
            //            } catch (InterruptedException e){
//                Logger.getLogger(StreamReceiver.class.getName()).log(Level.SEVERE, null, e);
//            }

            System.out.println("sr stop stream end");
        }
    }
    //    /**
//     * XXX Test Method
//     * @param args
//     */
//    public static void main(String[] args) throws IOException {
//        StreamReceiver sr = new StreamReceiver();
//        sr.start();
//        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//        String s;
//        while (!(s = br.readLine()).isEmpty()) {
//            if (s.equals("play")) {
//                sr.play();
//            } else if (s.equals("stop")) {
//                sr.stopStream();
//            } else if (s.equals("pause")) {
//                sr.pause();
//            }
//        }
//
//    }
}
