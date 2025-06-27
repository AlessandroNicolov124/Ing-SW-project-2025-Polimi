package it.polimi.ingsw.AdventureCards;

import it.polimi.ingsw.Components.*;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.Direction;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.GameModel;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Meteor Swarm

 * A Meteor Swarm can really mess with your paint job. The card depicts several large and/or small meteors and the
 * directions they come from. Deal with meteors one at a time, top to bottom. They affect all players simultaneously.

 * For each meteor, the leader rolls two dice. The roll determines which row or column the meteor can impact.
 * The rows and columns are numbered on the edge of your ship board. Each player receives their own personal meteor and
 * checks to see if it hits or misses their ship. If the roll does not miss, proceed as follows:

 * A small meteor will harmlessly bounce off of a well-built ship. It is only a problem if it hits an exposed connector.
 * In this case, you can still avoid damage by powering up a shield if you have one that protects that side.
 * You must pay 1 battery token to do this. If you can't or won't avoid the impact, the component the meteor hits is
 * destroyed. Remove it from your ship and add it to your pile of components lost along the way.

 * A large meteor is, of course, even more dangerous. It will damage even a well-built ship, and shields cannot stop it.
 * Your only hope is to blast it. You can only shoot it if you have a cannon pointed at it in the same column.
 * If it is a double cannon, you will have to pay 1 battery token to use it. Large meteors tend to come from in front of
 * you, which is why we recommended you pay special attention to cannons pointing forward.
 * If you don't shoot a large meteor, the component it hits is destroyed.
 */

/**
 * Big Meteor Swarm

 * Meteors can come at your ship from the left, the right, or even the rear!
 * Large meteors coming from in front of you can be blown up only by a forward-pointing cannon in the column from which
 * they are approaching, but large meteors from the side or the rear can be blown up by a cannon pointing toward the
 * meteor in the same or adjacent row or column. A reminder is printed on the flight board.
 */

public class MeteorSwarm extends AdventureCard {
    private List<Meteor> meteors;
    private Random random;

    public MeteorSwarm() {
        super();
        this.meteors = createRandomMeteors();
        this.random = new Random();
    }
    
    public MeteorSwarm(int copies, int duration, List<Meteor> meteors) {
        super(copies, duration);
        this.meteors = meteors;
        this.random = new Random();
    }

    /**
     * Create default set of meteors for the swarm
     * @return List of meteors
     */
    private List<Meteor> createRandomMeteors() {
        List<Meteor> defaultMeteors = new ArrayList<>();

        Random random = new Random();
        int numberOfMeteors = random.nextInt(5) + 1; // Random number between 1 and 10 meteors

        for (int i = 0; i < numberOfMeteors; i++) {
            // Randomly select a size
            MeteorSize size = MeteorSize.values()[random.nextInt(MeteorSize.values().length)];
            // Randomly select a direction
            Direction direction = Direction.values()[random.nextInt(Direction.values().length)];
            // Add the meteor with random properties
            defaultMeteors.add(new Meteor(size, direction));
        }
        return defaultMeteors;
    }

    @Override
    protected int additionalPls() {
        return 0;
    }

    @Override
    protected void resolveEvent(Player p) {
        // Process meteor impacts for a single player
    }

    @Override
    public void effect() {
        // This would be called when the card is drawn
    }
    
    /**
     * Process all meteor impacts
     * @param gameModel The GameModel
     */
    public void processMeteorImpacts(GameModel gameModel) {
        for (Meteor meteor : meteors) {
            // Roll dice to determine row/column of potential impact
            int diceRoll = rollDice();

            for(Player player : gameModel.getPlayers()) {
                processMeteorImpactFor(player, meteor, diceRoll);
            }

        }
    }

