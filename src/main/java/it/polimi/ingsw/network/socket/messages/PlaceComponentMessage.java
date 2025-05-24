package it.polimi.ingsw.network.socket.messages;

import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.network.socket.server.SocketClientHandler;
import it.polimi.ingsw.Player;

public class PlaceComponentMessage implements ServerMessage {
    private static final long serialVersionUID = 1L; // Good practice for Serializable classes
    private final int playerId;
    private final Component component;
    private final Position position;

    public PlaceComponentMessage(int playerId, Component component, Position position) {
        this.playerId = playerId;
        this.component = component;
        this.position = position;
    }

    public int getPlayerId() {
        return playerId;
    }

    public Component getComponent() {
        return component;
    }

    public Position getPosition() {
        return position;
    }

    @Override
    public void handleServer(SocketClientHandler handler) throws Exception {
        System.out.println("[ServerSock] Processing PlaceComponentMessage for player " + playerId + ": Component=" + component.getClass().getSimpleName() + ", Pos=" + position);
        Player player = handler.getController().getPlayerById(playerId);

        if (player != null) {
            boolean success = handler.getController().addComponentForPlayer(player, component, position);
            String errorMsg = success ? null : "Failed to place component (check position/connections).";
            handler.sendUpdate(new ComponentPlacedMessage(playerId, component, position, success, errorMsg));
        } else {
            // Handle case where player is not found (e.g., invalid ID)
            handler.sendUpdate(new ComponentPlacedMessage(playerId, component, position, false, "Player with ID " + playerId + " not found."));
        }
        // System.out.println("Server received PlaceComponentMessage for player " + playerId + " component " + component.getClass().getSimpleName() + " at " + position);
        // handler.sendUpdate(new ComponentPlacedMessage(playerId, component, position, true, null));
    }
} 