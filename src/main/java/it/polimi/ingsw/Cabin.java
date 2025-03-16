package it.polimi.ingsw;

// Classe Cabin
public class Cabin extends Component {
    private String type;

    public Cabin() {
        super();
    }

    @Override
    public void connectTo(Component c, Direction dir) {
        // Logica per connettere la cabina a un altro componente
        connections.add(new Connector(dir, this));
        System.out.println("Cabina connessa a " + c.getClass().getSimpleName() + " nella direzione " + dir);
    }

    @Override
    public void disconnect() {
        // Logica per disconnettere la cabina
        connections.clear();
        System.out.println("Cabina disconnessa.");
    }

    public void setType(String type) {
        this.type = type;
    }
}
