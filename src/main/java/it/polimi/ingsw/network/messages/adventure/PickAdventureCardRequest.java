package it.polimi.ingsw.network.messages.adventure;

import it.polimi.ingsw.network.messages.RequestMessage;
import it.polimi.ingsw.network.socket.server.SocketClientHandler;

public class PickAdventureCardRequest implements RequestMessage {

    private static final long serialVersionUID = 1L;
    private final int playerId;

    public PickAdventureCardRequest(int playerId) {
        this.playerId = playerId;
    }

    public int getPlayerId() {
        return playerId;
    }

    @Override
    public void handleServer(SocketClientHandler handler) {
        try {
            handler.pickAdventureCard(playerId);
        } catch (Exception e) {
            System.err.println("Error handling pickAdventureCard request: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 