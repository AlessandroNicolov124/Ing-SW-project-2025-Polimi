package it.polimi.ingsw.GUI.GameStateUpdateListener;

import java.util.List;
import java.util.UUID;

import it.polimi.ingsw.network.Difficulty;

public interface LobbyListener {
    void availableMatches(List<String> matches);
    void matchCreated(UUID id, String name, String playerName, int creatorId, Difficulty difficulty);

    void playerJoinedMatch(UUID matchId, String playerName);
    void playerLeftMatch(UUID matchId, int playerId, String playerName);
    void playersInMatch(UUID matchId, List<String> playerNames, int hostId);
    void updateHost(UUID matchId, int hostId, String hostName);

    void matchStarted(UUID matchId, String username);
    void loadingStatus(UUID matchId, int ready, int total);
    void playerReady(UUID matchId, int ready, int total);
    void shipBuildingStart(UUID matchId, int readyPlayers);
    void cabinColorSelected(String color);

    void cabinColorSelectionFailed(String errorMsg);
    void matchCreatedFailed(String message);
    void playerJoinedMatchFailed(String message);
    void playerLeftMatchFailed(String message);
    void updateAvailableMatchesFailed(String message);
    void updateHostFailed(String message);
    void matchStartedFailed(String message);
    void loadingAckResponseFailed(String message);
    void loadingStatusFailed(String message);
    void playersInMatchFailed(String message);
    void playerReadyFailed(String message);
    void shipBuildingStartFailed(String message);
}