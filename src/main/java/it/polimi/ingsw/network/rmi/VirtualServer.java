package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.Position;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.network.client.RmiClientIf;
import it.polimi.ingsw.network.socket.messages.RotationType;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface VirtualServer extends Remote {

    void connect(RmiClientIf client) throws RemoteException;

    void stopServer() throws RemoteException;

    int sendSrvAddPlayerReqAndWaitResp(String username) throws RemoteException;

    Component pickComponent(int playerId) throws RemoteException;

    void placeComponent(int playerId, Component component, Position position) throws RemoteException;

    void removeComponent(int playerId, Position position) throws RemoteException;

    void rotateComponent(int playerId, Position position, RotationType rotationType) throws RemoteException;
    
    void validateShip(int playerId) throws RemoteException;

}