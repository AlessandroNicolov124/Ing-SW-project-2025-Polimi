package it.polimi.ingsw.view.tui.components;

public enum TuiState {
    INTRO,                // banner
    ASK_USERNAME,         // ask username
    WAIT_FOR_CALLBACK_PLAYER,  // wait for player creation
    ASK_NET,              // ask socket / rmi
    ASK_ADDRESS,          // ask host:port
    CONNECTING,           // create client
    WAIT_PLAYER_READY,    // Wait for player to be ready
    SHIP_BUILDING,        // Added for ship building phase
    PREPARATION_FLIGHT,     // Preparation flight phase
    WAIT_ALL_SHIPS_READY, // Wait for all players to finish preparation flight
    CHOOSE_START_POSITION, // New state for choosing starting position
    WAIT_FOR_ALL_PLAYERS_POSITIONED, // New state for waiting after this player chose position
    ADVENTURE_PHASE,      // Pick adventure cards in position order
    RUNNING,              // execute game commands
    GAME_ENDED,           // Game completed (adventure deck empty)
    TERMINATED,            // exit

    LOBBY_MENU,
    WAIT_IN_MATCH,
    LOADING,
    WAITING_FOR_PLAYERS

}