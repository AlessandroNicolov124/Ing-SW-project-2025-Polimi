package it.polimi.ingsw.network.client;

import it.polimi.ingsw.Components.Component;

import java.util.List;
import java.util.UUID;

import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.network.rmi.messages.UpdateMessage;
import it.polimi.ingsw.network.socket.messages.RotationType;

public interface GeneralClientIf extends AutoCloseable {
    
    int createPlayer(String username) throws Exception;

    void placeComponent(Component component, Position position) throws Exception;

    void showUpdate(String message) throws Exception;

    default void showUpdate(UpdateMessage msg) throws Exception {
        showUpdate(msg.toString());
    }

    void validateShip() throws Exception;

    void rotateComponent(Position position, RotationType rotationType) throws Exception;

    void setLocalPlayer(Player player) throws Exception;

    Component pickComponentCard() throws Exception;

    UUID createMatch(String matchName) throws Exception;

    List<String> getAvailableMatches() throws Exception;

    boolean joinMatch(UUID matchId) throws Exception;

    boolean leaveMatch(UUID matchId) throws Exception;

    boolean startMatch(UUID matchId) throws Exception;

    boolean isPlayerHostById(UUID matchId) throws Exception;

    List<String> getPlayersInMatch(UUID matchId) throws Exception;

    Player getLocalPlayer() throws Exception;

}
