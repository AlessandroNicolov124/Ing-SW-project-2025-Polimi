package it.polimi.ingsw.network.socket.client;

import java.io.IOException;
import java.io.ObjectOutputStream;

import it.polimi.ingsw.network.socket.messages.GenericMessage;
import it.polimi.ingsw.network.socket.messages.XMessage;
import it.polimi.ingsw.network.socket.messages.YMessage;
import it.polimi.ingsw.network.socket.server.VirtualServer;


public class VirtualSocketServer implements VirtualServer {

    private final ObjectOutputStream out;

    public VirtualSocketServer(ObjectOutputStream out) {
        this.out = out;
    }

    private void send(GenericMessage m) throws IOException {
        synchronized (out) {
            out.writeObject(m);
            out.flush();
        }
    }

    @Override
    public void connect(VirtualView client) {

    }

    @Override
    public void x() throws IOException {
        send(new XMessage());
    }

    @Override
    public void y() throws IOException {
        send(new YMessage());
    }

    @Override
    public void stopServer() throws IOException {
        out.close();
    }

}