package it.polimi.ingsw.network.socket.server;

import it.polimi.ingsw.GameController;
import it.polimi.ingsw.network.socket.client.VirtualView;
import it.polimi.ingsw.network.socket.messages.GenericMessage;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SocketClientHandler implements Runnable, VirtualView {
    private final Socket socket;
    private final GameController controller;
    private final SocketServer server;
    private final ObjectInputStream  in;
    private final ObjectOutputStream out;
    private final BlockingQueue<String> updates = new LinkedBlockingQueue<>();

    public SocketClientHandler(Socket s, GameController c, SocketServer srv) throws IOException {
        socket = s; controller = c; server = srv;
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
                    synchronized (out) { out.writeObject(msg); out.flush(); }
                }
            } catch (InterruptedException | IOException ex) { close(); }
        }, "dispatcher-"+socket.getPort());
        t.setDaemon(true); t.start();
    }
    public void sendUpdate(String m) { updates.offer(m); }
    @Override public void showUpdate(String m) { sendUpdate(m); }

    // client -> server
    @Override public void run() {
        try {
            while (true) {
                Object o = in.readObject();
                System.out.println("new obj recieved" + o);
                ((GenericMessage) o).handle(this);
            }
        } catch (EOFException | java.net.SocketException e) {
            System.out.println("Client disconnected" + socket.getRemoteSocketAddress());
        } catch (Exception e) { e.printStackTrace(); }
        finally { 
            close(); 
        }
    }

    //something

    

    private void close() {
        server.removeClient(this);
        try { socket.close(); } catch (IOException ignored) { }
    }

}