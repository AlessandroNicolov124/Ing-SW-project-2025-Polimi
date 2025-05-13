package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.Position;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.network.client.RmiClientIf;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface VirtualServer extends Remote {

    void connect(RmiClientIf client) throws RemoteException;

    void stopServer() throws RemoteException;

    int sendSrvAddPlayerReqAndWaitResp(String username) throws RemoteException;

    void placeComponent(int playerId, Component component, Position position) throws RemoteException;

}