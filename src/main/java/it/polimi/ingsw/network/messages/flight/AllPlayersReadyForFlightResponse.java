package it.polimi.ingsw.network.messages.flight;

import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class AllPlayersReadyForFlightResponse implements ResponseMessage {

    private static final long serialVersionUID = 1L;
    private final boolean allPlayersReady;
    private final boolean success;
    private final String errorMessage;

    public AllPlayersReadyForFlightResponse(boolean allPlayersReady, boolean success, String errorMessage) {
        this.allPlayersReady = allPlayersReady;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    @Override
    public void handleClient(GameListener listener) {
        listener.allPlayersReadyForFlight(allPlayersReady);
    }
    
    
}
