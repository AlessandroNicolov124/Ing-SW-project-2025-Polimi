package it.polimi.ingsw.GUI.GameStateUpdateListener;

import java.util.List;
import java.util.UUID;

public interface LobbyListener {
    void availableMatches(List<String> matches);
    void matchCreated(UUID matchId, String matchName, String playerName, int playerId);

    void playerJoinedMatch(UUID matchId, String playerName);
    void playerLeftMatch(UUID matchId, int playerId, String playerName);
    void playersInMatch(UUID matchId, List<String> playerNames, int hostId);
    void updateHost(UUID matchId, int hostId, String hostName);

    void matchStarted(UUID matchId, String username);
    void loadingStatus(UUID matchId, int ready, int total);
    void playerReady(UUID matchId, int ready, int total);
    void shipBuildingStart(UUID matchId, int readyPlayers);
}