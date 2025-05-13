package it.polimi.ingsw.network.rmi;

import java.rmi.RemoteException;

import it.polimi.ingsw.network.client.GeneralVirtualViewIf;

import java.rmi.Remote;

public interface VirtualView extends Remote, GeneralVirtualViewIf {

    @Override
    void showUpdate(String message) throws RemoteException;
}