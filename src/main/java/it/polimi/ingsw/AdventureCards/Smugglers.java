package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.Player;
import it.polimi.ingsw.GameModel;
import it.polimi.ingsw.Components.*;
import java.util.*;

/**
 * Smugglers

 * Smugglers and other enemies pose a threat to everyone, but they attack the players' ships in order. First, they
 * attack the leader. If they win, they attack the next player in line, and so on, until they have attacked everyone or
 * until someone defeats them.

 * The upper right corner of the card shows what happens if you lose. (These smugglers take your 2 most valuable goods.
 * If you run out of goods, they take batteries instead.) The bottom part of the card shows what you get if you win.
 * (If you defeat Smugglers, you can load the indicated goods – see Gaining Goods on page 11.)
 * The strength of the enemy is given by the number next to the cannon symbol. (These Smugglers have strength 4.)

 * The leader counts up cannon strength, paying for any double cannons used. If this strength is greater than the
 * enemy's, the player wins and may claim the reward by losing flight days. If any player wins, the enemies go away and
 * the remaining players are not attacked.

 * If the player loses (has a lower strength than the enemy), the player pays the penalty specified in the upper half of
 * the card. Then the enemy attacks the next player.

 * In case of a tie (when the player has strength equal to the enemy's) nothing happens to that player, but the enemy is
 * not defeated, either. The enemy moves on to attack the next player.
 */

public class Smugglers extends AdventureCard {
    private int smugglerStrength;
    private List<Cargo> goodsLostOnDefeat;
    private List<Cargo> goodsGainedOnVictory;
    private int flightDaysCost;
    private boolean isDefeated;

    public Smugglers(int copies, int duration, int smugglerStrength, List<Cargo> goodsLostOnDefeat,
                     List<Cargo> goodsGainedOnVictory, int flightDaysCost) {
        super(copies, duration);
        this.smugglerStrength = smugglerStrength;
        this.goodsLostOnDefeat = goodsLostOnDefeat;
        this.goodsGainedOnVictory = goodsGainedOnVictory;
        this.flightDaysCost = flightDaysCost;
        this.isDefeated = false;
    }

    @Override
    int additionalPls() {
        return 0;
    }

    @Override
    void resolveEvent(Player p) {
        // Individual player resolution would depend on the game mechanics
    }

    @Override
    public void effect() {
        // This would be called when the card is drawn
    }
    
    /**
     * Process the smuggler encounter with all players in order
     * @param gameModel The current game state
     */
    public void processSmugglers(GameModel gameModel) {
        if (isDefeated) {
            return; // Smugglers already defeated
        }
        
        List<Player> playersInOrder = gameModel.getFlightBoard().getPlayersInFlightOrder();
        
        for (Player player : playersInOrder) {
            // Calculate player's cannon strength
            double playerStrength = player.getShip().calculateFirePower();
            
            // Compare strengths
            if (playerStrength > smugglerStrength) {
                // Player defeats smugglers
                isDefeated = true;
                
                // Offer reward (player can choose whether to accept)
                // This would be implemented in the UI
                offerReward(player, gameModel, true); // Assuming player accepts reward
                
                break; // Smugglers are defeated, no more attacks

            } else if (playerStrength < smugglerStrength) {
                // Player is defeated by smugglers
                applyPenalty(player);
                
                // Continue to next player
            } else {
                // Tie - nothing happens, continue to next player

            }
        }
    }
    
    /**
     * Offer the player a reward for defeating smugglers
     * @param player The player who defeated the smugglers
     * @param gameModel The current game state
     * @param acceptReward Whether the player accepts the reward
     */
    public void offerReward(Player player, GameModel gameModel, boolean acceptReward) {
        if (!isDefeated) {
            return; // Smugglers weren't defeated
        }
        
        if (acceptReward) {
            // Load goods on player's ship
            loadGoods(player);
            // Move player back on flight board
            gameModel.updatePosition(player, -flightDaysCost);
        }
        // If player doesn't accept reward, nothing happens to their position
    }

    /**
     * Load goods from a planet onto a player's ship
     * @param player The player loading goods
     */
    private void loadGoods(Player player) {
        // Implementation of how goods are tracked
        // placeholder
        List<Cargo> goodsToLoad = goodsLostOnDefeat;
        // Logic to add goods to player's inventory here

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
        // Ordina le merci disponibili
        goodsToLoad.sort(cargoComparator);

        // stive del giocatore
        List<CargoHolds> playerCargoHolds = player.getShip().getCargoHolds();
        // Calcola lo spazio totale disponibile
        int totalAvailableSpace = playerCargoHolds.stream()
                .mapToInt(CargoHolds::getAvailableSpace)
                .sum();

        // Cerca di caricare le merci
        for (Cargo cargo : goodsToLoad) {
            // Se non c'è più spazio, interrompi
            if (totalAvailableSpace <= 0) {
                break;
            }

            boolean loaded = false;

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
    }
    
    /**
     * Apply the penalty to a player defeated by smugglers
     * @param player The player who was defeated
     */
    private void applyPenalty(Player player) {
        // Take most valuable goods from player
        // If player has no goods, take batteries instead

        List<Cargo> allCargo = player.getShip().getAllCargo();
        // Ordina le merci per valore decrescente
        allCargo.sort(Comparator.comparing(Cargo::getValue).reversed());

        int penaltyCount = 2; // Numero di merci da rimuovere
        int removedCount = 0;

        // Rimuovi le merci più preziose
        for (int i = 0; i < allCargo.size() && removedCount < penaltyCount; i++) {
            Cargo cargo = allCargo.get(i);

            // Trova la stiva che contiene questa merce
            for (CargoHolds cargoHold : player.getShip().getCargoHolds()) {
                if (cargoHold.getCargos().contains(cargo)) {
                    cargoHold.unloadCargo(cargo);
                    removedCount++;
                    break;
                }
            }
        }

        // Se non sono state rimosse abbastanza merci, rimuovi batterie
        int batteriesNeeded = penaltyCount - removedCount;
        if (batteriesNeeded > 0) {
            // Assumendo che esista un metodo per rimuovere batterie
            player.dischargeBatteries(batteriesNeeded);
        }
    }
    
    // Getters
    public int getSmugglerStrength() {
        return smugglerStrength;
    }
    
    public List<Cargo> getGoodsLostOnDefeat() {
        return goodsLostOnDefeat;
    }
    
    public List<Cargo> getGoodsGainedOnVictory() {
        return goodsGainedOnVictory;
    }
    
    public int getFlightDaysCost() {
        return flightDaysCost;
    }
    
    public boolean isDefeated() {
        return isDefeated;
    }
}