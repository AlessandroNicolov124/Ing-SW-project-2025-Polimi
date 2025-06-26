package it.polimi.ingsw.network.messages.lobby;

import java.util.List;

import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class GetMatchesResponse implements ResponseMessage {

    private static final long serialVersionUID = 1L;
    private final List<String> matches;
    private final boolean success;
    private final String errorMessage;

    public GetMatchesResponse(List<String> matches, boolean success, String errorMessage) {
        this.matches = matches;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    @Override
    public void handleClient(GameListener listener) {
        listener.updateAvailableMatches(matches);
    }
    
}
