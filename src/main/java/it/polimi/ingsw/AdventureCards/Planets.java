package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.Components.*;
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
        LEVEL2_VARIATION_1, // 3 flight days, 3 planets
        LEVEL2_VARIATION_2, // 4 flight days, 3 planets  
        LEVEL2_VARIATION_3, // 3 flight days, 3 planets
        LEVEL2_VARIATION_4  // 2 flight days, 4 planets
    }
    
    private List<Planet> planets;
    private int flightDaysCost;
    private PlanetVariation variation;
    private transient Map<Planet, Player> landedPlayers;

    public Planets(){
        super();
        this.variation = PlanetVariation.LEVEL1_VARIATION_1; // Default variation
        initializePlanetVariation(this.variation);
        this.landedPlayers = new HashMap<>();
    }

    public Planets(int copies, int duration, PlanetVariation variation) {
        super(copies, duration);
        this.variation = variation;
        initializePlanetVariation(variation);
        this.landedPlayers = new HashMap<>();
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
        processPlanets(gameModel);
    }

    @Override
    public String getDescription() {
        return "Choose planets to land on and collect goods. Landing costs " + flightDaysCost + " flight days. " +
               "The leader chooses first, followed by others in order. Only one ship per planet allowed. " +
               "Variation: " + variation + " (" + planets.size() + " planets available)";
    }
    
    /**
     * Allow players to land on planets in order
     * @param gameModel The current game state
     */
    public void processPlanets(GameModel gameModel) {
        List<Player> playersInOrder = gameModel.getFlightBoard().getPlayersInFlightOrder();
        List<Player> landedPlayersList = new ArrayList<>();
        
        System.out.println("[Planets] Processing " + variation + " with " + planets.size() + " planets, cost: " + flightDaysCost + " flight days");
        
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

            // Ordina dal più indietro al più avanti (ordine crescente delle posizioni)
            return Integer.compare(pos1, pos2);
        });
        
        for (Player player : landedPlayersList) {
            // Move player back on flight board (positive flight day cost = move to worse position)
            gameModel.updatePosition(player, flightDaysCost);
            System.out.println("[Planets] " + player.getNickName() + " landed on planet, lost " + flightDaysCost + " flight days");
        }
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
        
        // TODO This is a placeholder - actual implementation would involve player choice
        // For now, make players more likely to land on valuable planets
        Random random = new Random();
        
        // Players have a 70% chance to land if there are valuable planets available
        if (random.nextDouble() < 0.7) {
            // Prefer planets with more valuable cargo (higher total value)
            availablePlanets.sort((p1, p2) -> {
                int value1 = p1.getGoodsToLoad().stream().mapToInt(Cargo::getValue).sum();
                int value2 = p2.getGoodsToLoad().stream().mapToInt(Cargo::getValue).sum();
                return Integer.compare(value2, value1); // Descending order
            });
            
            // 60% chance to pick the best planet, 40% chance to pick randomly
            if (random.nextDouble() < 0.6) {
                return availablePlanets.get(0); // Best planet
            } else {
                int randomIndex = random.nextInt(availablePlanets.size());
                return availablePlanets.get(randomIndex);
            }
        }
        
        return null; // Player chooses not to land
    }
    
    /**
     * Load goods from a planet onto a player's ship
     * @param player The player loading goods
     * @param planet The planet providing goods
     */
    protected void loadGoods(Player player, Planet planet) {
        // Implementation of how goods are tracked
        // placeholder
        List<Cargo> goodsToLoad = new ArrayList<>(planet.getGoodsToLoad()); // Create mutable copy
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
        Collections.sort(goodsToLoad, cargoComparator);

        // stive del giocatore
        List<CargoHolds> playerCargoHolds = player.getShip().getCargoHolds();
        // Calcola lo spazio totale disponibile
        int totalAvailableSpace = playerCargoHolds.stream()
                .mapToInt(CargoHolds::getAvailableSpace)
                .sum();

        int loadedCount = 0;
        
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
                                loadedCount++;
                            }
                            break;
                        default:
                            break;
                    }
                    if (loaded) break;
                }

                // Se non caricato nelle speciali, prova nelle normali
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
            // Per tutti gli altri colori
            else {
                // Prova a caricare nelle stive che possono contenere quel tipo di merce
                for (CargoHolds cargoHold : playerCargoHolds) {
                    if (!cargoHold.isFull() && cargoHold.canContainCargo(cargo)) {
                        cargoHold.loadCargo(cargo);
                        loaded = true;
                        totalAvailableSpace--;
                        loadedCount++;
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