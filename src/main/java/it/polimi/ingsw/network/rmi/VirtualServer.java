package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.network.client.GeneralClientIf;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface VirtualServer extends Remote{

    void connect(RmiClient client) throws RemoteException;

    void stopServer() throws RemoteException;

    int sendSrvAddPlayerReqAndWaitResp(String username) throws RemoteException;


}