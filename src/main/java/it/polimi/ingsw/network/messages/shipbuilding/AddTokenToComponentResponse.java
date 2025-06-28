package it.polimi.ingsw.network.messages.shipbuilding;

import it.polimi.ingsw.Position;
import it.polimi.ingsw.TokenType;
import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class AddTokenToComponentResponse implements ResponseMessage {
    
    private static final long serialVersionUID = 1L;
    private final String playerName;
    private final Position position;
    private final TokenType tokenType;
    private final boolean success;
    private final String errorMessage;

    public AddTokenToComponentResponse(String playerName, Position position, TokenType tokenType, boolean success, String errorMessage) {
        this.playerName = playerName;
        this.position = position;
        this.tokenType = tokenType;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    @Override
    public void handleClient(GameListener listener) {
        listener.addTokenToComponent(playerName, position, tokenType, success, errorMessage);
    }

}