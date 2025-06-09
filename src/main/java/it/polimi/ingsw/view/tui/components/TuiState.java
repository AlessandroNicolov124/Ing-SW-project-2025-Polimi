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
    CHOOSE_START_POSITION, // New state for choosing starting position
    WAIT_FOR_ALL_PLAYERS_POSITIONED, // New state for waiting after this player chose position
    RUNNING,              // execute game commands
    TERMINATED,            // exit

    LOBBY_MENU,
    WAIT_IN_MATCH,
    LOADING,
    WAITING_FOR_PLAYERS

}