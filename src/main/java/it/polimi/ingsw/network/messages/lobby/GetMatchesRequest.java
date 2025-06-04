package it.polimi.ingsw.network.messages.lobby;


import it.polimi.ingsw.network.messages.RequestMessage;
import it.polimi.ingsw.network.socket.server.SocketClientHandler;

public class GetMatchesRequest implements RequestMessage {
    
    private static final long serialVersionUID = 1L;

    public GetMatchesRequest() {

    }

    @Override
    public void handleServer(SocketClientHandler handler) {
        try {
            handler.getAvailableMatches();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
