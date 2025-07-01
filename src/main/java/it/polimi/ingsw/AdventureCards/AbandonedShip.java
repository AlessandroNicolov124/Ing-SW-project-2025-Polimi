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
    private static final long serialVersionUID = 1L;

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
    public void effect(GameModel gameModel){
        // For abandoned ship, players can choose to salvage in flight order  
        // This implementation would need UI interaction, so for now just log
        System.out.println("Abandoned Ship encountered - players can choose to salvage for credits");
    }

    /**
     * Allow a player to use the abandoned ship opportunity
     * @param player The player who wants to use the opportunity
     * @return true if the opportunity was successfully used, false otherwise
     */
    public boolean useOpportunity(Player player, GameModel gameModel) {
        if (opportunityUsed) {
            System.out.println("Opportunity already used by another player.");
            return false; // Opportunity already used by another player
        }

        int currentCredits = gameModel.getCredits(player);

        Map<CrewType, Integer> playerCrewCounts = player.getShip().getCrewCounts();
        // Verify that player has enough crew of each required type
        if (playerCrewCounts == null || playerCrewCounts.isEmpty()) {
            System.out.println("No crew available on the ship.");
            return false; // No crew available
        }
        for (Map.Entry<CrewType, Integer> requirement : crewTypeRequired.entrySet()) {
            CrewType requiredType = requirement.getKey();
            int requiredCount = requirement.getValue();
            int playerCount = playerCrewCounts.getOrDefault(requiredType, 0);
            if (playerCount < requiredCount) {
                System.out.println("Not enough crew of type " + requiredType + ". Required: " + requiredCount + ", Available: " + playerCount);
                return false;
            }
        }

        // Se ha abbastanza crew, rimuovi i crew richiesti
        // Rimuovi i crew richiesti e verifica che la rimozione sia andata a buon fine
        for (Map.Entry<CrewType, Integer> requirement : crewTypeRequired.entrySet()) {
            CrewType crewType = requirement.getKey();
            int requiredCount = requirement.getValue();
            int removed = player.getShip().removeCrewOfType(crewType, requiredCount);
            if (removed < requiredCount) {
                // Rollback: riaggiungi i crew già rimossi (opzionale, se serve coerenza)
                for (Map.Entry<CrewType, Integer> rollback : crewTypeRequired.entrySet()) {
                    if (rollback.getKey() == crewType) break;
                    player.getShip().addCrewOfType(rollback.getKey(), rollback.getValue());
                }
                return false;
            }
        }

        // Aggiorna crediti e flight days
        gameModel.setCredits(player, currentCredits + creditsGained);
        gameModel.updatePosition(player, flightDaysCost);
        
        System.out.println("[AbandonedShip] " + player.getNickName() + " used abandoned ship: +" + creditsGained + 
                         " credits, lost " + flightDaysCost + " flight days");

        // Pulisci la mappa dei crew
        Map<CrewType, Integer> crewCounts = player.getShip().getCrewCounts();
        crewCounts.entrySet().removeIf(e -> e.getValue() == 0);

        opportunityUsed = true;
        return true;
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