package it.polimi.ingsw.network.client.listeners;


import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.network.socket.messages.RotationType;
import it.polimi.ingsw.Position;

import java.util.List;
import java.util.UUID;

public interface GameListener {
    
    void playerCreated(int playerId, String playerName);
    
    void displayRefresh();
    void componentPlaced(Component component, Position position);
    void componentRemoved(Position position);
    void componentRotated(Position position, RotationType rotationType);
    void shipValidated(boolean isValid, String message);
    void showAvailablePositions(List<Integer> positions);
    boolean notifyNewPlayerPosition(int playerId, int position);

    void printTextMsg(String message);

    void matchCreated(UUID matchId, String matchName, String playerName, int playerId);
    void playerJoinedMatch(UUID matchId, String playerName);
    void playerLeftMatch(UUID matchId, int playerId, String playerName);
    void updateAvailableMatches(List<String> matchNames);
    void updateHost(UUID matchId, int playerId, String playerName);
    void matchStarted(UUID matchId, String username);
    void playersInMatch(UUID matchId, List<String> players, int hostId);
}