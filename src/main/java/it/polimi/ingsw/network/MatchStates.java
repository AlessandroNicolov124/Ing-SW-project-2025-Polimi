package it.polimi.ingsw.network;

public enum MatchStates {
    WAITING_FOR_PLAYERS,
    LOADING,
    IN_PROGRESS,
    SHIP_VALIDATION,      
    FLIGHT_PREPARATION, 
    WAIT_ALL_SHIPS_READY,  
    FLIGHT_IN_PROGRESS,
    FINISHED
}