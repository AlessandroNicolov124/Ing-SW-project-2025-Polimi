package it.polimi.ingsw.network.messages.shipbuilding;

import it.polimi.ingsw.Position;
import it.polimi.ingsw.TokenType;
import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class RemoveTokenFromComponentResponse implements ResponseMessage {
    private final String playerName;
    private final Position position;
    private final TokenType tokenType;
    private final boolean success;
    private final String message;    

    public RemoveTokenFromComponentResponse(String playerName, Position position, TokenType tokenType, boolean success, String message) {
        this.playerName = playerName;
        this.position = position;
        this.tokenType = tokenType;
        this.success = success;
        this.message = message;
    }


    @Override
    public void handleClient(GameListener listener) {
        listener.removeTokenFromComponent(playerName, position, tokenType, success, message);
    }
    
}
