package it.polimi.ingsw.Components;

import it.polimi.ingsw.*;

import java.util.*;

/**
 * Cabin is a component that represents a cabin in the game.
 * It can hold crew members and has connectors for life support systems.
 * The cabin can be a starting cabin or a regular cabin.
 * The starting cabin can only hold human crew members and has universal connectors.
 */
public final class Cabin extends Component {
    private List<Crew> crew;
    private boolean isStartingCabin;
    private Set<LifeSupportSystemType> connectedLifeSupports;

    public void initializeConnectors() {
        // Cabina di partenza ha connettori universali su tutti i lati
        connections.add(new Connector(Direction.NORTH, new UniversalConnectorType()));
        connections.add(new Connector(Direction.EAST, new UniversalConnectorType()));
        connections.add(new Connector(Direction.SOUTH, new UniversalConnectorType()));
        connections.add(new Connector(Direction.WEST, new UniversalConnectorType()));
    }

    public Cabin() {
        this(false);
    }

    @Override
    public String getComponentString() {
        String s = this.getClass().getSimpleName().toLowerCase() + "_";
        if(isStartingCabin){
            s += "starting";
        }else{
            s += getConnectionsString();
        }
        return s;
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

    public Crew getCrewByIndex(int index) {
        return crew.get(index);
    }

    public List<Crew> getCrew() {
        return crew;
    }

    public boolean removeCrew(Crew crew) {
        return this.crew.remove(crew);
    }

    /**
     * check if this cabin is the starting cabin.
     * @return true if this is the starting cabin, false otherwise
     */
    public boolean isStartingCabin() {
        return isStartingCabin;
    }

    /**
     * Connect a life support system to this cabin.
     * @param type the type of life support system to connect
     */
    public void connectLifeSupport(LifeSupportSystemType type) {
        connectedLifeSupports.add(type);
    }

    /**
     * Check if this cabin has a connected life support system of the specified type.
     * @param type the type of life support system to check
     * @return true if the cabin has the specified life support system connected, false otherwise
     */
    public boolean hasConnectedLifeSupport(LifeSupportSystemType type) {
        return connectedLifeSupports.contains(type);
    }

    public Set<LifeSupportSystemType> getConnectedLifeSupports() {
        return new HashSet<>(connectedLifeSupports);
    }

    /**
     * Add a crew member to this cabin. if possibile:
     * if the cabin is the starting cabin, it can only accept human crew members.
     * The cabin can hold a maximum of 2 human crew members or 1 alien crew member.
     * @param crewMember the crew member to add
     * @return true if the crew member was added successfully, false otherwise
     */
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

    /**
     * Check if an alien of the specified type can be added to this cabin.
     * it is necessary to check if the cabin is not the starting cabin, and if it has the appropriate life support system connected.
     * @param alienType the type of alien to check
     * @return true if the alien can be added, false otherwise
     */
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

    /**
     * Initialize the crew in the starting cabin with two human crew members.
     * This method is only applicable if the cabin is the starting cabin.
     * @return true if the crew was initialized successfully, false otherwise
     */
    public boolean initializeCrewStartingCabin() {
        if (isStartingCabin) {
            return addCrew(new HumanCrew()) &&
                    addCrew(new HumanCrew());
        }
        return false;
    }

    /**
     * Check if the cabin is occupied by any crew members.
     * @return true if the cabin has at least one crew member, false otherwise
     */
    public boolean isOccuped() {
        return !crew.isEmpty();
    }

    public void disconnectLifeSupport(LifeSupportSystemType type) {
        connectedLifeSupports.remove(type);
    }
}

