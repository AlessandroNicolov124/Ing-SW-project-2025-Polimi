package it.polimi.ingsw.network.socket.client;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import it.polimi.ingsw.network.client.GeneralClient;
import it.polimi.ingsw.network.socket.messages.ClientMessage;
import it.polimi.ingsw.network.socket.messages.GenericMessage;
import it.polimi.ingsw.network.socket.messages.TextUpdateMessage;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.network.socket.messages.RotationType;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.view.tui.components.GalaxyTruckerTUI;


public class SocketClient implements Runnable, VirtualView {
    
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream  in;
    private final VirtualSocketServer proxy;
    private final BlockingDeque<GenericMessage> messages = new LinkedBlockingDeque<>();
    private int playerId = -1;
    private Player localPlayerReference;
    private GalaxyTruckerTUI tui;

    public SocketClient(String host,int port, GalaxyTruckerTUI tui) throws IOException {
        socket = new Socket(host,port);
        out = new ObjectOutputStream(socket.getOutputStream()); 
        out.flush();
        in  = new ObjectInputStream (socket.getInputStream());
        proxy = new VirtualSocketServer(out, messages);
        this.tui = tui;
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
        System.out.println("[Server] " + text);
    }

    @Override public void close() throws Exception { 
        socket.close(); 
    }


    public int addPlayer(String username) throws IOException {
        return proxy.addPlayer(username);
    }

    public void placeComponent(Component component, Position position) throws IOException {
        if (playerId == -1) {
            throw new IllegalStateException("Cannot place component before player ID is assigned.");
        }
        proxy.placeComponent(playerId, component, position);
    }

    public void removeComponent(Position position) throws IOException {
        if (playerId == -1) {
            throw new IllegalStateException("Cannot remove component before player ID is assigned.");
        }
        proxy.removeComponent(playerId, position);
    }

    public void rotateComponent(Position position, RotationType rotationType) throws IOException {
        if (playerId == -1) {
            throw new IllegalStateException("Cannot rotate component before player ID is assigned.");
        }
        proxy.rotateComponent(playerId, position, rotationType);
    }

    public void validateShip() throws IOException {
        if (playerId == -1) {
            throw new IllegalStateException("Cannot validate ship before player ID is assigned.");
        }
        proxy.validateShip(playerId);
    }

    public void setPlayerId(int id) {
        this.playerId = id;
    }

    public void setLocalPlayer(Player player) {
        this.localPlayerReference = player;
    }

    public void handleLocalComponentPlacement(Component component, Position position, boolean success) {
        if (success && localPlayerReference != null) {
            localPlayerReference.getShip().placeComponent(component, position);
            if (tui != null) tui.triggerShipDisplayRefresh();
        }
    }

    public void handleLocalComponentRemoval(Position position, boolean success) {
        if (success && localPlayerReference != null) {
            Component compToRemove = localPlayerReference.getShip().getComponentAt(position);
            if (compToRemove != null) {
                localPlayerReference.getShip().removeComponent(compToRemove);
            }
            if (tui != null) tui.triggerShipDisplayRefresh();
        }
    }

    public void handleLocalComponentRotation(Position position, Component updatedComponentState, boolean success) {
        if (success && localPlayerReference != null && updatedComponentState != null) {
            Component oldComp = localPlayerReference.getShip().getComponentAt(position);
            if (oldComp != null) localPlayerReference.getShip().removeComponent(oldComp);
            localPlayerReference.getShip().placeComponent(updatedComponentState, position);
            if (tui != null) tui.triggerShipDisplayRefresh();
        }
    }
}