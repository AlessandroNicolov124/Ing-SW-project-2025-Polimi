package it.polimi.ingsw.GUI.GameStateUpdateListener;

import java.util.List;
import java.util.Map;

import it.polimi.ingsw.Position;
import it.polimi.ingsw.TokenType;
import it.polimi.ingsw.Components.Component;

public interface PreparationFlightListener {

    void getPlayerShipResponse(String playerName, Map<Position, Component> ship);

    void addTokenToComponent(String playerName, Position position, TokenType tokenType, boolean success, String errorMessage);

    void getPlayerTokenList(String playerName, Map<Position, List<TokenType>> tokens);

    void removeTokenFromComponent(String playerName, Position position, TokenType tokenType, boolean success, String errorMessage);



}