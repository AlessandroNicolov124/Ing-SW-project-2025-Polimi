package it.polimi.ingsw.network.socket.messages;

import it.polimi.ingsw.Player;
import it.polimi.ingsw.network.socket.server.SocketClientHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Message sent from client to server to request a position on the flight board.
 */
public class SelectPositionMessage implements ServerMessage {
    private int playerId;
    private int position;

    public SelectPositionMessage(int playerId, int position) {
        this.playerId = playerId;
        this.position = position;
    }

    @Override
    public void handleServer(SocketClientHandler handler) {
        System.out.println("[Server] Player " + playerId + " requested position " + position);
        
        // Get the player from the controller
        Player player = handler.getController().getPlayerById(playerId);
        
        if (player == null) {
            System.out.println("[Server] Player with ID " + playerId + " not found");
            handler.sendUpdate(new PositionResponseMessage(playerId, position, false, "Player not found"));
            return;
        }
        
        // Check available positions
        List<Integer> availablePositions = handler.getController().getAvailableStartingPositions();
        
        if (!availablePositions.contains(position)) {
            System.out.println("[Server] Position " + position + " is not available");
            handler.sendUpdate(new PositionResponseMessage(playerId, position, false, 
                    "Position " + position + " is not available. Available positions: " + availablePositions));
            return;
        }
        
        // Try to set position
        boolean success = handler.getController().setPlayerInitialStartingPosition(player, position);
        
        if (success) {
            System.out.println("[Server] Player " + player.getNickName() + " (ID: " + playerId + ") successfully claimed position " + position);
            // Send response to the client who requested
            handler.sendUpdate(new PositionResponseMessage(playerId, position, true, "Position set successfully"));
            
            // Broadcast update to all clients
            handler.broadcast(new PositionUpdateMessage(playerId, player.getNickName(), position));
            
            // Check if all players have positions now
            checkAllPlayersPositioned(handler);
        } else {
            System.out.println("[Server] Failed to set position " + position + " for player " + player.getNickName());
            handler.sendUpdate(new PositionResponseMessage(playerId, position, false, "Failed to set position"));
        }
    }
    
    /**
     * Check if all active players have selected positions and if so, broadcast game start
     */
    private void checkAllPlayersPositioned(SocketClientHandler handler) {
        List<Player> players = handler.getController().getPlayers();
        Map<Player, Integer> positions = handler.getController().getAllPlayerPositions();
        
        boolean allPositioned = true;
        for (Player p : players) {
            if (p.isActive() && (positions.get(p) == null || positions.get(p) <= 0)) {
                allPositioned = false;
                break;
            }
        }
        
        if (allPositioned && !players.isEmpty()) {
            System.out.println("[Server] All players have selected positions. Starting game phase.");
            
            // Convert player positions to a map of player IDs to positions for the client
            Map<Integer, Integer> playerPositions = new HashMap<>();
            for (Map.Entry<Player, Integer> entry : positions.entrySet()) {
                playerPositions.put(entry.getKey().getId(), entry.getValue());
            }
            
            // Broadcast to all clients that all positions are selected
            handler.broadcast(new AllPositionsSelectedMessage(playerPositions));
        }
    }

    public int getPlayerId() {
        return playerId;
    }

    public int getPosition() {
        return position;
    }
} 