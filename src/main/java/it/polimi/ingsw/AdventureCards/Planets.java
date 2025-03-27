package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.Player;
import it.polimi.ingsw.GameModel;
import java.util.*;

/**
 * Planets

 * A Planets card has 2 to 4 planets where you can pick up goods. Landing will cost you the number of flight days shown in
 * the lower right corner. If you want to land, mark the planet you choose with your second rocket (the one not on the
 * flight board). Only one rocket is allowed per planet.

 * The leader chooses first, followed by the other players in order. No one is required to land. In fact, players in front
 * sometimes prevent the others from landing at all.

 * Those who landed load the indicated goods on their ships. (See Gaining Goods on page 11.) Goods can be rearranged or
 * discarded at this time. It is legal to land just to block others from landing, but be sure it is worth the loss of
 * flight days.

 * Once everyone has decided whether to land, those who landed move their markers back that many empty spaces, with the
 * player who is farthest behind moving first.
 */

public class Planets extends AdventureCard{
    private List<Planet> planets;
    private int flightDaysCost;
    private Map<Planet, Player> landedPlayers;

    public Planets(){
        super();
        this.planets = createDefaultPlanets();
        this.flightDaysCost = 2;
        this.landedPlayers = new HashMap<>();
    }

    public Planets(int copies, int duration, List<Planet> planets, int flightDaysCost) {
        super(copies, duration);
        this.planets = planets;
        this.flightDaysCost = flightDaysCost;
        this.landedPlayers = new HashMap<>();
    }
    
    /**
     * Create default planets for the card
     * @return List of default planets
     */
    private List<Planet> createDefaultPlanets() {
        List<Planet> defaultPlanets = new ArrayList<>();
        defaultPlanets.add(new Planet("Alpha", 2)); // Name and goods count
        defaultPlanets.add(new Planet("Beta", 3));
        defaultPlanets.add(new Planet("Gamma", 1));
        return defaultPlanets;
    }

    @Override
    int additionalPls(){
        return 0;
    }

    @Override
    void resolveEvent(Player p) {
        // Individual player resolution would depend on the game mechanics
    }

    @Override
    public void effect(){
        // This would be called when the card is drawn
        // The actual implementation depends on game flow and UI
    }
    
    /**
     * Allow players to land on planets in order
     * @param gameModel The current game state
     */
    public void processPlanets(GameModel gameModel) {
        List<Player> playersInOrder = getPlayersInOrder(gameModel);
        List<Player> landedPlayersList = new ArrayList<>();
        
        // First phase: players decide whether to land, in leader-first order
        for (Player player : playersInOrder) {
            // Player chooses whether to land and on which planet
            Planet chosenPlanet = choosePlanet(player, landedPlayersList);
            
            if (chosenPlanet != null) {
                // Player decided to land
                landedPlayers.put(chosenPlanet, player);
                landedPlayersList.add(player);
                
                // Load goods on player's ship
                loadGoods(player, chosenPlanet);
            }
        }
        
        // Second phase: move back players who landed, in back-to-front order
        Collections.sort(landedPlayersList, (p1, p2) -> {
            // This would need to compare player positions on the flight board
            // For now, using the natural order
            return 0;
        });
        
        for (Player player : landedPlayersList) {
            // Move player back on flight board
            gameModel.getFlightBoard().updatePosition(player, -flightDaysCost);
        }
    }
    
    /**
     * Get players in order based on their position on the flight board
     * @param gameModel The current game state
     * @return Ordered list of players starting with the leader
     */
    private List<Player> getPlayersInOrder(GameModel gameModel) {
        // This would need to get players in order based on their position
        // For now, return a copy of the players list
        return new ArrayList<>(gameModel.getPlayers());
    }
    
    /**
     * Let a player choose a planet to land on
     * @param player The player who is choosing
     * @param landedPlayers List of players who have already landed
     * @return The chosen planet, or null if player doesn't want to land
     */
    private Planet choosePlanet(Player player, List<Player> landedPlayers) {
        // This would need to implement player choice logic
        // For now, return a random available planet
        
        List<Planet> availablePlanets = new ArrayList<>();
        
        for (Planet planet : planets) {
            if (!this.landedPlayers.containsKey(planet)) {
                availablePlanets.add(planet);
            }
        }
        
        if (availablePlanets.isEmpty()) {
            return null; // No planets available
        }
        
        // This is a placeholder - actual implementation would involve player choice
        Random random = new Random();
        int randomIndex = random.nextInt(availablePlanets.size());
        return availablePlanets.get(randomIndex);
    }
    
    /**
     * Load goods from a planet onto a player's ship
     * @param player The player loading goods
     * @param planet The planet providing goods
     */
    private void loadGoods(Player player, Planet planet) {
        // Implementation of how goods are tracked
        // placeholder
        int goodsCount = planet.getGoodsCount();
        // Logic to add goods to player's inventory here
    }

    public static class Planet {
        private String name;
        private int goodsCount;
        
        public Planet(String name, int goodsCount) {
            this.name = name;
            this.goodsCount = goodsCount;
        }
        
        public String getName() {
            return name;
        }
        
        public int getGoodsCount() {
            return goodsCount;
        }
    }
}