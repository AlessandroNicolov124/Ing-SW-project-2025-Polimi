package it.polimi.ingsw.network.client;

import java.io.IOException;

public abstract class GeneralClient {
    private ClientCommunicateToSrvIf clientToSrv;
    private GeneralVirtualViewIf virtualView;

    public int addPlayerToMatchOnSrv(String username) throws IOException {return 0;}
}
