package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.GameController;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.network.client.GeneralClientIf;
import it.polimi.ingsw.network.client.RmiClientIf;
import it.polimi.ingsw.network.rmi.messages.ComponentPlacedMessage;
import it.polimi.ingsw.network.rmi.messages.PickComponentMessage;
import it.polimi.ingsw.network.rmi.messages.PlayerAddedMessage;
import it.polimi.ingsw.network.rmi.messages.UpdateMessage;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.Unreferenced;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

public class RmiServer extends UnicastRemoteObject
        implements VirtualServer, Unreferenced, AutoCloseable, Runnable {

    private final GameController controller;
    private final List<RmiClientIf> clients = new CopyOnWriteArrayList<>();
    private final BlockingQueue<UpdateMessage> updates = new LinkedBlockingQueue<>();
    private final Thread dispatcher;
    private final Registry registry;

    public RmiServer(GameController controller, Registry registry) throws RemoteException {
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
                    UpdateMessage msg = updates.take();
                    for (RmiClientIf c : clients) {
                        try {
                            c.showUpdate(msg);
                        }
                        catch (RemoteException e) {
                            clients.remove(c);
                        }
                        catch (Exception e) {
                        }
                    }
                }
            } catch (InterruptedException ignored) {
                System.out.println("Dispatcher thread if iterrupted, exiting...");
            }
        }, "Update-Dispatcher");
        t.setDaemon(true);
        t.start();

        return t;
    }

    public void run() {
        System.out.println("GalaxyTruckerServer RMI ready (port 1099)");
    }

    @Override
    public void connect(RmiClientIf client) throws RemoteException {
        clients.add(client);
        try {
            client.showUpdate("Welcome!");
            System.out.println("Client connected: " + client.toString());
        }
        catch (Exception e) {
            System.out.println("Error connecting to RMI server: " + e.getMessage());
        }

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

    @Override
    public int sendSrvAddPlayerReqAndWaitResp(String username) throws RemoteException {
        int min = 1;
        int max = 1000;
        int newId = ThreadLocalRandom.current().nextInt(min, max + 1);
        Player p = new Player(newId, username, true);
        controller.addPlayer(p);
        //updates.offer("Player " + username + " joined (id = " + newId + ")");
        System.out.println("[ServerRMI] Player " + username + " joined (id = " + newId + ")");
        updates.offer(new PlayerAddedMessage(newId));
        return newId;
    }

    public Component pickComponent(int playerId) throws RemoteException {
        Player p = this.controller.getPlayerById(playerId);
        Component c = this.controller.getCompFromDeck(p);
        System.out.println("[ServerRMI] Picked component: " + c.getClass().getSimpleName());
        updates.offer(new PickComponentMessage(playerId, c));
        return c;
    }

    @Override
    public void placeComponent(int playerId, Component component, Position position) {
        Player p = controller.getPlayerById(playerId);
        boolean ok = controller.addComponentForPlayer(p, component, position);

        System.out.println("[ServerRMI] Placed component: " + component.getClass().getSimpleName() + " at " + position);
        updates.offer(new ComponentPlacedMessage(playerId, component, position, ok));
    }



}