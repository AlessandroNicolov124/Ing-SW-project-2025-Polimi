package it.polimi.ingsw;

import java.util.ArrayList;
import java.util.List;

public abstract class Component {
    protected List<Connector> connections; // Lista di connessioni
    protected Direction componentDirection; // Direzione del componente

    public Component() {
        connections = new ArrayList<>();
    }

    public abstract void connectTo(Component c, Direction dir);
    public abstract void disconnect();
}

