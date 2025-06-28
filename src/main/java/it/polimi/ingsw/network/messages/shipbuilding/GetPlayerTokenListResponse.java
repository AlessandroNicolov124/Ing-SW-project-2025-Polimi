package it.polimi.ingsw.network.messages.shipbuilding;

import java.util.List;
import java.util.Map;

import it.polimi.ingsw.Position;
import it.polimi.ingsw.TokenType;
import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class GetPlayerTokenListResponse implements ResponseMessage {
    private final String playerName;
    private final Map<Position, List<TokenType>> tokens;
    private final boolean success;
    private final String message;

    public GetPlayerTokenListResponse(String playerName, Map<Position, List<TokenType>> tokens, boolean success, String message) {
        this.playerName = playerName;
        this.tokens = tokens;
        this.success = success;
        this.message = message;
    }

    @Override
    public void handleClient(GameListener listener) {
        listener.getPlayerTokenList(playerName, tokens);
    }
}