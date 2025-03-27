package it.polimi.ingsw.Components;

import it.polimi.ingsw.*;
import it.polimi.ingsw.Visitors.Visitor;

import java.util.*;

// Classe Cabin
public class Cabin extends Component {
    private List<Crew> crew;
    private boolean isStartingCabin;
    private Set<LifeSupportSystemType> connectedLifeSupports;

    public void initializeConnectors() {
        // Cabina di partenza ha connettori universali su tutti i lati
        connections.add(new Connector(Direction.NORTH, ConnectorType.UNIVERSAL));
        connections.add(new Connector(Direction.EAST, ConnectorType.UNIVERSAL));
        connections.add(new Connector(Direction.SOUTH, ConnectorType.UNIVERSAL));
        connections.add(new Connector(Direction.WEST, ConnectorType.UNIVERSAL));
    }

    public Cabin() {
        this(false);
    }

    public Cabin(boolean isStartingCabin) {
        super();
        this.crew = new ArrayList<>();
        this.isStartingCabin = isStartingCabin;
        this.connectedLifeSupports = new HashSet<>();
        if (isStartingCabin) {
            initializeConnectors();
        }
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public Crew getCrewByIndex(int index) {
        return crew.get(index);
    }

    public List<Crew> getCrew() {
        return crew;
    }

    public void removeCrew(Crew crew) {
        this.crew.remove(crew);
    }

    public boolean isStartingCabin() {
        return isStartingCabin;
    }

    public void connectLifeSupport(LifeSupportSystemType type) {
        connectedLifeSupports.add(type);
    }

    public boolean hasConnectedLifeSupport(LifeSupportSystemType type) {
        return connectedLifeSupports.contains(type);
    }

    public Set<LifeSupportSystemType> getConnectedLifeSupports() {
        return new HashSet<>(connectedLifeSupports);
    }

    public boolean addCrew(Crew crewMember) {
        // Check if this is the starting cabin and the crew is alien
        if (isStartingCabin && crewMember.getType() != CrewType.HUMAN) {
            return false; // Aliens can't live in the starting cabin
        }

        // Check if there are already 2 humans or an alien
        if (crew.size() >= 2 || (crew.size() == 1 && crew.get(0).getType() != CrewType.HUMAN)) {
            return false; // Cabin is full
        }

        // Check if the crew member can live in this cabin
        if (!crewMember.canLiveIn(this)) {
            return false;
        }

        // If we have one alien, we can't add another of any type
        if (crew.size() == 1 && crew.get(0).getType() != CrewType.HUMAN && crewMember.getType() != CrewType.HUMAN) {
            return false;
        }

        // If all checks pass, add the crew member
        crew.add(crewMember);
        return true;
    }

    public boolean canAddAlien(CrewType alienType) {
        // Can't add aliens to the starting cabin
        if (isStartingCabin) {
            return false;
        }

        // Can't add if cabin is full
        if (crew.size() >= 2 || (crew.size() == 1 && crew.get(0).getType() != CrewType.HUMAN)) {
            return false;
        }

        // Check if we have appropriate life support for this alien type
        return switch (alienType) {
            case PURPLE_ALIEN -> connectedLifeSupports.contains(LifeSupportSystemType.PURPLE);
            case BROWN_ALIEN -> connectedLifeSupports.contains(LifeSupportSystemType.BROWN);
            default -> false; // Not an alien type
        };
    }



}

