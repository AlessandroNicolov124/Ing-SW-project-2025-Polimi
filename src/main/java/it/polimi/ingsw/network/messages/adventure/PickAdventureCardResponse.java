package it.polimi.ingsw.network.messages.adventure;

import it.polimi.ingsw.AdventureCards.AdventureCard;
import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class PickAdventureCardResponse implements ResponseMessage {

    private static final long serialVersionUID = 1L;
    private final int playerId;
    private final AdventureCard adventureCard;
    private final boolean success;
    private final String message;
    private final int nextPlayerTurn; // Which player should pick next (-1 if phase is complete)

    public PickAdventureCardResponse(int playerId, AdventureCard adventureCard, boolean success, String message, int nextPlayerTurn) {
        this.playerId = playerId;
        this.adventureCard = adventureCard;
        this.success = success;
        this.message = message;
        this.nextPlayerTurn = nextPlayerTurn;
    }

    public int getPlayerId() {
        return playerId;
    }

    public AdventureCard getAdventureCard() {
        return adventureCard;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getNextPlayerTurn() {
        return nextPlayerTurn;
    }

    @Override
    public void handleClient(GameListener listener) {
        listener.adventureCardPicked(playerId, adventureCard, success, message, nextPlayerTurn);
    }
} 