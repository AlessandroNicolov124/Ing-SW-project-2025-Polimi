package it.polimi.ingsw.network.socket.server;

import java.io.IOException;

public interface VirtualServer {

    int sendSrvAddPlayerReqAndWaitResp(String username) throws IOException;

}