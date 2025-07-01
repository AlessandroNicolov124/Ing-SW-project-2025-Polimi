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
    private static final long serialVersionUID = 1L;
    private int smugglerStrength;
    private int goodsLostOnDefeat;
    private List<Cargo> goodsGainedOnVictory;
    private int flightDaysCost;
    private boolean isDefeated;

    public Smugglers(int copies, int duration, int smugglerStrength, int goodsLostOnDefeat,
                     List<Cargo> goodsGainedOnVictory, int flightDaysCost) {
        super(copies, duration);
        this.smugglerStrength = smugglerStrength;
        this.goodsLostOnDefeat = goodsLostOnDefeat;
        this.goodsGainedOnVictory = goodsGainedOnVictory;
        this.flightDaysCost = flightDaysCost;
        this.isDefeated = false;
    }

    @Override
    protected int additionalPls() {
        return 0;
    }

    @Override
    protected void resolveEvent(Player p) {
        // Individual player resolution would depend on the game mechanics
    }

    @Override
    public void effect(GameModel gameModel) {
        fightSmugglers(gameModel);
    }
    
    /**
     * Process the smuggler encounter with all players in order
     * @param gameModel The current game state
     */
    public void fightSmugglers(GameModel gameModel) {
        List<Player> playersDedeated = new ArrayList<>();

        for(Player p : gameModel.getFlightBoard().getPlayersInFlightOrder()) {
            if (!fightSmugglersFor(p)){
                playersDedeated.add(p);
            }else{
                offerReward(p, gameModel, true);
                break; // Only the first player to defeat smugglers gets the reward if he chooses to accept it
            }
        }
        applyPenalty(playersDedeated);
    }

    /**
     * Attempt to fight pirates
     * @param player The player trying to fight
     * @return true if pirates were defeated, false if player was defeated
     */
    public boolean fightSmugglersFor(Player player) {
        if (isDefeated) {
            return true; // Pirates already defeated
        }

        activateDoubleCannons(player);
        // Calculate player's cannon strength
        double playerStrength = player.getShip().calculateFirePower();
        player.getShip().dischargeAllActiveDoubleCannon();

        // Check if player defeats pirates
        if (playerStrength > smugglerStrength) {
            // Player wins, offer reward
            System.out.println("Player " + player.getNickName() + " defeated the smugglers!");
            isDefeated = true;
            return true;
        } else if(playerStrength < smugglerStrength){
            System.out.println("Player " + player.getNickName() + " was defeated by the smugglers!");
            // Player is defeated, pirates will fire at their ship
            return false;
        }else{
            return true; // Tie, smugglers are not defeated but player is not penalized
        }
    }

    /**
     * Let player decide which double cannons to activate
     * @param player The player making decisions
     */
    private void activateDoubleCannons(Player player) {
        List<DoubleCannon> doubleCannons = player.getShip().getOperationalDoubleCannons();

        for (DoubleCannon cannon : doubleCannons) {
            // Check if player has batteries and wants to activate this cannon
            if (player.getShip().calculateTotalBatteryCharge() > 0) {
                // TODO UI scelta del giocatore su uso delle batterie
                // ipotizzo lo voglia fare
                player.getShip().chargeWeapon(cannon);
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
            // Move player back on flight board (positive flight day cost = move to worse position)
            gameModel.updatePosition(player, flightDaysCost);
            
            System.out.println("[Smugglers] " + player.getNickName() + " defeated smugglers and loaded goods, lost " + 
                             flightDaysCost + " flight days");
        }
        // If player doesn't accept reward, nothing happens to their position
    }

    /**
     * Load goods from a planet onto a player's ship
     * @param player The player loading goods
     */
    private void loadGoods(Player player) {
        // Implementation of how goods are tracked
        List<Cargo> goodsToLoad = goodsGainedOnVictory;
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
     * @param defeatedPlayers The players who was defeated
     */
    private void applyPenalty(List<Player> defeatedPlayers) {
        if (defeatedPlayers.isEmpty()) {
            return;
        }
        // Take most valuable goods from player
        // If player has no goods, take batteries instead

        for(Player player : defeatedPlayers) {
            removeMostValuableGoodsOrBatteries(player);
        }
    }

    /**
     * Remove the most valuable goods from a player's ship
     * If the player has no goods, remove batteries instead
     * @param player The player to remove goods from
     */
    protected void removeMostValuableGoodsOrBatteries(Player player) {
        List<Cargo> allCargo = player.getShip().getCargoList();
        // Ordina le merci per valore decrescente
        allCargo.sort(Comparator.comparing(Cargo::getValue).reversed());

        //int penaltyCount = goodsLostOnDefeat; // Numero di merci da rimuovere
        int removedCount = 0;


        if(allCargo.isEmpty()) {
            // Se non ci sono merci, rimuovi batterie
            player.getShip().dischargeBatteries(goodsLostOnDefeat);
            return;
        }

        // Se il giocatore ha merci, rimuovi le più preziose
        for (int i = 0; i < allCargo.size() && removedCount < goodsLostOnDefeat; i++) {
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
        int batteriesNeeded = goodsLostOnDefeat - removedCount;
        if (batteriesNeeded > 0) {
            player.getShip().dischargeBatteries(batteriesNeeded);
        }
    }

    // Getters
    public int getSmugglerStrength() {
        return smugglerStrength;
    }
    
    public int getGoodsLostOnDefeat() {
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