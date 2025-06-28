package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.*;
import it.polimi.ingsw.Components.*;
import java.util.*;

/**
 * Stardust

 * Yellow cards are special events. In your learning flight, the only special event is Stardust.
 * Every player loses 1 flight day for every exposed connector.
 * (Each exposed connector only counts once, regardless of whether it is one pipe, two pipes, or universal.) In reverse
 * order, starting with the last player, each player counts exposed connectors and moves back that many empty spaces.
 */

public class Stardust extends AdventureCard {

    public Stardust() {
        super();
    }
    
    public Stardust(int copies, int duration) {
        super(copies, duration);
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
        applyStardust(gameModel);
    }

    @Override
    public String getDescription() {
        return "Stardust event! Every player loses 1 flight day for each exposed connector on their ship. " +
               "Players move backward in reverse order (last player moves first).";
    }
    
    /**
     * Apply stardust effects to all players
     * @param gameModel The current game state
     */
    public void applyStardust(GameModel gameModel) {
        // Calculate flight day costs for all players
        Map<Player, Integer> playerFlightDayCosts = new HashMap<>();
        
        for (Player player : gameModel.getPlayers()) {
            if (player.isActive()) {
                // Count exposed connectors
                int exposedConnectors = countExposedConnectors(player.getShip());
                
                // Each exposed connector costs 1 flight day
                if (exposedConnectors > 0) {
                    playerFlightDayCosts.put(player, exposedConnectors);
                    System.out.println("[Stardust] " + player.getNickName() + " has " + exposedConnectors + 
                                     " exposed connectors, loses " + exposedConnectors + " flight days");
                } else {
                    System.out.println("[Stardust] " + player.getNickName() + " has no exposed connectors");
                }
            }
        }
        
        // Apply flight day costs in reverse order (worst position player moves first)
        if (!playerFlightDayCosts.isEmpty()) {
            applyFlightDayCostsInReverseOrder(gameModel, playerFlightDayCosts);
        }
    }
    
    /**
     * Get players in reverse order (last player first)
     * @param gameModel The current game state
     * @return Ordered list of players starting with the last player
     */
    protected List<Player> getPlayersInReverseOrder(GameModel gameModel) {
        List<Player> players = new ArrayList<>(gameModel.getFlightBoard().getPlayersInFlightOrder());
        Collections.reverse(players);
        return players;
    }

    /**
     * Conta i connettori esposti nella nave
     * Un connettore è considerato "esposto" in due casi:
     * Quando non c'è alcun componente adiacente nella direzione del connettore
     * Quando c'è un componente adiacente, ma non ha un connettore nella direzione opposta
     * @param ship il costruttore della nave contenente i componenti e le loro posizioni
     * @return il numero di connettori esposti
     */
    public int countExposedConnectors(ShipBuilder ship) {
        int exposedConnectors = 0;
        Map<Component, Position> components = ship.getComponents();

        // Per ogni componente nella nave
        for (Map.Entry<Component, Position> entry : components.entrySet()) {
            Component component = entry.getKey();
            Position componentPosition = entry.getValue();

            //System.out.println("    Checking component at " + componentPosition + " of type " + component.getClass().getSimpleName());
            // Per ogni connettore nel componente
            for (Connector connector : component.getConnections()) {

                switch(connector.getConnectorType()) {
                    case NullConnectorType NULL:
                        //System.out.println("    Connector is NullConnectorType, skipping...");
                        // I connettori nulli non sono considerati esposti
                        continue;
                    default:
                        //System.out.println("    Connector is of type " + connector.getConnectorType().getClass().getSimpleName());
                        // Altri tipi di connettori sono considerati esposti se non hanno un componente adiacente compatibile

                        Direction connectorDirection = connector.getDirection();

                        // Calcola la posizione verso cui punta il connettore
                        Position adjacentPosition = connectorDirection.getAdjacentPosition(componentPosition);
                        //System.out.println("Checking connector in direction " + connectorDirection + " (adjacent position: " + adjacentPosition + ")");

                        // Verifica se c'è un componente adiacente in quella posizione
                        Component adjacentComponent = ship.getComponentAt(adjacentPosition);

                        if (adjacentComponent == null) {
                            // Non c'è un componente adiacente, quindi il connettore è esposto
                            exposedConnectors++;
                        } else {
                            // C'è un componente adiacente, verifica se ha un connettore compatibile
                            boolean isConnected = false;
                            Direction oppositeDirection = connectorDirection.getOpposite();

                            if(adjacentComponent.getConnectorByDirection(oppositeDirection).isCompatibleWith(connector)){
                                isConnected = true;
                            }

                            // Se non c'è un connettore compatibile, il connettore è esposto
                            if (!isConnected) {
                                exposedConnectors++;
                            }
                        }
                        break;

                }


            }
        }

        return exposedConnectors;
    }
}