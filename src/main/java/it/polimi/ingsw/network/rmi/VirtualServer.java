package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.Position;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.network.client.RmiClientIf;
import it.polimi.ingsw.network.messages.RotationType;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

public interface VirtualServer extends Remote {

    void connect(RmiClientIf client) throws RemoteException;

    void stopServer() throws RemoteException;

    Component pickComponent(int playerId) throws RemoteException;

    void placeComponent(int playerId, Component component, Position position) throws RemoteException;

    void removeComponent(int playerId, Position position) throws RemoteException;

    void rotateComponent(int playerId, Position position, RotationType rotationType) throws RemoteException;
    
    void validateShip(int playerId) throws RemoteException;

    int createPlayer(String username, RmiClientIf client) throws RemoteException;

    List<String> getAvailableMatches() throws RemoteException;

    UUID createMatch(String matchName, int playerId, String playerName, RmiClientIf client) throws RemoteException;

    boolean joinMatch(UUID matchId, int playerId, String playerName, RmiClientIf client) throws RemoteException;

    boolean leaveMatch(UUID matchId, int playerId, String playerName, RmiClientIf client) throws RemoteException;

    boolean startMatch(UUID matchId, int playerId, String username) throws RemoteException;

    int getHostPlayerId(UUID matchId, int playerId) throws RemoteException;

    List<String> getPlayersInMatch(UUID matchId) throws RemoteException;

}