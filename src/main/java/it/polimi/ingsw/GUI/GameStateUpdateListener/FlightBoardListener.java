package it.polimi.ingsw.GUI.GameStateUpdateListener;

import it.polimi.ingsw.Player;

import java.util.List;
import java.util.UUID;

public interface FlightBoardListener {
    void flightOrderRecieved(UUID matchId, int playerId, List<Player> list, boolean success);
}
