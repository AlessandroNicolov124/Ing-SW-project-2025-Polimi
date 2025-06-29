package it.polimi.ingsw.Components;

import it.polimi.ingsw.*;

import java.io.Serializable;
import java.util.*;

public sealed abstract class Component implements Serializable permits BatteryCompartment, Cabin, Cannon, CargoHolds, Engine, LifeSupportSystem, ShieldGenerator, StructuralModule {
    protected List<Connector> connections; // Lista di connessioni
    protected Direction componentDirection; // Direzione del componente
    protected boolean damaged; // Stato del componente (danneggiato o no)
    protected String originalComponentString;

    public Component() {
        this.connections = new ArrayList<>();
        damaged = false;
        componentDirection = Direction.NORTH;
    }

    public void saveOriginalState() {
        this.originalComponentString = getComponentString();
    }

    public void deleteConnection(){
        connections.clear();
    }

    /**
     * Rimuove il connettore nella direzione specificata, se presente.
     *
     * @param direction La direzione del connettore da rimuovere
     * @return true se un connettore è stato trovato e rimosso, false se nessun connettore
     *         è stato trovato nella direzione specificata
     */
    public boolean removeConnectionByDirection(Direction direction){
        Connector c = connections.stream()
                .filter(cc -> cc.getDirection() == direction)
                .findFirst().orElse(null);

        if (c == null) {
            return false;
        }

        return connections.remove(c);
    }

    /**
     * Aggiunge un connettore nella direzione specificata.
     * Se esiste già un connettore in quella direzione, verrà sostituito con quello nuovo.
     *
     * @param direction La direzione in cui aggiungere il connettore
     * @param connectorType Il tipo di connettore da aggiungere (Singolo, Doppio, Universale, Nullo)
     * @return true se l'operazione è riuscita, false altrimenti
     */
    public boolean addConnectionInDirection(Direction direction, ConnectorType connectorType) {
        // Prima rimuoviamo eventuali connettori esistenti in quella direzione
        removeConnectionByDirection(direction);

        // Creiamo il nuovo connettore
        Connector newConnector = new Connector(direction, connectorType);

        // Aggiungiamo il nuovo connettore alla lista delle connessioni
        return connections.add(newConnector);
    }

    /**
     * Aggiunge un connettore specifico nella direzione indicata.
     * Se esiste già un connettore in quella direzione, verrà sostituito.
     *
     * @param connector Il connettore da aggiungere
     * @return true se l'operazione è riuscita, false altrimenti
     */
    public boolean addConnection(Connector connector) {
        if (connector == null) {
            return false;
        }

        // Rimuoviamo eventuali connettori esistenti nella stessa direzione
        removeConnectionByDirection(connector.getDirection());

        // Aggiungiamo il nuovo connettore
        return connections.add(connector);
    }

    /**
     * Cambia il tipo di un connettore esistente in una data direzione.
     * Se non esiste un connettore in quella direzione, ne viene creato uno nuovo.
     *
     * @param direction La direzione del connettore da modificare
     * @param newType Il nuovo tipo di connettore
     * @return true se l'operazione è riuscita, false altrimenti
     */
    public boolean changeConnectorType(Direction direction, ConnectorType newType) {
        // Cerchiamo il connettore nella direzione specificata
        Connector existingConnector = connections.stream()
                .filter(conn -> conn.getDirection() == direction)
                .findFirst().orElse(null);

        if (existingConnector != null) {
            // Se esiste, rimuoviamo il connettore esistente
            connections.remove(existingConnector);

            // Creiamo un nuovo connettore con lo stesso indirizzo ma tipo diverso
            Connector newConnector = new Connector(direction, newType);

            // Aggiungiamo il nuovo connettore
            return connections.add(newConnector);
        } else {
            // Se non esiste, creiamo un nuovo connettore
            return addConnectionInDirection(direction, newType);
        }
    }

