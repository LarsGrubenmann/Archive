/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.clientadministration;

import client.controller.IClient;
import java.io.IOException;
import java.net.InetAddress;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author manuel, fatih, peter
 * @version 0.1 angelegt und bearbeitet
 */
public class ClientInfo implements Serializable {

    public final InetAddress address;
    transient protected final IClient facadeRef;
    public final int id;
    public final String name;
    private int providerID;

    public ClientInfo(InetAddress address, String name, int id, IClient facadeRef) {
        this.address = address;
        this.facadeRef = facadeRef;
        this.id = id;
        this.name = name;
    }

    public int getProviderID() {
        return providerID;
    }

    public void setProviderID(int prov) {
        this.providerID = prov;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object c) {
        if (c instanceof ClientInfo) {
            return id == ((ClientInfo) c).id;
        }
        return super.equals(c);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + this.id;
        return hash;
    }
}
