package it.polimi.ingsw.network.socket.server;

import it.polimi.ingsw.GameController;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.network.socket.client.VirtualView;
import it.polimi.ingsw.network.socket.messages.GenericMessage;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

public class SocketClientHandler implements Runnable, VirtualView {
    private final Socket socket;
    private final GameController controller;
    private final SocketServer server;
    private final ObjectInputStream  in;
    private final ObjectOutputStream out;
    private final BlockingQueue<String> updates = new LinkedBlockingQueue<>();

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
                    String msg = updates.take();
                    synchronized (out) { 
                        out.writeObject(msg); 
                        out.flush(); 
                    }
                }
            } catch (InterruptedException | IOException ex) { 
                close(); 
            }
        }, "dispatcher-" + socket.getPort());
        t.setDaemon(true); 
        t.start();
    }


    public void sendUpdate(String m) { 
        updates.offer(m); 
    }

    @Override 
    public void showUpdate(String m) { 
        sendUpdate(m); 
    }

    // client -> server
    @Override 
    public void run() {
        try {
            while (true) {
                Object o = in.readObject();
                if (o instanceof GenericMessage message) {
                    message.handle(this);
                } else {
                    System.out.println("Received unknown message type: " + o.getClass().getName());
                }
            }
        } catch (EOFException | java.net.SocketException e) {
            System.out.println("Client disconnected: " + socket.getRemoteSocketAddress());
        } catch (Exception e) { 
            e.printStackTrace(); 
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

    public void sendMessage(Object o) throws IOException {
        synchronized (out) { 
            out.writeObject(o); 
            out.flush(); 
        }
    }

    public void broadcast(String msg) { 
        server.broadcast(msg); 
    }

    private void close() {
        server.removeClient(this);
        try { 
            socket.close(); 
        } catch (IOException ignored) { }
    }

}