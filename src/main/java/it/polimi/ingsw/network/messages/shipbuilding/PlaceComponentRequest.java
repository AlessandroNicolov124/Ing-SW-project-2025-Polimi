package it.polimi.ingsw.network.messages.shipbuilding;

import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.network.messages.RequestMessage;
import it.polimi.ingsw.network.socket.server.SocketClientHandler;

public class PlaceComponentRequest implements RequestMessage {

    private static final long serialVersionUID = 1L;
    private final int playerId;
    private final Component component;
    private final Position position;

    public PlaceComponentRequest(int playerId, Component component, Position position) {
        this.playerId = playerId;
        this.component = component;
        this.position = position;
    }

    @Override
    public void handleServer(SocketClientHandler handler) {
        try {
            handler.placeComponent(component, position, playerId);
        } catch (Exception e) {
            System.err.println("Error handling placeComponent request: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
} 