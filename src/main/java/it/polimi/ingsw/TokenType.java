package it.polimi.ingsw;

import javafx.scene.paint.Color;

public enum TokenType {

    HUMAN(Color.WHITE),
    BATTERY(Color.GREEN),
    BROWN_ALIEN(Color.BROWN),
    PURPLE_ALIEN(Color.PURPLE),
    GREEN_GOOD_BLOCK(Color.GREEN),
    RED_GOOD_BLOCK(Color.RED),
    BLUE_GOOD_BLOCK(Color.BLUE),
    YELLOW_GOOD_BLOCK(Color.YELLOW);

    private final Color color;
    
    TokenType(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
    
}