package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.AdventureCards.AdventureCard;
import it.polimi.ingsw.GameController;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.network.Difficulty;
import it.polimi.ingsw.network.GameMatch;
import it.polimi.ingsw.network.LobbyManager;
import it.polimi.ingsw.network.MatchStates;
import it.polimi.ingsw.network.client.RmiClientIf;
import it.polimi.ingsw.network.messages.RotationType;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

public interface VirtualServer extends Remote {

    void connect(RmiClientIf client) throws RemoteException;

    void stopServer() throws RemoteException;

    Component pickComponent(int playerId, RmiClientIf client) throws RemoteException;

    void placeComponent(int playerId, Component component, Position position, RmiClientIf client) throws RemoteException;

    void removeComponent(int playerId, Position position, RmiClientIf client) throws RemoteException;

    void rotateComponent(int playerId, Position position, RotationType rotationType, RmiClientIf client) throws RemoteException;
    
    void validateShip(int playerId, RmiClientIf client) throws RemoteException;

    int createPlayer(String username, RmiClientIf client) throws RemoteException;

    void getAvailableMatches(RmiClientIf client) throws RemoteException;

    UUID createMatch(String matchName, int playerId, String playerName, Difficulty difficulty, RmiClientIf client) throws RemoteException;

    boolean joinMatch(UUID matchId, int playerId, String playerName, RmiClientIf client) throws RemoteException;

    boolean leaveMatch(UUID matchId, int playerId, String playerName, RmiClientIf client) throws RemoteException;

    boolean startMatch(UUID matchId, int playerId, String playerName, RmiClientIf client) throws RemoteException;

    boolean loadingAck(UUID matchId, RmiClientIf client) throws RemoteException;

    boolean playerReady(UUID matchId, int playerId, RmiClientIf client) throws RemoteException;

    void selectCabinColor(String color, int playerId, RmiClientIf client) throws RemoteException;
    
    void pickAdventureCard(int playerId, RmiClientIf client) throws RemoteException;
    void readyForFlight(UUID matchId, int playerId, RmiClientIf client) throws RemoteException;

    /**
     * Select a planet to land on during a Planets adventure card
     * @param playerId The player making the selection
     * @param planetIndex Index of the chosen planet (-1 if no planet chosen)
     * @param client The client making the request
     * @throws RemoteException if there's an RMI communication error
     */
    void selectPlanet(int playerId, int planetIndex, RmiClientIf client) throws RemoteException;

    //void getPlayerOrderFromsrv();

}
