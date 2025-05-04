package it.polimi.ingsw.network.socket;

import it.polimi.ingsw.GameController;
import it.polimi.ingsw.network.socket.server.SocketServer;

public class SocketServerLauncher {
    public static void main(String[] a) {
        int port = (a.length>0) ? Integer.parseInt(a[0]) : 7777;
        SocketServer srv = new SocketServer(port, new GameController());
        new Thread(srv, "socket-server").start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { srv.shutdown(); } catch (Exception ignored) { }
        }));
    }
}
