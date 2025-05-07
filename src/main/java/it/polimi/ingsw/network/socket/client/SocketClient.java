package it.polimi.ingsw.network.socket.client;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import it.polimi.ingsw.network.client.GameClient;
import it.polimi.ingsw.network.socket.messages.ClientMessage;
import it.polimi.ingsw.network.socket.messages.GenericMessage;
import it.polimi.ingsw.network.socket.messages.TextUpdateMessage;


public class SocketClient implements Runnable, VirtualView, GameClient {
    
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream  in;
    private final VirtualSocketServer proxy;
    private final BlockingDeque<GenericMessage> messages = new LinkedBlockingDeque<>();
    private int playerId = -1;

    public SocketClient(String host,int port) throws IOException {
        socket = new Socket(host,port);
        out = new ObjectOutputStream(socket.getOutputStream()); 
        out.flush();
        in  = new ObjectInputStream (socket.getInputStream());
        proxy = new VirtualSocketServer(out, messages);
    }

    @Override
    public void run() {
        try {
            while (!socket.isClosed()) {
                Object obj = in.readObject();
                if (obj instanceof ClientMessage cm) {
                    cm.handleClient(this);
                }
                if (obj instanceof GenericMessage gm) {
                    if (!(gm instanceof TextUpdateMessage)){
                        messages.offerLast(gm);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void showUpdate(String text) {
        System.out.println("[update] " + text);
    }

    @Override public void close() throws Exception { 
        socket.close(); 
    }

    @Override
    public int addPlayer(String username) throws Exception {
        return proxy.addPlayer(username);
    }
    
    public void setPlayerId(int id) {
        this.playerId = id;
    }
    
}