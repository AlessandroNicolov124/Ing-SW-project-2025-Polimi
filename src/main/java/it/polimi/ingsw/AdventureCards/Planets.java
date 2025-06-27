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

    public List<Planet> getPlanets() {
        return planets;
    }

    public int getFlightDaysCost() {
        return flightDaysCost;
    }

    public Map<Planet, Player> getLandedPlayers() {
        return landedPlayers;
    }

    
    /**
     * Create default planets for the card
     * @return List of default planets
     */
    private List<Planet> createDefaultPlanets() {
        List<Planet> defaultPlanets = new ArrayList<>();

        List<Cargo> defaultCargo1 = new ArrayList<>();
        defaultCargo1.add(new BlueCargo());
        defaultCargo1.add(new BlueCargo());
        defaultCargo1.add(new RedCargo());

        List<Cargo> defaultCargo2 = new ArrayList<>();
        defaultCargo2.add(new BlueCargo());
        defaultCargo2.add(new BlueCargo());
        defaultCargo2.add(new GreenCargo());
        defaultCargo2.add(new GreenCargo());

        List<Cargo> defaultCargo3 = new ArrayList<>();
        defaultCargo3.add(new YellowCargo());

        defaultPlanets.add(new Planet("Alpha", defaultCargo1)); // Name and goods count
        defaultPlanets.add(new Planet("Beta", defaultCargo2));
        defaultPlanets.add(new Planet("Gamma", defaultCargo3));
        return defaultPlanets;
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
               "The leader chooses first, followed by others in order. Only one ship per planet allowed.";
    }
    
    /**
     * Allow players to land on planets in order
     * @param gameModel The current game state
     */
    public void processPlanets(GameModel gameModel) {
        List<Player> playersInOrder = gameModel.getFlightBoard().getPlayersInFlightOrder();
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
        Collections.sort(landedPlayersList, (p1, p2) ->{
            int pos1 = gameModel.getFlightBoard().getPosition(p1);
            int pos2 = gameModel.getFlightBoard().getPosition(p2);

            // Ordina dal più indietro al più avanti (ordine crescente delle posizioni)
            return Integer.compare(pos1, pos2);
        });
        
        for (Player player : landedPlayersList) {
            // Move player back on flight board
            gameModel.updatePosition(player, -flightDaysCost);
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
        Random random = new Random();
        int randomIndex = random.nextInt(availablePlanets.size());
        return availablePlanets.get(randomIndex);
    }
    
    /**
     * Load goods from a planet onto a player's ship
     * @param player The player loading goods
     * @param planet The planet providing goods
     */
    protected void loadGoods(Player player, Planet planet) {
        // Implementation of how goods are tracked
        // placeholder
        List<Cargo> goodsToLoad = planet.getGoodsToLoad();
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

    public static class Planet implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private List<Cargo> goodsToLoad;
        
        public Planet(String name, List<Cargo> goodsToLoad) {
            this.name = name;
            this.goodsToLoad = goodsToLoad;
        }
        
        public String getName() {
            return name;
        }
        
        public List<Cargo> getGoodsToLoad() {
            return goodsToLoad;
        }
    }
}