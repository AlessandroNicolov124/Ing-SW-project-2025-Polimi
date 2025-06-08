package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Abandoned Ship

 * If you find an Abandoned Ship, you're in gravy! There's probably some sort of protocol for reporting these things, but
 * who cares? Fix it up and sell it to your crew. (Yeah, they're so sick of flying with you, they'll pay for the chance to
 * jump ship.)

 * Only one player can use this opportunity. The leader decides first. The leader can give up the specified number of crew
 * figures and take the indicated number of cosmic credits. This also costs flight days.

 * If the leader chooses not to use the opportunity, it falls to the next player in line, and so on. Once one player fixes
 * up the ship, the remaining players are out of luck.

 * Sometimes you'll find a ship that you'll be tempted to keep for yourself. Don't be greedy. Let the crew have it.
 * There's probably a reason that ship was abandoned.
 */

public class AbandonedShip extends AdventureCard {
    private Map<CrewType, Integer> crewTypeRequired;
    private int creditsGained;
    private int flightDaysCost;
    private boolean opportunityUsed;

    public AbandonedShip(int copies, int duration, Map<CrewType, Integer> crewTypeRequired, int creditsGained, int flightDaysCost) {
        super(copies, duration);
        this.crewTypeRequired = crewTypeRequired;
        this.creditsGained = creditsGained;
        this.flightDaysCost = flightDaysCost;
        this.opportunityUsed = false;
    }

    @Override
    protected int additionalPls(){
        return 0;
    }

    @Override
    protected void resolveEvent(Player p) {
        // The leader gets first opportunity, if declined it passes to the next player
    }

    @Override
    public void effect(){
        // This would be called when the card is drawn
    }

    /**
     * Allow a player to use the abandoned ship opportunity
     * @param player The player who wants to use the opportunity
     * @return true if the opportunity was successfully used, false otherwise
     */
    public boolean useOpportunity(Player player, GameModel gameModel) {
        if (opportunityUsed) {
            return false; // Opportunity already used by another player
        }
        
        // Implement crew figure check and exchange logic
        // This is a simplified implementation
        int currentCredits = gameModel.getCredits(player);

        Map<CrewType, Integer> playerCrewCounts = player.getShip().getCrewCounts();
        // Verify that player has enough crew of each required type
        boolean hasEnoughCrew = true;
        for (Map.Entry<CrewType, Integer> requirement : crewTypeRequired.entrySet()) {
            CrewType requiredType = requirement.getKey();
            int requiredCount = requirement.getValue();

            int playerCount = playerCrewCounts.getOrDefault(requiredType, 0);
            if (playerCount < requiredCount) {
                hasEnoughCrew = false;
                break;
            }
        }

        if(hasEnoughCrew) {
            // Try to remove the required crew
            boolean allCrewRemoved = true;

            // Keep track of successfully removed crew to revert if needed
            Map<CrewType, Integer> removedCrew = new HashMap<>();

            // Try to remove each type of required crew
            for (Map.Entry<CrewType, Integer> requirement : crewTypeRequired.entrySet()) {
                CrewType crewType = requirement.getKey();
                int requiredCount = requirement.getValue();

                int actuallyRemoved = player.getShip().removeCrewOfType(crewType, requiredCount);
                removedCrew.put(crewType, actuallyRemoved);

                if (actuallyRemoved < requiredCount) {
                    allCrewRemoved = false;
                    break;
                }
            }

            if (allCrewRemoved) {
                // If successful, update player's credits and mark opportunity as used
                gameModel.setCredits(player, currentCredits + creditsGained);

                // Apply flight day cost
                gameModel.updatePosition(player, -flightDaysCost);

                opportunityUsed = true;
                return true;
            } else {
//                // Revert any removed crew if we couldn't remove all required crew
//                for (Map.Entry<CrewType, Integer> removed : removedCrew.entrySet()) {
//                    CrewType crewType = removed.getKey();
//                    int count = removed.getValue();
//
//                    // Add back any crew we managed to remove before failing
//                    if (count > 0) {
//                        player.getShip().addCrewOfType(crewType, count);
//                    }
//                }
                System.out.println( "ERROR to remove Crew Member ");
                return false;
            }

        }else{
            return false;
        }


//        // Check if player has enough crew to trade
//        int crewNumber = player.getShip().getTotalCrewCount();
//        // Logic to check and remove crew would go here
//        if(crewNumber >= crewRequired){
//            if(player.removeCrewMembers(crewRequired)){
//                // If successful, update player's credits and mark opportunity as used
//                gameModel.setCredits(player, currentCredits + creditsGained);
//
//                // Apply flight day cost
//                // This would involve updating the flight board
//                gameModel.updatePosition(player, -flightDaysCost);
//
//                opportunityUsed = true;
//            }else{
//                return false;
//            }
//
//        }else{
//            return false;
//        }
//        return true;
    }

    // Getters and setters
    public Map<CrewType, Integer> getCrewTypeRequired() {
        return crewTypeRequired;
    }

    public int getCreditsGained() {
        return creditsGained;
    }

    public int getFlightDaysCost() {
        return flightDaysCost;
    }

    public boolean isOpportunityUsed() {
        return opportunityUsed;
    }
}