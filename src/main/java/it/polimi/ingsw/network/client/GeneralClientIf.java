package it.polimi.ingsw.network.client;

import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.network.rmi.messages.UpdateMessage;
import it.polimi.ingsw.network.socket.messages.RotationType;

import java.util.List;

public interface GeneralClientIf extends AutoCloseable {
    
    int addPlayer(String username) throws Exception;

    void placeComponent(Component component, Position position) throws Exception;

    void showUpdate(String message) throws Exception;

    default void showUpdate(UpdateMessage msg) throws Exception {
        showUpdate(msg.toString());
    }

    void validateShip() throws Exception;

    void rotateComponent(Position position, RotationType rotationType) throws Exception;

    void setLocalPlayer(Player player) throws Exception;

    Component pickComponentCard() throws Exception;

    boolean isPositionSelectionValid(int position) throws Exception;

    // Position selection methods
    List<Integer> requestAvailablePositions() throws Exception;
    
    boolean selectPosition(int position) throws Exception;
    
    boolean areAllPlayersPositioned() throws Exception;

}
