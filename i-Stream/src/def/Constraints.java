/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package def;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileFilter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.JTextComponent.AccessibleJTextComponent;

/**
 *
 * @author Lars
 */
public class Constraints {

    public static final String RMI_CLIENT_NAME = "client";
    public static final String RMI_SERVER_NAME = "server";
//    public static final int RMI_PORT = 15477;
    public static final int TCP_PORT = 51234;
    public static final int STREAM_BUFER = 4096;
    public static InetAddress localad;
    public static final Color GUI_YELLOW = new Color(202, 209, 104);
    public static final Color GUI_RED = new Color(221, 110, 110);
    public static final Color GUI_GREEN = new Color(105, 181, 117);
    public static final Color GUI_YELLOW_SELECT = new Color(176, 183, 93);
    public static final Color GUI_RED_SELECT = new Color(180, 90, 90);
    public static final Color GUI_GREEN_SELECT = new Color(75, 127, 83);
    public static final Color GUI_BLUE = new Color(140, 127, 255);
    public static final FileFilter MP3_IO_FILEFILTER = new FileFilter() {

        public boolean accept(File pathname) {
            return pathname.isDirectory() || pathname.getName().endsWith(".mp3");
        }
    };
    public static final javax.swing.filechooser.FileFilter MP3_SWING_FILEFILTER = new javax.swing.filechooser.FileFilter() {

        @Override
        public boolean accept(File f) {
            return MP3_IO_FILEFILTER.accept(f);
        }

        @Override
        public String getDescription() {
            return "Mp3 File";
        }
    };
    public static final Border BORDER = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
    public static final Color GUI_JTF_UNFOCUSED = new Color(80, 80, 80);
    private static final String iconPath = "res" + File.separatorChar;
    private static final HashMap<String, ImageIcon> icons = new HashMap<String, ImageIcon>();

    public static final ImageIcon getResourceIcon(String path) {
        String key = path.trim().replaceFirst("\\..*$", "");
        if (!icons.containsKey(key)) {
            URL url = ClassLoader.getSystemResource(iconPath + path);
            if (url != null) {
//                System.out.println("load: " + url);
                icons.put(key, new ImageIcon(url));
            } else {
//                System.out.println("resource not found: " + path);
            }
        }
        return icons.get(key);
    }

    public static InetAddress getLocalHost() {
        try {
            Enumeration<NetworkInterface> netInter = NetworkInterface.getNetworkInterfaces();
            int n = 0;
            while (netInter.hasMoreElements()) {
                NetworkInterface ni = netInter.nextElement();
                System.out.println("NetworkInterface " + n++ + ": " + ni.getDisplayName());
                
                for (InetAddress iaddress : Collections.list(ni.getInetAddresses())) {
                    System.out.println("IP: " + iaddress.getHostAddress());
                    if (iaddress.getHostAddress().equals("134.34.16.229")){
                        System.out.println(iaddress.isLinkLocalAddress());
                        System.out.println(iaddress.isLoopbackAddress());
                        System.out.println(iaddress.isMulticastAddress());
                        System.out.println(iaddress.isSiteLocalAddress());
                        System.out.println(iaddress.isMCGlobal());
                        System.out.println(iaddress.isMCNodeLocal());
                        
                    }
                    System.out.println("Loopback? " + iaddress.isLoopbackAddress());
//                    System.out.println();
                    System.out.println(iaddress);
                    if (iaddress instanceof Inet4Address && !iaddress.isLoopbackAddress()) {
                        
//                        return iaddress;
                    }
                }

            }
        } catch (SocketException ex) {
            Logger.getLogger(Constraints.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
//    public static void setIcon(String path, String name){
//            URL url = ClassLoader.getSystemResource(iconPath + path);
//            if (url != null) {
//                System.out.println("load: " + url);
//                icons.put(path.replace(".png", ""),new ImageIcon(url));
//                icons.put(path, new ImageIcon(url));
//            } else {
//                System.out.println("resource not found: " + url);
//            }
//    }
//
//    public static final ImageIcon getIcon(String name){
//        return icons.get(name);
//    }
//    public static void setIcon(String path, String name){
//            URL url = ClassLoader.getSystemResource(iconPath + path);
//            if (url != null) {
//                System.out.println("load: " + url);
//                icons.put(path.replace(".png", ""),new ImageIcon(url));
//                icons.put(path, new ImageIcon(url));
//            } else {
//                System.out.println("resource not found: " + url);
//            }
//    }
//
//    public static final ImageIcon getIcon(String name){
//        return icons.get(name);
//    }
}
