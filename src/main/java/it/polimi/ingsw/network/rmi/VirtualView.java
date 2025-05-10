package it.polimi.ingsw.network.rmi;

import java.rmi.RemoteException;

import it.polimi.ingsw.network.client.GameClient;

import java.rmi.Remote;

public interface VirtualView extends Remote, GameClient {
    
    void showUpdate(String message) throws RemoteException;

    @Override
    int addPlayer(String username) throws RemoteException;

}