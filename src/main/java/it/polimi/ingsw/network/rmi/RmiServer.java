package it.polimi.ingsw.network.rmi;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.Unreferenced;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class RmiServer extends UnicastRemoteObject
                       implements VirtualServer, Unreferenced, AutoCloseable {

    private final Controller controller;
    private final List<VirtualView> clients = new CopyOnWriteArrayList<>();
    private final BlockingQueue<String> updates = new LinkedBlockingQueue<>();
    private final Thread dispatcher;
    private final Registry registry;

    public RmiServer(Controller controller, Registry registry) throws RemoteException {
        super();
        this.controller = controller;
        this.registry   = registry;
        this.dispatcher = startDispatcher();
    }

    // Thread which dispatches updates to all clients
    private Thread startDispatcher() {
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    String msg = updates.take();
                    for (VirtualView c : clients) {
                        try { 
                            c.showUpdate(msg); 
                        }
                        catch (RemoteException e) { 
                            clients.remove(c); 
                        }
                    }
                }
            } catch (InterruptedException ignored) {
                System.out.println("Dispatcher thread ifnterrupted, exiting...");
                }
        }, "Update-Dispatcher");
        t.setDaemon(true);
        t.start();

        return t;
    }

    @Override
    public void connect(VirtualView client) throws RemoteException {
        clients.add(client);
        client.showUpdate("Welcome!");
    }

    @Override
    public void x() throws RemoteException {
        int v = controller.x();
        updates.offer("X executed, incremented value: " + v);
    }

    @Override
    public void y() throws RemoteException {
        controller.y();
        updates.offer("Y executed, value set to 0");
    }

    @Override
    public void stopServer() throws RemoteException {
        new Thread(() -> { 
            try { 
                close(); 
            } catch (Exception ignored) {
                System.out.println("Error while closing server: " + ignored.getMessage());
            }
        }).start();
    }

    @Override
    public void close() throws Exception {
        dispatcher.interrupt();
        UnicastRemoteObject.unexportObject(this, true);
        if (registry != null) UnicastRemoteObject.unexportObject(registry, true);
        System.out.println("Shutting down GalaxyTruckerServer...");
    }

    // Unreferenced method, called when the server is no longer referenced by any client
    // This is a fallback mechanism to ensure the server is closed when all clients disconnect
    @Override public void unreferenced() { 
        System.out.println("Unreferenced: " + this);
        try { 
            close(); 
        } 
        catch (Exception ignored) {
            System.out.println("Error while closing server: " + ignored.getMessage());
        }
    }

}