package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.GameModel;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.ShipBuilder;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.Direction;
import it.polimi.ingsw.Components.*;
import java.util.*;

/**
 * Epidemic

 * Epidemic makes you remove 1 crew member (human or alien) from every occupied cabin that is joined to another occupied
 * cabin. The safe thing to do is to build your ship so that no two cabins are joined. If you do have joined cabins,
 * maybe you can find an adventure that will allow you to empty one of them before the epidemic strikes.
 */

public class Epidemic extends AdventureCard {

    public Epidemic() {
        super();
    }
    
    public Epidemic(int copies, int duration) {
        super(copies, duration);
    }

    @Override
    protected int additionalPls() {
        return 0;
    }

    @Override
    protected void resolveEvent(Player p) {
        // Apply epidemic to a single player
        applyEpidemic(p);
    }

    @Override
    public void effect(GameModel gameModel) {
        // Apply epidemic to all players
        for (Player player : gameModel.getPlayers()) {
            applyEpidemic(player);
        }
    }
    
    /**
     * Apply epidemic effects to a player's ship
     * @param player The player whose ship is affected by the epidemic
     * @return Number of crew members lost
     */
    public int applyEpidemic(Player player) {
        int crewLost = 0;
        // Find all cabins in the ship
        List<Cabin> cabins = player.getShip().getAllCabins();
        
        // Check which cabins are connected to other cabins
        List<Cabin> connectedCabins = findConnectedCabins(cabins, player.getShip());

        // Remove 1 crew member from each connected cabin
        for (Cabin cabin : connectedCabins) {
            if (player.getShip().removeCrewFromCabin(cabin.getCrew().getFirst(), cabin)) {
                crewLost++;
            }
        }
        
        return crewLost;
    }
    
    /**
     * Find cabins that are connected to other cabins
     * @param cabins List of all cabins
     * @param ship The player's ship
     * @return List of cabins that are connected to other cabins
     */
    private List<Cabin> findConnectedCabins(List<Cabin> cabins, ShipBuilder ship) {
        List<Cabin> connectedCabins = new ArrayList<>();
        
        for (Cabin cabin : cabins) {
            // Check if cabin is occupied
            if (!cabin.isOccuped()) {
                continue;
            }

            // Get position of this cabin
            Position cabinPosition = ship.getPositionOf(cabin);
            
            // Check all adjacent positions for other occupied cabins
            for (Direction direction : Direction.values()) {
                Position adjacentPosition = direction.getAdjacentPosition(cabinPosition);
                Component adjacentComponent = ship.getComponentAt(adjacentPosition);
                
                switch (adjacentComponent) {
                    case Cabin c:
                        if (c.isOccuped()) {
                            // This cabin is connected to another occupied cabin
                            connectedCabins.add(cabin);
                            break;
                        }
                        break;
                    case null:
                        // No component at this position, continue checking other directions
                        break;
                    default:
                        break;
                }
            }
        }
        
        return connectedCabins;
    }
}