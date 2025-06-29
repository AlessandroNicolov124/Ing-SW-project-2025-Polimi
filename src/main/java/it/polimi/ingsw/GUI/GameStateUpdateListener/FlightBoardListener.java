package it.polimi.ingsw.GUI.GameStateUpdateListener;

import it.polimi.ingsw.AdventureCards.AdventureCard;
import it.polimi.ingsw.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface FlightBoardListener {
    void flightOrderRecieved(UUID matchId, int playerId, List<Player> list, boolean success);
    void adventureCardPicketFromsrv(int playerId, AdventureCard adventureCard, boolean success, String message, int nextPlayerTurn, Map<Integer, Integer> updatedPositions);
}
