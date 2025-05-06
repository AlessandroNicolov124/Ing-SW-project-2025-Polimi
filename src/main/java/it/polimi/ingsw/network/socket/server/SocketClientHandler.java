package it.polimi.ingsw.network.socket.server;

import it.polimi.ingsw.GameController;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.network.socket.client.VirtualView;
import it.polimi.ingsw.network.socket.messages.GenericMessage;
import it.polimi.ingsw.network.socket.messages.TextUpdateMessage;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadLocalRandom;

public class SocketClientHandler implements Runnable, VirtualView {
    private final Socket socket;
    private final GameController controller;
    private final SocketServer server;
    private final ObjectInputStream  in;
    private final ObjectOutputStream out;
    private final BlockingDeque<GenericMessage> updates = new LinkedBlockingDeque<>();

    public SocketClientHandler(Socket s, GameController c, SocketServer srv) throws IOException {
        socket = s; 
        controller = c; 
        server = srv;
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in  = new ObjectInputStream (socket.getInputStream());
        startDispatcher();
    }

    // server -> client
    private void startDispatcher() {
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    GenericMessage msg = updates.take();
                    synchronized (out) {
                        out.writeObject(msg);
                        out.flush();
                    }
                }
            } catch (IOException | InterruptedException ignored) {
            } finally {
                close();
            }
        }, "dispatcher-" + socket.getInetAddress());
        t.setDaemon(true);
        t.start();
    }


    public void sendUpdate(GenericMessage m) { 
        updates.offer(m); 
    }

    @Override
    public void showUpdate(String m) {
        sendUpdate(new TextUpdateMessage(m));
    }

    // client -> server
    @Override
    public void run() {
        try {
            while (!socket.isClosed()) {
                Object obj = in.readObject();
                if (obj instanceof GenericMessage g) {
                    g.handleServer(this); 
                }
            }
        } catch (Exception ignored) {
        } finally {
            close();
        }
    }

    public int createPlayer(String username) {
        int min = 1;
        int max = 1000;
        int newId = ThreadLocalRandom.current().nextInt(min, max + 1);
        Player p = new Player(newId, username, true);
        controller.addPlayer(p);
        return newId;
    }

    public void broadcast(GenericMessage msg) { 
        server.broadcast(msg); 
    }

    private void close() {
        try {
            socket.close();
        } catch (IOException ignored) {
            
        }
        server.removeClient(this);
    }

}