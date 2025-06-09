package it.polimi.ingsw.network.messages.shipbuilding;

import it.polimi.ingsw.Position;
import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class RemoveComponentResponse implements ResponseMessage {

    private static final long serialVersionUID = 1L;
    private final int playerId;
    private final Position position;
    private final boolean success;
    private final String message;

    public RemoveComponentResponse(int playerId, Position position, boolean success, String message) {
        this.playerId = playerId;
        this.position = position;
        this.success = success;
        this.message = message;
    }

    public int getPlayerId() {
        return playerId;
    }

    public Position getPosition() {
        return position;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void handleClient(GameListener listener) {
        // Call the componentRemoved method for all listeners
        listener.componentRemoved(position);
        
        // Also provide feedback about the operation
        if (listener instanceof it.polimi.ingsw.view.tui.components.GalaxyTruckerTUI) {
            it.polimi.ingsw.view.tui.components.GalaxyTruckerTUI tui = 
                (it.polimi.ingsw.view.tui.components.GalaxyTruckerTUI) listener;
            
            // Check if this response is for the current player
            if (tui.getCurrentPlayer() != null && tui.getCurrentPlayer().getId() == playerId) {
                if (success) {
                    tui.showMessage("✓ Component removed from (" + position.getX() + "," + position.getY() + ")");
                } else {
                    tui.showMessage("✗ Failed to remove component: " + message);
                }
            }
        } else {
            // For non-TUI clients
            if (success) {
                System.out.println("Component removed from position: " + position);
            } else {
                System.err.println("Failed to remove component: " + message);
            }
        }
    }
} 