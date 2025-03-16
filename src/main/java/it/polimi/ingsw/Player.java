package it.polimi.ingsw;

import javax.swing.*;
import java.util.List;

public class Player {
    private int id;
    private String nickName;
    private boolean active;
    private ShipBuilder ship;
    private AdventureCard pickedCard; // Carta pescata dal giocatore

    public Player() {}

    public Player(int id, String nickName, boolean active, ShipBuilder ship) {
        this.id = id;
        this.nickName = nickName;
        this.active = active;
        this.ship = ship;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
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

    public void assembleShip(){

    }

    public void takeTurn(){

    }

    public void pickCard(GameModel gameModel) {
        AdventureCard card = gameModel.pickAdventureCard(); // Pescare una carta dal GameModel
        if (card != null) {
            this.pickedCard = card; // Assegna la carta pescata al giocatore
            System.out.println(nickName + " ha pescato una carta: " + card.getClass().getSimpleName());


            //  metodo per applicare l'effetto della carta, se necessario
            card.effect(); // Esempio di applicazione dell'effetto della carta
        } else {
            System.out.println("Nessuna carta disponibile da pescare.");
        }
    }
}
