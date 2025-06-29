package it.polimi.ingsw.network.messages.adventure;

import it.polimi.ingsw.Player;
import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

import java.util.List;
import java.util.UUID;

public class FlightOrderRespone  implements ResponseMessage {
    private final UUID matchId;
    private int playerId;
    private List<Player> listPos;
    private boolean success;

    public FlightOrderRespone(UUID matchId, int playerId, List<Player> listPos, boolean success) {
        this.matchId = matchId;
        this.playerId = playerId;
        this.listPos = listPos;
        this.success = success;
    }

    @Override
    public void handleClient(GameListener listener) {
        listener.FlightOrderpositionRecieved(matchId, playerId, listPos, success);
    }
}
