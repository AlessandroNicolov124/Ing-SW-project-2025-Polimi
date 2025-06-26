package it.polimi.ingsw.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import it.polimi.ingsw.GameController;
import it.polimi.ingsw.Player;

public class GameMatch {

    private static final int MAX_PLAYERS = 4;

    private UUID matchId;
    private String matchName;
    private GameController controller;
    private List<Player> players;
    private MatchStates matchState;
    private Player hostPlayer;
    private Map<Integer, Boolean> readyPlayersMap;

    private Map<Integer, String> playerCabinColors = new HashMap<>();
    private Set<String> availableColors = new HashSet<>(Arrays.asList("BLUE", "GREEN", "YELLOW", "RED"));
    private Map<Integer, Boolean> playersValidated = new HashMap<>();
    private Map<Integer, Boolean> playersReadyForFlight = new HashMap<>();

    private Difficulty difficulty;

    public GameMatch(UUID matchId, String matchName, int hostId, String hostName, Difficulty difficulty) {
        this.matchId = matchId;
        this.matchName = matchName;
        this.controller = new GameController();
        this.players = new ArrayList<>();
        this.matchState = MatchStates.WAITING_FOR_PLAYERS;
        this.difficulty = difficulty;

        Player host = new Player(hostId, hostName, true);
        this.players.add(host);
        this.hostPlayer = host;
        this.controller.addPlayer(host);
    }

    // Getter
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

    public MatchStates getMatchState() {
        return this.matchState;
    }

    public synchronized Player getHostPlayer() {
        return this.hostPlayer;
    }

    public Player getPlayerById(int playerId) {
        return players.stream()
                .filter(player -> player.getId() == playerId)
                .findFirst()
                .orElse(null);
    }