    /**
     * Assicura che ci sia un connettore di tipo specificato in una data direzione.
     * Se già esiste un connettore dello stesso tipo, non fa nulla.
     * Se esiste un connettore di tipo diverso, lo sostituisce.
     * Se non esiste alcun connettore, ne crea uno nuovo.
     *
     * @param direction La direzione in cui assicurare il connettore
     * @param requiredType Il tipo di connettore richiesto
     * @return true se è stata fatta una modifica, false se il connettore richiesto era già presente
     */
    public boolean ensureConnector(Direction direction, ConnectorType requiredType) {
        // Cerchiamo il connettore esistente
        Connector existingConnector = getConnectorByDirection(direction);

        // Se non esiste un connettore o è di tipo diverso, facciamo la modifica
        if (existingConnector == null || !existingConnector.getConnectorType().getClass().equals(requiredType.getClass())) {
            // Rimuovi eventuale connettore esistente
            if (existingConnector != null) {
                connections.remove(existingConnector);
            }

            // Aggiungi il nuovo connettore
            Connector newConnector = new Connector(direction, requiredType);
            connections.add(newConnector);
            return true;
        }

        // Nessuna modifica necessaria, il connettore richiesto è già presente
        return false;
    }

    public boolean isDamaged() {
        return damaged;
    }

    public void setDamaged(boolean damaged) {
        this.damaged = damaged;
    }

    /**
     * Determines if this component is a starting cabin that cannot be removed.
     * Default implementation returns false. Cabin class overrides this.
     * 
     * @return true if this is a starting cabin, false otherwise
     */
    public boolean isStartingCabin() {
        return false;
    }

    public void setComponentDirection(Direction direction) {
        componentDirection = direction;
    }

    public Direction getComponentDirection() {
        return componentDirection;
    }

    // Get connections for visitor operations
    public List<Connector> getConnections() {
        return new ArrayList<>(connections);
    }

    public Connector getConnectorByDirection(Direction direction) {
        return connections.stream().filter(c -> c.getDirection() == direction).findFirst().orElse(null);
    }

    // Verifica se questo componente può connettersi a un altro componente in una data direzione
    public boolean canConnectTo(Component other, Direction direction) {
        // Trova il connettore in questa direzione
        Optional<Connector> myConnector = connections.stream()
                .filter(c -> c.getDirection() == direction)
                .findFirst();

        if (!myConnector.isPresent()) {
            return false;
        }

        // Trova il connettore nell'altra direzione
        Direction oppositeDirection = direction.getOpposite();
        Optional<Connector> otherConnector = other.getConnections().stream()
                .filter(c -> c.getDirection() == oppositeDirection)
                .findFirst();

        if (!otherConnector.isPresent()) {
            return false;
        }

        // Verifica se i connettori sono compatibili
        boolean comp = areConnectorsCompatible(myConnector.get(), otherConnector.get());
        System.out.println(" Connector are compatible: " + comp);
        return comp;
    }

    private boolean areConnectorsCompatible(Connector c1, Connector c2) {
        return c1.isCompatibleWith(c2);
    }

    public void initializeConnectors(ConnectorType n, ConnectorType e, ConnectorType s, ConnectorType w) {
        connections.add(new Connector(Direction.NORTH, n));
        connections.add(new Connector(Direction.EAST, e));
        connections.add(new Connector(Direction.SOUTH, s));
        connections.add(new Connector(Direction.WEST, w));
    }

    // Metodo pubblico che usa l'originale se disponibile
    public String getComponentString() {
        if (originalComponentString != null) {
            return originalComponentString;
        }
        return calculateComponentString();
    }
    
    // Metodo astratto che le sottoclassi devono implementare
    protected abstract String calculateComponentString();

    public String getConnectionsString(){
        StringBuilder sb = new StringBuilder();

        for(Direction d : Direction.values()){
        Connector c = getConnectorByDirection(d);
            switch(c.getConnectorType()){
                case NullConnectorType n -> sb.append("N");
                case SingleConnectorType n -> sb.append("S");
                case DoubleConnectorType n -> sb.append("D");
                case UniversalConnectorType n -> sb.append("U");
            }
        }
        return sb.toString();
    }
}











