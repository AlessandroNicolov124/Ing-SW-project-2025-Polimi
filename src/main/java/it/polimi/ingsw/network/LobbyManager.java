package it.polimi.ingsw.network;

import it.polimi.ingsw.network.client.GeneralClientIf;
import it.polimi.ingsw.network.messages.ResponseMessage;

import java.util.List;
import java.util.UUID;

public interface LobbyManager {

    int generatePlayerId(GeneralClientIf client);

    boolean registerUsername(int playerId, String playerName);

    boolean isUsernameAvailable(String username);

    void unregisterUsername(int playerId);
    
    GameMatch createMatch(String matchName, int hostId, String hostName);
    
    boolean joinMatch(UUID matchId, int playerId, String playerName);
    
    boolean leaveMatch(UUID matchId, int playerId);

    boolean startMatch(UUID matchId, int requestingPlayerId);

    boolean loadingAckResponse(UUID matchId);

    List<String> getAvailableMatches();
    
    GameMatch findMatchByPlayerId(int playerId);
    
    GameMatch findMatchById(UUID matchId);
    
    void removeEmptyMatches();

    void removeInProgressMatch();

    // Methods for managing clients

    void addClientToMatch(UUID matchId, GeneralClientIf client);
    
    void removeClientFromMatch(UUID matchId, GeneralClientIf client);
    
    List<GeneralClientIf> getClientsInMatch(UUID matchId);
    
    void broadcastToAll(ResponseMessage responseMessage);
    
    void multicastToMatch(UUID matchId, ResponseMessage responseMessage);

    void multicastToMatchExceptOne(UUID matchId, ResponseMessage responseMessage, GeneralClientIf client);

}