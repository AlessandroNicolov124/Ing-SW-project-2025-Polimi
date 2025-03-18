package it.polimi.ingsw;

public class GameController {
    private GameModel gameModel;
    private GameView gameView;

    public GameController() {
        gameModel = new GameModel();
    }
    public void startGame(){

    }

    public void addPlayer(Player player){
        gameModel.getPlayers().add(player);
    }


    public void addComponentForPlayer(Player player, Component component, Position pos) {
        int index = gameModel.getPlayers().indexOf(player);
        if (index >= 0) {
            player.getShip().addComponent(component, pos);
        }
    }

    public void updateView(){}



}
