package it.polimi.ingsw;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
//        GameModel gameModel = new GameModel();
//
//        // Aggiungi alcune carte all'adventureDeck
//        gameModel.getAdventureDeck().add(new Planets());
//        gameModel.getAdventureDeck().add(new AbandonedStation());
//
//        Player player1 = new Player(0,"Giocatore 1",true,null);
//        gameModel.getPlayers().add(player1);
//
//        // Il giocatore pesca una carta
//        player1.pickCard(gameModel);
//
//        ShipBuilder shipBuilder = new ShipBuilder();
//
//        // Creazione di componenti
//        Cabin cabin = new Cabin();
//        cabin.setType("Cabina di comando");
//
//        Engine engine = new Engine();
//        engine.setType("Motore a reazione");
//
//        // Aggiunta di componenti alla nave
//        shipBuilder.addComponent(cabin, new Position(0, 0));
//        shipBuilder.addComponent(engine, new Position(1, 0));
//
//        // Validazione della nave
//        if (shipBuilder.validateShip()) {
//            System.out.println("La nave è assemblata correttamente.");
//        } else {
//            System.out.println("La nave non è assemblata correttamente.");
//        }
//
//        // Rimozione di un componente
//        shipBuilder.removeComponent(engine);
        GameController gameController = new GameController();


    }
}
