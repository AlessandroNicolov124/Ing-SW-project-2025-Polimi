package it.polimi.ingsw.network.messages.shipbuilding;

import it.polimi.ingsw.Position;
import it.polimi.ingsw.TokenType;
import it.polimi.ingsw.network.messages.RequestMessage;
import it.polimi.ingsw.network.socket.server.SocketClientHandler;

public class AddTokenToComponentRequest implements RequestMessage {

    private static final long serialVersionUID = 1L;
    private final String playerName;
    private final Position position;
    private final TokenType tokenType;

    public AddTokenToComponentRequest(String playerName, Position position, TokenType tokenType) {
        this.playerName = playerName;
        this.position = position;
        this.tokenType = tokenType;
    }

    @Override
    public void handleServer(SocketClientHandler handler) {
        try {
            handler.addTokenToComponent(playerName, position, tokenType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
