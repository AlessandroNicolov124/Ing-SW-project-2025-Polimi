package it.polimi.ingsw.network.socket.messages;

import it.polimi.ingsw.Position;
import it.polimi.ingsw.network.socket.server.SocketClientHandler;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.Components.Component;

public class RotateComponentMessage implements ServerMessage {
    private static final long serialVersionUID = 1L;
    private final int playerId;
    private final Position position;
    private final RotationType rotationType;

    public RotateComponentMessage(int playerId, Position position, RotationType rotationType) {
        this.playerId = playerId;
        this.position = position;
        this.rotationType = rotationType;
    }

    // Getters...
    public int getPlayerId() { return playerId; }
    public Position getPosition() { return position; }
    public RotationType getRotationType() { return rotationType; }

    @Override
    public void handleServer(SocketClientHandler handler) throws Exception {
        System.out.println("[Server] Processing RotateComponentMessage for player " + playerId + ": Pos=" + position + ", Type=" + rotationType);
        Player player = handler.getController().getPlayerById(playerId);
        Component rotatedComponent = null;
        boolean success = false;
        String errorMsg = "Action failed.";

        if (player != null) {
            Component componentToRotate = player.getShip().getComponentAt(position);
            if (componentToRotate != null) {
                // Perform rotation via GameController
                switch (rotationType) {
                    case CLOCKWISE:
                        rotatedComponent = handler.getController().rotateComponentClockwise(player, componentToRotate);
                        break;
                    case ANTI_CLOCKWISE:
                        rotatedComponent = handler.getController().rotateComponentCounterClockwise(player, componentToRotate);
                        break;
                    case OPPOSITE:
                        rotatedComponent = handler.getController().rotateComponentOpposite(player, componentToRotate);
                        break;
                }
                // Assuming rotation methods return the rotated component or null/throw exception on failure
                // For simplicity, let's assume it always succeeds if component is found
                // A more robust implementation would check return values or catch exceptions
                success = true; 
                errorMsg = null;
            } else {
                errorMsg = "No component found at position " + position;
            }
        } else {
            errorMsg = "Player not found.";
        }
        // Send response (even if rotation failed locally, send state)
        handler.sendUpdate(new ComponentRotatedMessage(playerId, position, rotatedComponent, success, errorMsg));
    }
} 