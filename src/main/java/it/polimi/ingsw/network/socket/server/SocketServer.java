package it.polimi.ingsw.network.socket.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import it.polimi.ingsw.GameController;

public class SocketServer implements Runnable {
    
    private final int port;
    private final GameController controller;
    private final List<SocketClientHandler> clients = new CopyOnWriteArrayList<>();
    private ServerSocket ss;

    public SocketServer(int port, GameController ctrl) { this.port = port; this.controller = ctrl; }

    public SocketServer(int port) { this.port = port; controller = null; }

    @Override public void run() {
        try (ServerSocket srv = new ServerSocket(port)) {
            ss = srv;
            System.out.println("Socket-Server listening at port "+ port);
            while (!srv.isClosed()) {
                Socket s = srv.accept();
                SocketClientHandler h = new SocketClientHandler(s, controller, this);
                clients.add(h);
                new Thread(h, "client-"+s.getPort()).start();
                h.showUpdate("Welcome! Connection established.");
            }
        } catch (IOException e) { System.out.println("Server shutdown: "+e.getMessage()); }
    }

    void broadcast(String m) { 
        clients.forEach(c -> c.sendUpdate(m)); 
    }

    void removeClient(SocketClientHandler c) { 
        clients.remove(c); 
    }

    public void shutdown() throws IOException { 
        ss.close(); 
    }

}