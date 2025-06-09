package it.polimi.ingsw.network.messages.lobby;

import java.util.List;

import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class GetMatchesResponse implements ResponseMessage {

    private static final long serialVersionUID = 1L;
    private final List<String> matches;

    public GetMatchesResponse(List<String> matches) {
        this.matches = matches;
    }

    public List<String> getMatches() {
        return matches;
    }

    @Override
    public void handleClient(GameListener listener) {
        listener.updateAvailableMatches(matches);
    }
    
}
