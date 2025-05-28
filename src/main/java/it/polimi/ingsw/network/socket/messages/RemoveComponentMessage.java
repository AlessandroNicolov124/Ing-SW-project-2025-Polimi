package it.polimi.ingsw.network.socket.messages;

import it.polimi.ingsw.Position;
import it.polimi.ingsw.network.socket.server.SocketClientHandler;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.Components.Component; // Required for controller method signature

public class RemoveComponentMessage implements ServerMessage {
    private static final long serialVersionUID = 1L;
    private final int playerId;
    private final Position position;

    public RemoveComponentMessage(int playerId, Position position) {
        this.playerId = playerId;
        this.position = position;
    }

    public int getPlayerId() {
        return playerId;
    }

    public Position getPosition() {
        return position;
    }

    @Override
    public void handleServer(SocketClientHandler handler) throws Exception {
        System.out.println("[Server] Processing RemoveComponentMessage for player " + playerId + ": Pos=" + position);
        Player player = handler.getController().getPlayerById(playerId);
        if (player != null) {
            // GameController.removeComponentForPlayer expects a Component object.
            // We need to get the component at the given position from the player's ship first.
            Component componentToRemove = player.getShip().getComponentAt(position);
            boolean success = false;
            String errorMsg = "No component found at position " + position;

            if (componentToRemove != null) {
                success = handler.getController().removeComponentForPlayer(player, componentToRemove);
                errorMsg = success ? null : "Failed to remove component.";
            } 
            handler.sendUpdate(new ComponentRemovedMessage(playerId, position, success, errorMsg));
        } else {
            handler.sendUpdate(new ComponentRemovedMessage(playerId, position, false, "Player not found."));
        }
    }
} 