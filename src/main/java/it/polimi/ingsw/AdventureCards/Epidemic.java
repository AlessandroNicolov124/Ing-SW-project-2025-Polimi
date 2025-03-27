package it.polimi.ingsw.AdventureCards;

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
    int additionalPls() {
        return 0;
    }

    @Override
    void resolveEvent(Player p) {
        // Apply epidemic to a single player
        applyEpidemic(p);
    }

    @Override
    public void effect() {
        // This would be called when the card is drawn
    }
    
    /**
     * Apply epidemic effects to a player's ship
     * @param player The player whose ship is affected by the epidemic
     * @return Number of crew members lost
     */
    public int applyEpidemic(Player player) {
        int crewLost = 0;
        ShipBuilder ship = player.getShip();
        
        // Find all cabins in the ship
        List<Component> allComponents = ship.getAllComponents();
        List<Cabin> cabins = new ArrayList<>();
        
        for (Component component : allComponents) {
            if (component instanceof Cabin) {
                cabins.add((Cabin) component);
            }
        }
        
        // Check which cabins are connected to other cabins
        List<Cabin> connectedCabins = findConnectedCabins(cabins, ship);
        
        // Remove 1 crew member from each connected cabin
        for (Cabin cabin : connectedCabins) {
            if (removeCrewMember(cabin)) {
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
            if (!isCabinOccupied(cabin)) {
                continue;
            }
            
            // Get position of this cabin
            Position cabinPosition = ship.getPositionOf(cabin);
            
            // Check all adjacent positions for other occupied cabins
            for (Direction direction : Direction.values()) {
                Position adjacentPosition = direction.getAdjacentPosition(cabinPosition);
                Component adjacentComponent = ship.getComponentAt(adjacentPosition);
                
                if (adjacentComponent instanceof Cabin) {
                    Cabin adjacentCabin = (Cabin) adjacentComponent;
                    if (isCabinOccupied(adjacentCabin)) {
                        // This cabin is connected to another occupied cabin
                        connectedCabins.add(cabin);
                        break;
                    }
                }
            }
        }
        
        return connectedCabins;
    }
    
    /**
     * Check if a cabin is occupied by crew
     * @param cabin The cabin to check
     * @return true if the cabin is occupied, false otherwise
     */
    private boolean isCabinOccupied(Cabin cabin) {
        // Implementation depends on how cabin occupancy is tracked
        // For now, assume all cabins are occupied
        return true;
    }
    
    /**
     * Remove a crew member from a cabin
     * @param cabin The cabin to remove a crew member from
     * @return true if a crew member was removed, false otherwise
     */
    private boolean removeCrewMember(Cabin cabin) {
        // Implementation depends on how crew are tracked
        return true; // placeholder
    }
}