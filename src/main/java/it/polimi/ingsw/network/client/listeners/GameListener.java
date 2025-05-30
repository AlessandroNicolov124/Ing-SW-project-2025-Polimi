package it.polimi.ingsw.network.client.listeners;


import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.network.socket.messages.RotationType;
import it.polimi.ingsw.Position;

import java.util.List;
import java.util.UUID;

public interface GameListener {
    
    void displayRefresh();
    void componentPlaced(Component component, Position position);
    void componentRemoved(Position position);
    void componentRotated(Position position, RotationType rotationType);
    void shipValidated(boolean isValid, String message);
    void showAvailablePositions(List<Integer> positions);
    boolean notifyNewPlayerPosition(int playerId, int position);

    void printTextMsg(String message);

    void matchCreated(UUID matchId, String matchName, String playerName);
    void playerJoinedMatch(UUID matchId, String playerName);
}