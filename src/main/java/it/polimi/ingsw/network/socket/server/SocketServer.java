package it.polimi.ingsw.network.socket.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import it.polimi.ingsw.network.LobbyManager;

public class SocketServer implements Runnable {
    
    private final int port;
    private final LobbyManager lobbyManager;
    private final List<SocketClientHandler> clients = new CopyOnWriteArrayList<>();
    private ServerSocket ss;

    public SocketServer(int port, LobbyManager lobbyManager) { 
        this.port = port; 
        this.lobbyManager = lobbyManager; 
    }

    @Override
        public void run() {
            try (ServerSocket srv = new ServerSocket(port)) {
                ss = srv;
                System.out.println("Socket-Server listening at port " + port);
                while (!srv.isClosed()) {
                    Socket s = srv.accept();
                    SocketClientHandler h = new SocketClientHandler(s, lobbyManager, this);
                    clients.add(h);
                    new Thread(h, "client-" + s.getInetAddress()).start();
                }
            } catch (IOException e) {
                System.err.println("Server closed: " + e.getMessage());
            }
        }

    void removeClient(SocketClientHandler c) { 
        clients.remove(c); 
    }

    public void shutdown() throws IOException { 
        ss.close(); 
    }

}