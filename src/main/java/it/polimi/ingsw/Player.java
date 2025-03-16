package it.polimi.ingsw;

import javax.swing.*;
import java.util.List;

public class Player {
    private int id;
    private String nickName;
    private boolean active;
    private List<Cargo> cargoList;
    private ShipBuilder ship;

    public Player() {}

    public Player(int id, String nickName, boolean active, List<Cargo> cargoList, ShipBuilder ship) {
        this.id = id;
        this.nickName = nickName;
        this.active = active;
        this.cargoList = cargoList;
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

    public List<Cargo> getCargoList() {
        return cargoList;
    }

    public void setCargoList(List<Cargo> cargoList) {
        this.cargoList = cargoList;
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

    public void pickCard(){

    }
}
