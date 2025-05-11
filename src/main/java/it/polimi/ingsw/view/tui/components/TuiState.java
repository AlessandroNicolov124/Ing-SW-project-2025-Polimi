package it.polimi.ingsw.view.tui.components;

public enum TuiState {
    INTRO,                // banner
    ASK_USERNAME,         // ask username
    ASK_NET,              // ask socket / rmi
    ASK_ADDRESS,          // ask host:port
    CONNECTING,           // create client
    WAIT_PLAYER_READY,    // Wait for player to be ready
    SHIP_BUILDING,        // Added for ship building phase
    RUNNING,              // execute game commands
    TERMINATED            // exit
}