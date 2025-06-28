package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.Position;
import it.polimi.ingsw.TokenType;
import it.polimi.ingsw.Components.Cargo;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.network.Difficulty;
import it.polimi.ingsw.network.GameMatch;
import it.polimi.ingsw.network.client.RmiClientIf;
import it.polimi.ingsw.network.messages.RotationType;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
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

    boolean startMatch(UUID matchId, int playerId, String username, RmiClientIf client) throws RemoteException;

    boolean loadingAck(UUID matchId, RmiClientIf client) throws RemoteException;

    boolean playerReady(UUID matchId, int playerId, RmiClientIf client) throws RemoteException;

    void selectCabinColor(String color, int playerId, RmiClientIf client) throws RemoteException;
    
    void pickAdventureCard(int playerId, RmiClientIf client) throws RemoteException;
    void readyForFlight(UUID matchId, int playerId, RmiClientIf client) throws RemoteException;

    void getPlayerShip(String playerName, RmiClientIf client) throws RemoteException;

    void addTokenToComponent(String playerName, Position position, TokenType tokenType, RmiClientIf client) throws RemoteException;

    void getPlayerTokenList(String playerName, RmiClientIf client) throws RemoteException;

    void removeTokenFromComponent(String playerName, Position position, TokenType tokenType, RmiClientIf client) throws RemoteException;

}
