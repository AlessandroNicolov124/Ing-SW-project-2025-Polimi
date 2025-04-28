package it.polimi.ingsw.network.rmi;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RmiServerLauncher {
    public static void main(String[] args) throws Exception {
        System.setProperty("java.rmi.server.hostname", "127.0.0.1");
        Registry reg = LocateRegistry.createRegistry(1099);
        Controller ctrl = new Controller();
        RmiServer server = new RmiServer(ctrl, reg);
        Naming.rebind("GalaxyTruckerServer", server);
        System.out.println("GalaxyTruckerServer ready (port 1099)…");

        // Gracefully shutdown the server on exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { 
                server.close(); 
            } catch (Exception ignored) {
                System.out.println("Error while closing server: " + ignored.getMessage());
            }
        }));
    }
}