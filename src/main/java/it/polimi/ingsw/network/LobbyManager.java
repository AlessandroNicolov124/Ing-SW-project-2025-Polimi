package it.polimi.ingsw.network;

import it.polimi.ingsw.Player;
import java.util.List;
import java.util.UUID;

public interface LobbyManager {

    int generatePlayerId();
    
    GameMatch createMatch(String matchName, Player host);
    
    boolean joinMatch(UUID matchId, Player player);
    
    boolean leaveMatch(UUID matchId, Player player);

    boolean startMatch(UUID matchId, int requestingPlayerId);

    List<String> getAvailableMatches();
    
    GameMatch findMatchByPlayerId(int playerId);
    
    GameMatch findMatchById(UUID matchId);
    
    void removeMatch(UUID matchId, int playerId);

    void removeEmptyMatches();

    void removeInProgressMatch();

}