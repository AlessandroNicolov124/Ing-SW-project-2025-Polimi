package it.polimi.ingsw;

// Classe Engine
public class Cannon extends Component {
    private String type;

    public Cannon() {
        super();
    }

    @Override
    public void connectTo(Component c, Direction dir) {
        connections.add(new Connector(dir, this));
        System.out.println("Motore connesso a " + c.getClass().getSimpleName() + " nella direzione " + dir);
    }

    @Override
    public void disconnect() {
        connections.clear();
        System.out.println("Motore disconnesso.");
    }

    public void setType(String type) {
        this.type = type;
    }
}
