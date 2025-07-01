package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.Components.*;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.GameModel;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Abandoned Station

 * When fleeing the tragic disaster that befell this space station, the inhabitants probably left behind some good loot.
 * It will take a big crew to search for it, though. To use this opportunity, you must have at least as many crew as shown
 * on the card.

 * Only one player can use this opportunity. The leader decides first. If the leader does not have enough crew or if the
 * leader does not wish to lose those flight days, the leader can pass the opportunity to the next player along the
 * flight. Once someone decides to dock, the others are out of luck.

 * When you dock with a space station, load the indicated goods on your ship. You can rearrange or discard goods at this
 * time. Move your marker back the indicated number of flight days.

 * Note that on an Abandoned Station you lose no crew.
 */

public class AbandonedStation extends AdventureCard{
    private static final long serialVersionUID = 1L;
    private int minCrewRequired;
    private int flightDaysCost;
    private List<Cargo> goodsToLoad;
    private boolean opportunityUsed;

    public AbandonedStation(int copies, int duration, int minCrewRequired, int flightDaysCost, List<Cargo> goodsToLoad) {
        super(copies, duration);
        this.minCrewRequired = minCrewRequired;
        this.flightDaysCost = flightDaysCost;
        this.goodsToLoad = goodsToLoad;
        this.opportunityUsed = false;
    }

    @Override
    protected int additionalPls(){
        return 0;
    }

    @Override
    protected void resolveEvent(Player p) {
        // Implementation depends on how player choice is handled in the game
    }

    @Override
    public void effect(GameModel gameModel){
        // For abandoned station, players can choose to dock in flight order
        // This implementation would need UI interaction, so for now just log
        System.out.println("Abandoned Station encountered - players can choose to dock for goods");
    }
    
    /**
     * Allow a player to dock at the abandoned station
     * @param player The player who wants to dock
     * @param gameModel The current game state
     * @return true if docking was successful, false otherwise
     */
    public boolean dockAtStation(Player player, GameModel gameModel) {
        if (opportunityUsed) {
            System.out.println("Opportunity already used by another player.");
            return false; // Opportunity already used by another player
        }
        
        // Check if player has enough crew
        int crewCount = player.getShip().getTotalCrewCount();
        
        if (crewCount < minCrewRequired) {
            System.out.println("Not enough crew to dock at the station.");
            return false; // Not enough crew
        }
        
        // Load goods on player's ship
        if(!loadGoods(player)){
            System.out.println("No goods available to load.");
            return false;
        }

        // Apply flight day cost (positive value = move to worse position)
        gameModel.updatePosition(player, flightDaysCost);
        
        System.out.println("[AbandonedStation] " + player.getNickName() + " docked at station, lost " + 
                         flightDaysCost + " flight days");

        opportunityUsed = true;
        return true;
    }

    /**
     * Load goods from a planet onto a player's ship
     *
     * @param player The player loading goods
     * @return true if goods were loaded successfully (almost one), false otherwise
     */
    private boolean loadGoods(Player player) {
        // Implementation of how goods are tracked

        // Ordine di priorità: rosso, giallo, verde, blu
        Comparator<Cargo> cargoComparator = Comparator
                .comparing((Cargo cargo) -> {
                    switch(cargo) {
                        case RedCargo c: return 1;
                        case YellowCargo y: return 2;
                        case GreenCargo g: return 3;
                        case BlueCargo b: return 4;
                        default: return 5;
                    }
                })
                .thenComparing(Cargo::getValue, Comparator.reverseOrder());
        // Crea una lista mutabile per ordinare
        List<Cargo> sortedGoods = new java.util.ArrayList<>(goodsToLoad);
        sortedGoods.sort(cargoComparator);

        // stive del giocatore
        List<CargoHolds> playerCargoHolds = player.getShip().getCargoHolds();
        // Calcola lo spazio totale disponibile
        int totalAvailableSpace = playerCargoHolds.stream()
                .mapToInt(CargoHolds::getAvailableSpace)
                .sum();

        boolean loaded = false;

        // Cerca di caricare le merci
        for (Cargo cargo : sortedGoods) {
            // Se non c'è più spazio, interrompi
            if (totalAvailableSpace <= 0) {
                break;
            }

            loaded = false;

            // Gestione speciale per i rossi
            if (cargo.isRed()) {
                // Prova prima nelle stive speciali rosse
                for (CargoHolds cargoHold : playerCargoHolds) {
                    switch (cargoHold) {
                        case SpecialCargoHolds c:
                            if (!cargoHold.isFull() && cargoHold.canContainCargo(cargo)) {
                                cargoHold.loadCargo(cargo);
                                loaded = true;
                                totalAvailableSpace--;
                            }
                            break;
                        default:
                            break;
                    }
                }

                // Se non caricato nelle speciali, prova nelle normali
                if (!loaded) {
                    for (CargoHolds cargoHold : playerCargoHolds) {
                        if (!cargoHold.isFull() && cargoHold.canContainCargo(cargo)) {
                            cargoHold.loadCargo(cargo);
                            loaded = true;
                            totalAvailableSpace--;
                            break;
                        }
                    }
                }
            }
            // Per tutti gli altri colori
            else {
                // Prova a caricare nelle stive che possono contenere quel tipo di merce
                for (CargoHolds cargoHold : playerCargoHolds) {
                    if (!cargoHold.isFull() && cargoHold.canContainCargo(cargo)) {
                        cargoHold.loadCargo(cargo);
                        loaded = true;
                        totalAvailableSpace--;
                        break;
                    }
                }

                // Se non caricato nelle stive appropriate,
                // carica in qualsiasi stiva disponibile
                if (!loaded) {
                    for (CargoHolds cargoHold : playerCargoHolds) {
                        if (!cargoHold.isFull()) {
                            cargoHold.loadCargo(cargo);
                            loaded = true;
                            totalAvailableSpace--;
                            break;
                        }
                    }
                }
            }
        }
        return loaded;
    }
    
    // Getters and setters
    public int getMinCrewRequired() {
        return minCrewRequired;
    }
    
    public int getFlightDaysCost() {
        return flightDaysCost;
    }
    
    public List<Cargo> getGoodsToLoad() {
        return goodsToLoad;
    }
    
    public boolean isOpportunityUsed() {
        return opportunityUsed;
    }
}