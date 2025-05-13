package it.polimi.ingsw.network.socket.messages;

import it.polimi.ingsw.network.socket.server.SocketClientHandler;
import it.polimi.ingsw.Player;

public class ValidateShipMessage implements ServerMessage {
    private static final long serialVersionUID = 1L;
    private final int playerId;

    public ValidateShipMessage(int playerId) {
        this.playerId = playerId;
    }

    public int getPlayerId() {
        return playerId;
    }

    @Override
    public void handleServer(SocketClientHandler handler) throws Exception {
        System.out.println("[Server] Processing ValidateShipMessage for player " + playerId);
        Player player = handler.getController().getPlayerById(playerId);
        boolean isValid = false;
        String reason = "Player not found."; // Default reason

        if (player != null) {
            isValid = handler.getController().validateShipForPlayer(player);
            // We might need more detailed reasons from validateShip in the future
            reason = isValid ? "Ship is valid." : "Ship validation failed (check connections, required components, etc.).";
        }
        handler.sendUpdate(new ShipValidationResultMessage(playerId, isValid, reason));
    }
} 