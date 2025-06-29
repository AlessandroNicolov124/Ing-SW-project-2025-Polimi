package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.Components.*;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.GameModel;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
    
    /**
     * Interface for handling planet selection callbacks
     */
    public interface PlanetSelectionCallback {
        /**
         * Request a planet selection from a player
         * @param player The player making the selection
         * @param availablePlanets List of available planets
         * @param flightDaysCost Cost in flight days for landing
         */
        void requestPlanetSelection(Player player, List<Planet> availablePlanets, int flightDaysCost);
        
        /**
         * Check if all players have made their selections
         * @return true if all selections are complete
         */
        boolean allSelectionsComplete();
        
        /**
         * Get the selection made by a specific player
         * @param player The player whose selection to get
         * @return Planet selected by the player, or null if no selection made
         */
        Planet getPlayerSelection(Player player);
    }
    
    /**
     * Enumeration of different planet card variations
     */
    public enum PlanetVariation {
        // Level 1 Deck Variations
        LEVEL1_VARIATION_1, // 2 flight days, 3 planets
        LEVEL1_VARIATION_2, // 3 flight days, 4 planets  
        LEVEL1_VARIATION_3, // 3 flight days, 2 planets
        LEVEL1_VARIATION_4, // 1 flight day, 3 planets
        
        // Level 2 Deck Variations  
        LEVEL2_VARIATION_1, // 3 flight days, 2 planets
        LEVEL2_VARIATION_2, // 4 flight days, 3 planets  
        LEVEL2_VARIATION_3, // 3 flight days, 3 planets
        LEVEL2_VARIATION_4  // 2 flight days, 4 planets
    }
    
    private List<Planet> planets;
    private int flightDaysCost;
    private PlanetVariation variation;
    private transient Map<Planet, Player> landedPlayers;
    private transient Map<Player, Planet> playerSelections;
    private transient PlanetSelectionCallback selectionCallback;

    public Planets(){
        super();
        this.variation = PlanetVariation.LEVEL1_VARIATION_1; // Default variation
        initializePlanetVariation(this.variation);
        this.landedPlayers = new HashMap<>();
        this.playerSelections = new HashMap<>();
    }

    public Planets(int copies, int duration, PlanetVariation variation) {
        super(copies, duration);
        this.variation = variation;
        initializePlanetVariation(variation);
        this.landedPlayers = new HashMap<>();
        this.playerSelections = new HashMap<>();
    }

    /**
     * Set the callback for handling planet selections
     */
    public void setSelectionCallback(PlanetSelectionCallback callback) {
        this.selectionCallback = callback;
    }

    /**
     * Handle a player's planet selection
     * @param player The player making the selection
     * @param planetIndex Index of the chosen planet (-1 if no planet chosen)
     * @return true if selection was valid and processed
     */
    public boolean handlePlayerSelection(Player player, int planetIndex) {
        if (planetIndex == -1) {
            // Player chooses not to land
            playerSelections.put(player, null);
            System.out.println("[Planets] " + player.getNickName() + " chose not to land on any planet");
            return true;
        }
        
        if (planetIndex < 0 || planetIndex >= planets.size()) {
            System.out.println("[Planets] Invalid planet index " + planetIndex + " for player " + player.getNickName());
            return false;
        }
        
        Planet chosenPlanet = planets.get(planetIndex);
        
        // Check if planet is already taken
        if (landedPlayers.containsKey(chosenPlanet)) {
            System.out.println("[Planets] Planet " + chosenPlanet.getName() + " is already occupied");
            return false;
        }
        
        // Record the selection
        playerSelections.put(player, chosenPlanet);
        landedPlayers.put(chosenPlanet, player);
        
        System.out.println("[Planets] " + player.getNickName() + " selected planet: " + chosenPlanet.getName());
        return true;
    }

    /**
     * Get the name of a planet by its index
     * @param planetIndex Index of the planet
     * @return Name of the planet, or null if index is invalid
     */
    public String getPlanetNameByIndex(int planetIndex) {
        if (planetIndex < 0 || planetIndex >= planets.size()) {
            return null;
        }
        return planets.get(planetIndex).getName();
    }

    /**
     * Initialize planets and flight day cost based on the specified variation
     */
    private void initializePlanetVariation(PlanetVariation variation) {
        switch (variation) {
            // Level 1 Deck Variations
            case LEVEL1_VARIATION_1:
                this.planets = createLevel1Variation1Planets();
                this.flightDaysCost = 2;
                break;
            case LEVEL1_VARIATION_2:
                this.planets = createLevel1Variation2Planets();
                this.flightDaysCost = 3;
                break;
            case LEVEL1_VARIATION_3:
                this.planets = createLevel1Variation3Planets();
                this.flightDaysCost = 3;
                break;
            case LEVEL1_VARIATION_4:
                this.planets = createLevel1Variation4Planets();
                this.flightDaysCost = 1;
                break;
                
            // Level 2 Deck Variations
            case LEVEL2_VARIATION_1:
                this.planets = createLevel2Variation1Planets();
                this.flightDaysCost = 3;
                break;
            case LEVEL2_VARIATION_2:
                this.planets = createLevel2Variation2Planets();
                this.flightDaysCost = 4;
                break;
            case LEVEL2_VARIATION_3:
                this.planets = createLevel2Variation3Planets();
                this.flightDaysCost = 3;
                break;
            case LEVEL2_VARIATION_4:
                this.planets = createLevel2Variation4Planets();
                this.flightDaysCost = 2;
                break;
        }
    }

    /**
     * Create planets for Level 1 Variation 1 (2 flight days, 3 planets)
     */
    private List<Planet> createLevel1Variation1Planets() {
        List<Planet> planetsVar1 = new ArrayList<>();

        // Planet 1: Red cargo only
        List<Cargo> cargo1 = Arrays.asList(new RedCargo(), new RedCargo());
        planetsVar1.add(new Planet("Red Planet", cargo1));

        // Planet 2: Red and Blue cargo
        List<Cargo> cargo2 = Arrays.asList(new RedCargo(), new BlueCargo(), new BlueCargo());
        planetsVar1.add(new Planet("Blue Planet", cargo2));

        // Planet 3: Yellow cargo only
        List<Cargo> cargo3 = Arrays.asList(new YellowCargo());
        planetsVar1.add(new Planet("Yellow Planet", cargo3));

        return planetsVar1;
    }

    /**
     * Create planets for Level 1 Variation 2 (3 flight days, 4 planets)
     */
    private List<Planet> createLevel1Variation2Planets() {
        List<Planet> planetsVar2 = new ArrayList<>();

        // Planet 1: Red, Green, Blue cargo
        List<Cargo> cargo1 = Arrays.asList(new RedCargo(), new GreenCargo(), new BlueCargo(), new BlueCargo(), new BlueCargo());
        planetsVar2.add(new Planet("Green-Orange-Blue Planet", cargo1));

        // Planet 2: Red, Yellow, Blue cargo  
        List<Cargo> cargo2 = Arrays.asList(new RedCargo(), new YellowCargo(), new BlueCargo(), new BlueCargo());
        planetsVar2.add(new Planet("Orange-Blue Planet", cargo2));

        // Planet 3: Red and Blue cargo
        List<Cargo> cargo3 = Arrays.asList(new RedCargo(), new BlueCargo(), new BlueCargo(), new BlueCargo(), new BlueCargo());
        planetsVar2.add(new Planet("Blue Planet", cargo3));

        // Planet 4: Red and Green cargo
        List<Cargo> cargo4 = Arrays.asList(new RedCargo(), new GreenCargo());
        planetsVar2.add(new Planet("Green Planet", cargo4));

        return planetsVar2;
    }

    /**
     * Create planets for Level 1 Variation 3 (3 flight days, 2 planets)
     */
    private List<Planet> createLevel1Variation3Planets() {
        List<Planet> planetsVar3 = new ArrayList<>();

        // Planet 1: Yellow, Green, Blue cargo
        List<Cargo> cargo1 = Arrays.asList(new YellowCargo(), new GreenCargo(), new BlueCargo(), new BlueCargo());
        planetsVar3.add(new Planet("Yellow-Purple Planet", cargo1));

        // Planet 2: Yellow cargo only
        List<Cargo> cargo2 = Arrays.asList(new YellowCargo(), new YellowCargo());
        planetsVar3.add(new Planet("Yellow Planet", cargo2));

        return planetsVar3;
    }

    /**
     * Create planets for Level 1 Variation 4 (1 flight day, 3 planets)
     */
    private List<Planet> createLevel1Variation4Planets() {
        List<Planet> planetsVar4 = new ArrayList<>();

        // Planet 1: Green cargo only
        List<Cargo> cargo1 = Arrays.asList(new GreenCargo(), new GreenCargo());
        planetsVar4.add(new Planet("Green Planet", cargo1));

        // Planet 2: Yellow cargo only
        List<Cargo> cargo2 = Arrays.asList(new YellowCargo());
        planetsVar4.add(new Planet("Yellow Planet", cargo2));

        // Planet 3: Blue cargo only
        List<Cargo> cargo3 = Arrays.asList(new BlueCargo(), new BlueCargo(), new BlueCargo());
        planetsVar4.add(new Planet("Blue Planet", cargo3));

        return planetsVar4;
    }

    /**
     * Create planets for Level 2 Variation 1 (3 flight days, 2 planets)
     */
    private List<Planet> createLevel2Variation1Planets() {
        List<Planet> planetsVar1 = new ArrayList<>();

        // Planet 1: Red cargo only
        List<Cargo> cargo1 = Arrays.asList(new RedCargo(), new RedCargo());
        planetsVar1.add(new Planet("Red Planet", cargo1));

        // Planet 2: Green cargo only
        List<Cargo> cargo2 = Arrays.asList(new GreenCargo(), new GreenCargo(), new GreenCargo(), new GreenCargo());
        planetsVar1.add(new Planet("Green Planet", cargo2));

        return planetsVar1;
    }

    /**
     * Create planets for Level 2 Variation 2 (4 flight days, 3 planets)
     */
    private List<Planet> createLevel2Variation2Planets() {
        List<Planet> planetsVar2 = new ArrayList<>();

        // Planet 1: Red and Yellow cargo
        List<Cargo> cargo1 = Arrays.asList(new RedCargo(), new RedCargo(), new RedCargo(), new YellowCargo());
        planetsVar2.add(new Planet("Red Planet", cargo1));

        // Planet 2: Red and Green cargo
        List<Cargo> cargo2 = Arrays.asList(new RedCargo(), new RedCargo(), new GreenCargo(), new GreenCargo());
        planetsVar2.add(new Planet("Green Planet", cargo2));

        // Planet 3: Red and Blue cargo
        List<Cargo> cargo3 = Arrays.asList(new RedCargo(), new BlueCargo(), new BlueCargo(), new BlueCargo(), new BlueCargo());
        planetsVar2.add(new Planet("Blue Planet", cargo3));

        return planetsVar2;
    }

    /**
     * Create planets for Level 2 Variation 3 (3 flight days, 3 planets)
     */
    private List<Planet> createLevel2Variation3Planets() {
        List<Planet> planetsVar3 = new ArrayList<>();

        // Planet 1: Green cargo only
        List<Cargo> cargo1 = Arrays.asList(new GreenCargo(), new GreenCargo(), new GreenCargo(), new GreenCargo());
        planetsVar3.add(new Planet("Green Planet", cargo1));

        // Planet 2: Yellow cargo only
        List<Cargo> cargo2 = Arrays.asList(new YellowCargo(), new YellowCargo());
        planetsVar3.add(new Planet("Yellow Planet", cargo2));

        // Planet 3: Blue cargo only
        List<Cargo> cargo3 = Arrays.asList(new BlueCargo(), new BlueCargo(), new BlueCargo(), new BlueCargo());
        planetsVar3.add(new Planet("Blue Planet", cargo3));

        return planetsVar3;
    }

    /**
     * Create planets for Level 2 Variation 4 (2 flight days, 4 planets)
     */
    private List<Planet> createLevel2Variation4Planets() {
        List<Planet> planetsVar4 = new ArrayList<>();

        // Planet 1: Red and Yellow cargo
        List<Cargo> cargo1 = Arrays.asList(new RedCargo(), new YellowCargo());
        planetsVar4.add(new Planet("Red Planet", cargo1));

        // Planet 2: Yellow and Blue cargo
        List<Cargo> cargo2 = Arrays.asList(new YellowCargo(), new GreenCargo(), new BlueCargo());
        planetsVar4.add(new Planet("Yellow-Blue Planet", cargo2));

        // Planet 3: Green cargo only
        List<Cargo> cargo3 = Arrays.asList(new GreenCargo(), new GreenCargo());
        planetsVar4.add(new Planet("Green Planet", cargo3));

        // Planet 4: Yellow cargo only
        List<Cargo> cargo4 = Arrays.asList(new YellowCargo());
        planetsVar4.add(new Planet("Yellow Planet", cargo4));

        return planetsVar4;
    }

    public List<Planet> getPlanets() {
        return planets;
    }

    public int getFlightDaysCost() {
        return flightDaysCost;
    }

    public PlanetVariation getVariation() {
        return variation;
    }

    public Map<Planet, Player> getLandedPlayers() {
        return landedPlayers;
    }

    @Override
    protected int additionalPls(){
        return 0;
    }

    @Override
    protected void resolveEvent(Player p) {
        // Individual player resolution would depend on the game mechanics
    }

    @Override
    public void effect(GameModel gameModel){
        // Set up the card for interactive selection
        // The actual processPlanets call will be triggered by the server after setting up the callback
        System.out.println("[Planets] " + variation + " card drawn - preparing for interactive planet selection");
        
        // For now, fall back to the existing processPlanets method
        // In a full implementation, this would be handled by the server triggering 
        // interactive selection through the network layer
        processPlanets(gameModel);
    }

    @Override
    public String getDescription() {
        return "Choose planets to land on and collect goods. Landing costs " + flightDaysCost + " flight days. " +
               "The leader chooses first, followed by others in order. Only one ship per planet allowed. " +
               "Variation: " + variation + " (" + planets.size() + " planets available)";
    }

    @Override
    public boolean isPlanetsCard() {
        return true;
    }
    
    /**
     * Allow players to land on planets in order - now supports interactive selection
     * @param gameModel The current game state
     */
    public void processPlanets(GameModel gameModel) {
        List<Player> playersInOrder = gameModel.getFlightBoard().getPlayersInFlightOrder();
        
        System.out.println("[Planets] Processing " + variation + " with " + planets.size() + " planets, cost: " + flightDaysCost + " flight days");
        
        // Clear previous selections
        playerSelections.clear();
        landedPlayers.clear();
        
        // If we have a callback, use interactive selection
        if (selectionCallback != null) {
            // Request selections from all players in order
            for (Player player : playersInOrder) {
                List<Planet> availablePlanets = getAvailablePlanets();
                if (!availablePlanets.isEmpty()) {
                    selectionCallback.requestPlanetSelection(player, availablePlanets, flightDaysCost);
                }
            }
            
            // Wait for all selections to be made (this would be handled by the server/controller)
            // The actual waiting and processing would be handled by the game controller
            
        } else {
            // Fallback to random selection for testing/non-interactive mode
            processRandomSelections(playersInOrder);
        }
        
        // Process the results once all selections are made
        processLandingResults(gameModel, playersInOrder);
    }
    
    /**
     * Get currently available planets (not yet taken)
     */
    public List<Planet> getAvailablePlanets() {
        List<Planet> available = new ArrayList<>();
        for (Planet planet : planets) {
            if (!landedPlayers.containsKey(planet)) {
                available.add(planet);
            }
        }
        return available;
    }
    
    /**
     * Complete the planet selection process and apply all results
     * This method should be called when all players have made their selections
     */
    public void completePlanetSelection(GameModel gameModel) {
        List<Player> playersInOrder = gameModel.getFlightBoard().getPlayersInFlightOrder();
        processLandingResults(gameModel, playersInOrder);
    }
    
    /**
     * Process landing results after all players have made selections
     */
    private void processLandingResults(GameModel gameModel, List<Player> playersInOrder) {
        List<Player> landedPlayersList = new ArrayList<>();
        
        // Process all player selections
        for (Player player : playersInOrder) {
            Planet chosenPlanet = playerSelections.get(player);
            
            if (chosenPlanet != null) {
                landedPlayersList.add(player);
                loadGoods(player, chosenPlanet);
                System.out.println("[Planets] " + player.getNickName() + " landed on planet " + chosenPlanet.getName() + 
                                 " and collected " + chosenPlanet.getGoodsToLoad().size() + " cargo units");
            } else {
                System.out.println("[Planets] " + player.getNickName() + " chose not to land on any planet");
            }
        }
        
        // Second phase: move back players who landed, in back-to-front order
        Collections.sort(landedPlayersList, (p1, p2) ->{
            int pos1 = gameModel.getFlightBoard().getPosition(p1);
            int pos2 = gameModel.getFlightBoard().getPosition(p2);

            // Sort from farthest back to front (ascending order of positions)
            return Integer.compare(pos1, pos2);
        });
        
        for (Player player : landedPlayersList) {
            // Move player back on flight board (positive flight day cost = move to worse position)
            gameModel.updatePosition(player, flightDaysCost);
            System.out.println("[Planets] " + player.getNickName() + " landed on planet, lost " + flightDaysCost + " flight days");
        }
    }
    
    /**
     * Fallback method for random planet selection (used when no callback is set)
     */
    private void processRandomSelections(List<Player> playersInOrder) {
        Random random = new Random();
        
        for (Player player : playersInOrder) {
            List<Planet> availablePlanets = getAvailablePlanets();
            
            if (availablePlanets.isEmpty()) {
                playerSelections.put(player, null);
                continue;
            }
            
            // 70% chance to land if there are available planets
            if (random.nextDouble() < 0.7) {
                // Sort by value and prefer better planets
                availablePlanets.sort((p1, p2) -> {
                    int value1 = p1.getGoodsToLoad().stream().mapToInt(Cargo::getValue).sum();
                    int value2 = p2.getGoodsToLoad().stream().mapToInt(Cargo::getValue).sum();
                    return Integer.compare(value2, value1); // Descending order
                });
                
                Planet chosenPlanet;
                if (random.nextDouble() < 0.6) {
                    chosenPlanet = availablePlanets.get(0); // Best planet
                } else {
                    int randomIndex = random.nextInt(availablePlanets.size());
                    chosenPlanet = availablePlanets.get(randomIndex);
                }
                
                playerSelections.put(player, chosenPlanet);
                landedPlayers.put(chosenPlanet, player);
            } else {
                playerSelections.put(player, null);
            }
        }
    }
    
    /**
     * Load goods from a planet onto a player's ship
     * @param player The player loading goods
     * @param planet The planet providing goods
     */
    protected void loadGoods(Player player, Planet planet) {
        List<Cargo> goodsToLoad = new ArrayList<>(planet.getGoodsToLoad()); // Create mutable copy

        // Priority order: red, yellow, green, blue
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
        
        // Sort available goods
        Collections.sort(goodsToLoad, cargoComparator);

        // Player's cargo holds
        List<CargoHolds> playerCargoHolds = player.getShip().getCargoHolds();
        // Calculate total available space
        int totalAvailableSpace = playerCargoHolds.stream()
                .mapToInt(CargoHolds::getAvailableSpace)
                .sum();

        int loadedCount = 0;
        
        // Try to load goods
        for (Cargo cargo : goodsToLoad) {
            // If no more space, break
            if (totalAvailableSpace <= 0) {
                break;
            }

            boolean loaded = false;

            // Special handling for red cargo
            if (cargo.isRed()) {
                // First try special red cargo holds
                for (CargoHolds cargoHold : playerCargoHolds) {
                    switch (cargoHold) {
                        case SpecialCargoHolds c:
                            if (!cargoHold.isFull() && cargoHold.canContainCargo(cargo)) {
                                cargoHold.loadCargo(cargo);
                                loaded = true;
                                totalAvailableSpace--;
                                loadedCount++;
                            }
                            break;
                        default:
                            break;
                    }
                    if (loaded) break;
                }

                // If not loaded in special holds, try normal holds
                if (!loaded) {
                    for (CargoHolds cargoHold : playerCargoHolds) {
                        if (!cargoHold.isFull() && cargoHold.canContainCargo(cargo)) {
                            cargoHold.loadCargo(cargo);
                            loaded = true;
                            totalAvailableSpace--;
                            loadedCount++;
                            break;
                        }
                    }
                }
            }
            // For all other colors
            else {
                // Try to load in appropriate cargo holds
                for (CargoHolds cargoHold : playerCargoHolds) {
                    if (!cargoHold.isFull() && cargoHold.canContainCargo(cargo)) {
                        cargoHold.loadCargo(cargo);
                        loaded = true;
                        totalAvailableSpace--;
                        loadedCount++;
                        break;
                    }
                }

                // If not loaded in appropriate holds, load in any available hold
                if (!loaded) {
                    for (CargoHolds cargoHold : playerCargoHolds) {
                        if (!cargoHold.isFull()) {
                            cargoHold.loadCargo(cargo);
                            loaded = true;
                            totalAvailableSpace--;
                            loadedCount++;
                            break;
                        }
                    }
                }
            }
        }
        
        System.out.println("[Planets] " + player.getNickName() + " loaded " + loadedCount + " out of " + 
                          goodsToLoad.size() + " available cargo units from planet " + planet.getName());
    }

    /**
     * Factory method to create a random planets card variation
     */
    public static Planets createRandomVariation() {
        PlanetVariation[] variations = PlanetVariation.values();
        Random random = new Random();
        PlanetVariation randomVariation = variations[random.nextInt(variations.length)];
        return new Planets(1, 1, randomVariation);
    }

    public static class Planet implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private List<Cargo> goodsToLoad;
        
        public Planet(String name, List<Cargo> goodsToLoad) {
            this.name = name;
            this.goodsToLoad = new ArrayList<>(goodsToLoad); // Make defensive copy
        }
        
        public String getName() {
            return name;
        }
        
        public List<Cargo> getGoodsToLoad() {
            return new ArrayList<>(goodsToLoad); // Return defensive copy
        }
        
        public int getTotalCargoValue() {
            return goodsToLoad.stream().mapToInt(Cargo::getValue).sum();
        }
        
        public int getCargoCount() {
            return goodsToLoad.size();
        }
        
        @Override
        public String toString() {
            return name + " (cargo: " + getCargoCount() + " units, value: " + getTotalCargoValue() + ")";
        }
    }
}