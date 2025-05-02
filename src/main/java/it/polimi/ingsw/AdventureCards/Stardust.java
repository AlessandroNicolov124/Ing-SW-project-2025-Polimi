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
     * Apply stardust effects to all players
     * @param gameModel The current game state
     */
    public void applyStardust(GameModel gameModel) {
        List<Player> playersInReverseOrder = getPlayersInReverseOrder(gameModel);
        
        for (Player player : playersInReverseOrder) {
            // Count exposed connectors
            //int exposedConnectors = countExposedConnectors(player);
            int exposedConnectors = countExposedConnectors(player.getShip());

            // Move player back on flight board
            if (exposedConnectors > 0) {
                gameModel.updatePosition(player, -exposedConnectors);
            }
        }
    }
    
    /**
     * Get players in reverse order (last player first)
     * @param gameModel The current game state
     * @return Ordered list of players starting with the last player
     */
    private List<Player> getPlayersInReverseOrder(GameModel gameModel) {
//        List<Player> players = new ArrayList<>(gameModel.getPlayers());
//
//        // Sort players by position on flight board (reverse order)
//        Collections.sort(players, (p1, p2) -> {
//            // This would need to compare player positions on the flight board
//            // For now, just reverse the current order
//            return 0;
//        });

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

            // Per ogni connettore nel componente
            for (Connector connector : component.getConnections()) {
                Direction connectorDirection = connector.getDirection();

                // Calcola la posizione verso cui punta il connettore
                Position adjacentPosition = connectorDirection.getAdjacentPosition(componentPosition);

                // Verifica se c'è un componente adiacente in quella posizione
                Component adjacentComponent = ship.getComponentAt(adjacentPosition);

                if (adjacentComponent == null) {
                    // Non c'è un componente adiacente, quindi il connettore è esposto
                    exposedConnectors++;
                } else {
                    // C'è un componente adiacente, verifichiamo se ha un connettore compatibile
                    boolean isConnected = false;
                    Direction oppositeDirection = connectorDirection.getOpposite();

                    for (Connector adjacentConnector : adjacentComponent.getConnections()) {
                        if (adjacentConnector.getDirection() == oppositeDirection) {
                            isConnected = true;
                            break;
                        }
                    }

                    // Se non c'è un connettore complementare, il connettore è esposto
                    if (!isConnected) {
                        exposedConnectors++;
                    }
                }
            }
        }

        return exposedConnectors;
    }
    
    /**
     * Count the number of exposed connectors on a player's ship
     * @param player The player
     * @return Number of exposed connectors
     */
    private int countExposedConnectors(Player player) {
        ShipBuilder ship = player.getShip();
        List<Component> components = ship.getAllComponents();
        Set<Connector> exposedConnectors = new HashSet<>();
        
        // Identify exposed connectors
        for (Component component : components) {
            List<Connector> conn = component.getConnections();
            for (Connector connector : conn) {
                if (isExposed(connector, ship)) {
                    exposedConnectors.add(connector);
                }
            }
//            if (component instanceof Connector) {
//                Connector connector = (Connector) component;
//                if (isExposed(connector, ship)) {
//                    exposedConnectors.add(connector);
//                }
//            }
        }
        
        return exposedConnectors.size();
    }
    
    /**
     * Check if a connector is exposed (not connected to another component)
     * @param connector The connector to check
     * @param ship The player's ship
     * @return true if the connector is exposed, false otherwise
     */
    private boolean isExposed(Connector connector, ShipBuilder ship) {
        // This would need to check if the connector is exposed
        // For now, return a placeholder implementation
        
        // Get position of the connector
        // Position connectorPosition = ship.getPositionOf(connector);
        
        // Check if all connector endpoints are connected to other components
        // Depends on how connectors and connections are implemented
        // For example, checking adjacent positions for other components
        
        return false;
    }
}