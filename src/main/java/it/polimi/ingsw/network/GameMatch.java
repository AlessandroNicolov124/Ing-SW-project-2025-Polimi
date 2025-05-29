package it.polimi.ingsw.network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import it.polimi.ingsw.GameController;
import it.polimi.ingsw.Player;

public class GameMatch {
    
    private static final int MAX_PLAYERS = 4;

    private UUID matchId;
    private GameController controller;
    private List<Player> players;
    private String matchName;
    private MatchStates matchState;
    private Player hostPlayer;


    public GameMatch(String matchName, Player hostPlayer) {
        this.matchId = UUID.randomUUID();
        this.controller = new GameController();
        this.players = new ArrayList<>();
        this.matchName = matchName;
        this.matchState = MatchStates.WAITING_FOR_PLAYERS;
        this.hostPlayer = hostPlayer;
        addPlayer(hostPlayer);
    }

    public UUID getMatchId() {
        return this.matchId;
    }

    public String getMatchName() {
        return this.matchName;
    }

    public GameController getController() {
        return this.controller;
    }

    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    public int getNumberOfPlayers() {
        return this.players.size();
    }

    public void addPlayer(Player player) {
        if (this.players.size() < MAX_PLAYERS && matchState == MatchStates.WAITING_FOR_PLAYERS) {
            this.players.add(player);
            this.controller.addPlayer(player);
            System.out.println("[GameMatch " + matchId + "] Player " + player.getNickName() + " added to match " + matchName);
        } else {
            System.out.println("[GameMatch " + matchId + "] Cannot add player " + player.getNickName() + ": Match is full or already started");  
        }
    }

    public boolean removePlayer(Player player) {
        boolean removed = this.players.remove(player);
        
        if (removed && matchState == MatchStates.WAITING_FOR_PLAYERS) {
            if (player.equals(this.hostPlayer) && !players.isEmpty()) {
                // for simplicity, we just take the first player as the new host i guess
                this.hostPlayer = players.get(0);
                System.out.println("[GameMatch " + matchId + "] Player " + player.getNickName() + " left. New host is " + hostPlayer.getNickName());
            }
            System.out.println("[GameMatch " + matchId + "] Player " + player.getNickName() + " removed from match " + matchName);
        }
        else {
            System.out.println("[GameMatch " + matchId + "] Player " + player.getNickName() + " could not be removed: Match is in progress or player not found");
        }
        return removed;
    }

    public boolean removePlayerById(int playerId) {
        Player playerToRemove = players.stream()
                .filter(player -> player.getId() == playerId)
                .findFirst()
                .orElse(null);
        if (playerToRemove != null) {
            return removePlayer(playerToRemove);
        } else {
            System.out.println("[GameMatch " + matchId + "] Player with ID " + playerId + " not found in match " + matchName);
        }
        return false;
    }

    public boolean isFull() {
        return this.players.size() >= MAX_PLAYERS;
    }

    public boolean isEmpty() {
        return this.players.isEmpty();
    }

    public boolean canStartMatch() {
        return this.players.size() >= 2 && matchState == MatchStates.WAITING_FOR_PLAYERS;
    }

    public boolean isPlayerInMatch(Player player) {
        return this.players.contains(player);
    }

    public boolean isPlayerInMatchById(int playerId) {
        return this.players.stream().anyMatch(player -> player.getId() == playerId);
    }

    public Player getPlayerById(int playerId) {
        return players.stream()
                .filter(player -> player.getId() == playerId)
                .findFirst()
                .orElse(null);
    }

    public void startMatch() {
        if (canStartMatch()) {
            this.matchState = MatchStates.IN_PROGRESS;
            this.controller.startGame(this.players); // TODO: modify GameController to accept a list of players i guess
            System.out.println("[GameMatch " + matchId + "] Match " + matchName + " started with players: " + players);
        } else {
            System.out.println("[GameMatch " + matchId + "] Cannot start match " + matchName + ": Not enough players or already started");
        }
    }

    public void endMatch() {
        this.matchState = MatchStates.FINISHED;
        System.out.println("[GameMatch " + matchId + "] Match " + matchName + " has ended.");
        // TODO: handle end of match logic
    }

    public MatchStates getMatchState() {
        return this.matchState;
    }

    public Player getHostPlayer() {
        return this.hostPlayer;
    }

    public boolean isPlayerHostById(int playerId) {
        return this.hostPlayer.getId() == playerId;
    }

    // TODO: implement something to retrieve match statistics or results

}