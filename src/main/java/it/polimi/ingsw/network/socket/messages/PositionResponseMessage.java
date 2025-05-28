package it.polimi.ingsw.network.socket.messages;

import it.polimi.ingsw.network.socket.client.SocketClient;

/**
 * Response message sent from server to client after a position selection request.
 */
public class PositionResponseMessage implements ClientMessage {
    private int playerId;
    private int position;
    private boolean success;
    private String message;

    public PositionResponseMessage(int playerId, int position, boolean success, String message) {
        this.playerId = playerId;
        this.position = position;
        this.success = success;
        this.message = message;
    }

    @Override
    public void handleClient(SocketClient client) {
        // Log the response on the client side
        if (success) {
            System.out.println("[Client] Successfully claimed position " + position);
        } else {
            System.out.println("[Client] Failed to claim position " + position + ": " + message);
        }
        
        // Notify the client's view about the result
        client.notifyPositionResponse(playerId, position, success, message);
    }

    public int getPlayerId() {
        return playerId;
    }

    public int getPosition() {
        return position;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
} 