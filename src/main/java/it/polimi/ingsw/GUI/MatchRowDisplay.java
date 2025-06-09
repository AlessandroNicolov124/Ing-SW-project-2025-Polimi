package it.polimi.ingsw.GUI;

import java.util.UUID;

public class MatchRowDisplay {
    private UUID MatchId;
    private String matchName;
    private String playerName;
    private int playerId;

    public MatchRowDisplay(UUID IdMatch, String name, String playerName, int playerId) {
        this.MatchId = IdMatch;
        this.matchName = name;
        this.playerName = playerName;
        this.playerId = playerId;
    }

    public UUID getMatchId() {
        return MatchId;
    }
    public String getMatchName() {
        return matchName;
    }
    public String getPlayerName() {
        return playerName;
    }
    public int getPlayerId() {
        return playerId;
    }
}