    protected void processMeteorImpactFor(Player player, Meteor meteor, int diceRoll) {
        // Check if meteor hits the ship
        Position hit = checkMeteorHit(player, meteor.getDirection(), diceRoll);

        if (hit!=null) {
            // Process impact based on meteor size
            if (meteor.getSize() == MeteorSize.SMALL) {
                // Small meteor: check for exposed connector or shield
                if (isExposedConnector(player, meteor.getDirection(), hit)) {
                    // Check for shield
                    ShieldGenerator shieldUsed = canDefendWithShield(player, meteor.getDirection());

                    if (shieldUsed==null) {
                        // Destroy component
                        player.getShip().destroyComponentAt(hit);
                    }else{
                        shieldUsed.discharge();
                    }
                }
            } else {
                // Large meteor: can only be stopped by cannon
                Cannon cannonUsed = useCannon(player, meteor.getDirection(), hit);

                if (cannonUsed == null) {
                    // Destroy component
                    player.getShip().destroyComponentAt(hit);
                }else{
                    //se doppio spara solo se carico e non danneggiato,
                    //se singolo spara solo se non danneggiato
                    cannonUsed.fire();
                }
            }
        }
    }

    //cerco un cannone che possa essere usato dal giocatore per colpire il meteorite grande
    //se il meteorite è grande, e proviene da nord è necessario un cannone che punti a nord e si trovi nella stessa colonna
    //se il meteorite è grande e proviene da est, ovest o sud, è necessario un cannone che punti a est, ovest o sud
    private Cannon useCannon(Player player, Direction direction, Position hit) {
        //Verifico che ci sia un cannone che punti nella direzione
        List<SingleCannon> sc = null;
        List<DoubleCannon> dc = null;

        switch (direction) {
            case NORTH -> {
                // Per meteore da nord, cerco cannone nella stessa colonna
                //c = findCannonInColumn(player, direction, hit.getX());
                sc = player.getShip().getOperationalSingleCannons().stream()
                        .filter(cannon -> cannon.getComponentDirection() == direction && player.getShip().getPositionOf(cannon).getX() == hit.getX())
                        .toList();
                if(!sc.isEmpty()) {
                    System.out.println("Using single cannon at position: " + player.getShip().getPositionOf(sc.getFirst()));
                    return sc.getFirst(); // Prendo il primo cannone singolo che punta a nord
                } else {
                    //cerco un cannone doppio, e lo uso se carico
                    dc = player.getShip().getOperationalDoubleCannons().stream()
                            .filter(cannon -> cannon.getComponentDirection() == direction && player.getShip().getPositionOf(cannon).getX() == hit.getX())
                            .toList();
                    if(!dc.isEmpty()) {
                        System.out.println("Using double cannon at position: " + player.getShip().getPositionOf(dc.getFirst()));

                        // TODO UI scelta del giocatore su uso delle batterie
                        // ipotizzo lo voglia fare
                        if (player.getShip().chargeWeapon(dc.getFirst())) {
                            return dc.getFirst();
                        }
                    }else{
                        System.out.println("No cannon available to shoot at meteor from north.");
                        return null; // Nessun cannone disponibile
                    }
                }
            }
            case SOUTH, EAST, WEST -> {
                // Per meteore da est/ovest/sud, cerco cannone che punti in quella direzione
                //c = findCannonInRow(player, direction, hit.getY());
                sc = player.getShip().getOperationalSingleCannons().stream()
                        .filter(cannon -> cannon.getComponentDirection() == direction)
                        .toList();
                if(!sc.isEmpty()) {
                    return sc.getFirst(); // Prendo il primo cannone singolo che punta a nord
                }else{
                    dc = player.getShip().getOperationalDoubleCannons().stream()
                            .filter(cannon -> cannon.getComponentDirection() == direction)
                            .toList();
                    if(!dc.isEmpty()) {
                        // TODO UI scelta del giocatore su uso delle batterie
                        // ipotizzo lo voglia fare
                        if (player.getShip().chargeWeapon(dc.getFirst())) {
                            return dc.getFirst();
                        }
                    }
                }
            }
            default -> throw new IllegalArgumentException("Invalid direction for cannon use: " + direction);
        }
        return null;
    }