    public Map<Integer, Boolean> getReadyPlayersMap() {
        return readyPlayersMap;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    // General match state checks
    public boolean isFull() {
        return this.players.size() >= MAX_PLAYERS;
    }

    public synchronized boolean isEmpty() {
        return this.players.isEmpty();
    }

    public boolean canStartMatch() {
        return this.players.size() >= 2 && matchState == MatchStates.WAITING_FOR_PLAYERS;
    }

    public boolean isPlayerInMatch(int playerId) {
        return this.players.contains(getPlayerById(playerId));
    }

    public boolean isPlayerInMatchById(int playerId) {
        return this.players.stream().anyMatch(player -> player.getId() == playerId);
    }

    public boolean isPlayerHostById(int playerId) {
        return this.hostPlayer != null && this.hostPlayer.getId() == playerId;
    }

    public void setPlayerCabinColor(int playerId, String color) {
        String previousColor = playerCabinColors.get(playerId);
        if (previousColor != null) {
            availableColors.add(previousColor);
        }
        
        playerCabinColors.put(playerId, color);
        availableColors.remove(color);
    }

    public String getPlayerCabinColor(int playerId) {
        return playerCabinColors.get(playerId);
    }
    
    public boolean isColorTaken(String color) {
        return !availableColors.contains(color);
    }

    public boolean allPlayersValidated() {
        return playersValidated.size() == players.size() && 
            playersValidated.values().stream().allMatch(Boolean::booleanValue);
    }

    public void setPlayerReadyForFlight(int playerId, boolean ready) {
        playersReadyForFlight.put(playerId, ready);
    }

    public boolean allPlayersReadyForFlight() {
        if (playersReadyForFlight.isEmpty()) {
            return false;
        }
        
        for (Player player : players) {
            if (player.isActive()) {
                Boolean isReady = playersReadyForFlight.get(player.getId());
                if (isReady == null || !isReady) {
                    return false;
                }
            }
        }
        return true;
    }

    public synchronized void setMatchState(MatchStates state) {
        MatchStates oldState = this.matchState;
        this.matchState = state;
        System.out.println("[GameMatch " + matchId + "] State changed from " + oldState + " to " + state);
    }

    public Map<Integer, Boolean> getPlayersValidated() {
        return playersValidated;
    }

    // Handling players in the match
    public synchronized void addPlayer(int playerId, String playerName) {
        if (this.players.size() < MAX_PLAYERS && matchState == MatchStates.WAITING_FOR_PLAYERS) {
            if (isPlayerInMatchById(playerId)) {
                System.out.println("[GameMatch " + matchId + "] Player " + playerName + " already in match " + matchName);
                return;
            }
            if (isFull() || matchState != MatchStates.WAITING_FOR_PLAYERS) {
                System.out.println("[GameMatch " + matchId + "] Cannot add player " + playerName + ": Match is full or already started");
                return;
            }
            Player player = new Player(playerId, playerName, true);
            this.players.add(player);
            this.controller.addPlayer(player);

            if (hostPlayer == null) {
                hostPlayer = player;
                System.out.println("[GameMatch " + matchId + "] Player " + playerName + " became host (no previous host)");
            } 

            System.out.println("[GameMatch " + matchId + "] Player " + playerName + " added to match " + matchName);
        } else {
            System.out.println("[GameMatch " + matchId + "] Cannot add player " + playerName + ": Match is full or already started");
        }
    }

    public synchronized boolean removePlayer(int playerId) {
        Player player = getPlayerById(playerId);
        if (player == null) {
            System.out.println("[GameMatch " + matchId + "] Player ID "
                + playerId + " not found in match " + matchName);
            return false;
        }

        // IMPORTANTE: Rimuovi e ripristina il colore della cabina del giocatore
        String color = playerCabinColors.remove(playerId);
        if (color != null) {
            availableColors.add(color);
            System.out.println("[GameMatch " + matchId + "] Released cabin color " + color + " for player " + player.getNickName());
        }

        // Rimuovi dalle mappe di stato
        playersValidated.remove(playerId);
        playersReadyForFlight.remove(playerId);
        if (readyPlayersMap != null) {
            readyPlayersMap.remove(playerId);
        }

        boolean wasHost = isPlayerHostById(playerId);
        boolean removed = this.players.remove(player);

        if (!removed) {
            System.out.println("[GameMatch " + matchId + "] Player " + player.getNickName() + " could not be removed from match " + matchName);
            return false;
        }

        System.out.println("[GameMatch " + matchId + "] Player " + player.getNickName() + " removed from match " + matchName);

        // Gestisci la riassegnazione dell'host
        if (players.isEmpty()) {
            this.hostPlayer = null;
            System.out.println("[GameMatch " + matchId + "] Match is now empty, no host assigned");
        } else if (wasHost) {
            // Assegna il primo giocatore rimanente come nuovo host
            this.hostPlayer = players.get(0);
            System.out.println("[GameMatch " + matchId + "] New host assigned: " + hostPlayer.getNickName() + " (ID: " + hostPlayer.getId() + ")");
        }

        return removed;
    }

    // Control match state
    public synchronized void startMatch() {
        if (matchState == MatchStates.WAITING_FOR_PLAYERS && players.size() >= 2) {
            matchState = MatchStates.LOADING;
            
            if (controller != null) {
                controller.startGame();
            }
            
            System.out.println("[GameMatch " + matchId + "] Match " + matchName + " started with " + players.size() + " players. Loading game...");
        } else {
            System.out.println("[GameMatch " + matchId + "] Cannot start match: insufficient players or wrong state");
        }
    }

    public synchronized void loadingAckResponse() {
        if (matchState == MatchStates.LOADING) {
            readyPlayersMap = new java.util.HashMap<>();
            for (Player player : players) {
                readyPlayersMap.put(player.getId(), false);
            }
            System.out.println("[GameMatch " + matchId + "] Game loaded. Waiting for players to finish loading...");
        }
    }

    public synchronized boolean setPlayerReady(int playerId) {
        if (readyPlayersMap == null || !readyPlayersMap.containsKey(playerId)) {
            System.out.println("[GameMatch " + matchId + "] Player ID " + playerId + " is not in the match or match is not loading.");
            return false;
        }

        // if the player is already ready, ignore the request otherwise he would be able to spam (interesting behavior for trolling)
        if (Boolean.TRUE.equals(readyPlayersMap.get(playerId))) {
            System.out.println("[GameMatch " + matchId + "] Player ID " + playerId + " was already ready. Ignoring duplicate.");
            return false;
        }

        readyPlayersMap.put(playerId, true);
        System.out.println("[GameMatch " + matchId + "] Player ID " + playerId + " is ready.");

        if (readyPlayersMap.values().stream().allMatch(Boolean::booleanValue)) {
            matchState = MatchStates.IN_PROGRESS;
            System.out.println("[GameMatch " + matchId + "] All players are ready. " + "Match is now in progress.");
        } else {
            System.out.println("[GameMatch " + matchId + "] Waiting for all players to be ready...");
        }
        return true;
    }


    public void endMatch() {
        this.matchState = MatchStates.FINISHED;
        System.out.println("[GameMatch " + matchId + "] Match " + matchName + " has ended.");
        // TODO: handle end of match logic
    }

    // TODO: implement something to retrieve match statistics or results 

}