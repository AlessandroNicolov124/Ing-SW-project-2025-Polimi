package it.polimi.ingsw;

import it.polimi.ingsw.AdventureCards.*;
import it.polimi.ingsw.Components.Component;

import java.io.Serializable;

/** this class represents a player in the game.
 * It contains the player's ID, nickname, active status, ship, and the card they picked.
 * It also provides methods to manage the player's ship and actions during the game.
 */
public class Player implements Serializable {
    private int id;
    private String nickName;
    private boolean active;
    private ShipBuilder ship;
    private AdventureCard pickedCard; // Carta pescata dal giocatore
    private boolean readyToStart;

    public Player(int id, String nickName, boolean active) {
        this.id = id;
        this.nickName = nickName;
        this.active = active;
        this.ship = new ShipBuilder(7, 5);
        this.readyToStart = false;
    }

    public Player(int id, String nickName, boolean active, ShipBuilder ship) {
        this.id = id;
        this.nickName = nickName;
        this.active = active;
        this.ship = ship;
    }

    public int getId() {
        return id;
    }

    public String getNickName() {
        return nickName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public ShipBuilder getShip() {
        return ship;
    }

    public void setShip(ShipBuilder ship) {
        this.ship = ship;
    }

    public void setReady(boolean ready)
    {
        this.readyToStart = ready;
    }

    public boolean getReady ()
    {
        return this.readyToStart;
    }

    public void takeTurn(){

    }

    /**
     * This method allows the player to pick a card from the adventure deck.
     * It assigns the picked card to the player and applies its effect.
     *
     * @param card The adventure card to be picked by the player.
     */
    public void pickCard(AdventureCard card) {
        if (card != null) {
            this.pickedCard = card; // Assegna la carta pescata al giocatore
            System.out.println(nickName + " ha pescato una carta: " + card.getClass().getSimpleName());

            //  metodo per applicare l'effetto della carta, se necessario
            card.effect(); // Esempio di applicazione dell'effetto della carta
        } else {
            System.out.println("Nessuna carta disponibile da pescare.");
        }
    }

    public AdventureCard getPickedCard() {
        return pickedCard;
    }

    public boolean placeComponent(Component component, Position pos) {
        return ship.placeComponent(component, pos);
    }

    public boolean removeComponentAt(Position position) {
        return ship.removeComponentAt(position);
    }

    // public boolean removeComponent(Component component) {
    //     return ship.removeComponent(component);
    // }

    public boolean validateShip() {
        String errors = ship.validateShip();
        return errors == null;
    }

    public String getShipValidationErrors() {
        return ship.validateShip();
    }


}