    /**
     * Check if player can and wants to defend with a shield
     * @param player The player attempting to defend
     * @param direction The direction of the attack
     * @return ShieldGenerator activated if defense is successful
     */
    private ShieldGenerator canDefendWithShield(Player player, Direction direction) {
        // Check if player has a shield that can protect against this direction and position
        List<ShieldGenerator> shields = player.getShip().getShieldsProtecting(direction);

        if (!shields.isEmpty() && player.getShip().calculateTotalBatteryCharge() > 0) {
            // Player has shield(s) and batteries - ask if they want to defend

            // TODO UI scelta del giocatore su uso delle batterie
            ShieldGenerator count = null;
            for(ShieldGenerator shield : shields){
                if(player.getShip().activateShield(shield)){
                    count = shield;
                    break;
                }
            }
            return count;
        }

        return null;
    }

    /**
     * Roll dice to determine the row or column of impact
     * @return Dice roll result (2-12)
     */
    private int rollDice() {
        return random.nextInt(6) + random.nextInt(6) + 2; // Simulating dice
    }

    /**
     * Check if meteor hits the player's ship
     * @param player The player whose ship may be hit
     * @param targetCoord The position (row/column) of the potential impact
     * @return Position hit if the fire hits the ship
     */
    private Position checkMeteorHit(Player player, Direction attack, int targetCoord) {
        // 4. Check if there's a component at those coordinates
        Component targetComponent = null;
        Position target = null;

        if(attack == Direction.SOUTH) {
            for(int i=player.getShip().getHeight()-1; i > 0; i--){
                target = new Position(targetCoord, i);
                targetComponent = player.getShip().getComponentAt(target);
                if(targetComponent != null) {
                    break;
                }
            }
        }else if(attack == Direction.NORTH) {
            for(int i=0; i < player.getShip().getHeight(); i++){
                target = new Position(targetCoord, i);
                targetComponent = player.getShip().getComponentAt(target);
                if(targetComponent != null) {
                    break;
                }
            }
        }else if(attack == Direction.WEST) {
            for(int i=0; i < player.getShip().getWidth(); i++){
                target = new Position(i, targetCoord);
                targetComponent = player.getShip().getComponentAt(target);
                if(targetComponent != null) {
                    break;
                }
            }
        }else if(attack == Direction.EAST) {
            for(int i=player.getShip().getWidth()-1; i > 0; i--){
                target = new Position(i, targetCoord);
                targetComponent = player.getShip().getComponentAt(target);
                if(targetComponent != null) {
                    break;
                }
            }
        }

        if (targetComponent == null) {
            System.out.println("The meteor targets an empty space!");
            return null;
        }else{
            System.out.println("The meteor hits the component (" + targetComponent.getClass().getSimpleName() + ")");
        }

        return target;
    }
    
    /**
     * Check if the impacted position is an exposed connector
     * @param player The player whose ship is being checked
     * @param direction The direction the meteor is coming from
     * @param position The position to check
     * @return true if the position contains an exposed connector, false otherwise
     */
    private boolean isExposedConnector(Player player, Direction direction, Position position) {
        // Implementation checks if there's an exposed connector
        return switch(player.getShip().getComponentAt(position).getConnectorByDirection(direction).getConnectorType()){
            case NullConnectorType n -> false;
            default -> true;
        };
    }
    
    /**
     * Inner class to represent a meteor
     */
    public static class Meteor implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private MeteorSize size;
        private Direction direction;
        
        public Meteor(MeteorSize size, Direction direction) {
            this.size = size;
            this.direction = direction;
        }
        
        public MeteorSize getSize() {
            return size;
        }
        
        public Direction getDirection() {
            return direction;
        }
    }
    
    /**
     * Enum to represent meteor sizes
     */
    public enum MeteorSize {
        SMALL,
        LARGE
    }
}