package it.polimi.ingsw;

import java.util.HashMap;
import java.util.Map;

public class FlightBoard {
    private Map<Player, Integer> positions;

    public FlightBoard() {
        positions = new HashMap<>();
    }

    public void updatePosition(Player p, int spaces) {
        positions.put(p, positions.getOrDefault(p, 0) + spaces);
    }
}
