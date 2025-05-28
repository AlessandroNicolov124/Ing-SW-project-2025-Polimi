package it.polimi.ingsw.network.socket.messages;

import it.polimi.ingsw.network.socket.client.SocketClient;

/**
 * Message broadcast to all clients when a player's position on the flight board changes.
 */
public class PositionUpdateMessage implements ClientMessage {
    private int playerId;
    private String playerName;
    private int position;

    public PositionUpdateMessage(int playerId, String playerName, int position) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.position = position;
    }

    @Override
    public void handleClient(SocketClient client) {
        // Log the position update
        System.out.println("[Client] Player " + playerName + " (ID: " + playerId + ") is now at position " + position);
        
        // Update the client's local game model
        client.updatePlayerPosition(playerId, position);
    }

    public int getPlayerId() {
        return playerId;
    }
    
    public String getPlayerName() {
        return playerName;
    }

    public int getPosition() {
        return position;
    }
} 