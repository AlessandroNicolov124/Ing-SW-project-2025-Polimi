# ing-sw-2025-Nicolov-Errigo-Roggi-Galimberti

In order to run the project jdk 23.0.2 must be used

To modify sequence diagram visit the following link: https://sequencediagram.org/
paste in the diagram you want to modify and then export the file once done.

The group implemented the following features: Regole Complete + TUI + GUI + RMI + Socket + 2FA.
Among the advanced functionalities we managed to implement multiple matches feature and the trial flight feature.

## How to use the application (Flow Overview)

Once you have launched the client (see instructions above), you will follow these main phases:

### 0. Connection Selection

- Before accessing the lobby, the client will ask you to choose the connection type:
    - **RMI (Remote Method Invocation)**
    - **TCP/IP Socket**
- You must select your preferred connection mode.  
  The interface (GUI or TUI) will guide you through this step.
- After the connection is established, you will proceed to the next phase.

### 1. Lobby

- After logging in or registering (with 2FA if enabled), you will arrive at the lobby.
- In the lobby, you can:
    - Create a new game
    - Join an existing game
    - Wait for other players to join
- The lobby displays the list of available matches and connected users.
- Once the required number of players is reached, the game creator can start the match.

### 2. Spaceship Construction Phase

- Each player enters the spaceship construction phase.
- Here, you can assemble your spaceship by choosing and placing available modules and components.
- The interface (both GUI and TUI) provides options to select, rotate, and place parts.
- There is usually a time limit for this phase, shown in the interface.
- **After assembly, your spaceship will go through a validation phase to check if it meets all the required rules and constraints.**
    - If the spaceship is invalid (e.g., rules are not respected or some components are missing), you will be prompted to make corrections.
    - Only after successful validation can you confirm your spaceship and proceed.
- Once finished and validated, confirm your spaceship to proceed.

### 3. Game / Flight Phase

- After all players complete their construction, the flight phase begins.
- Players face a series of challenges (events, obstacles, or trials) as their spaceships travel.
- Actions may include drawing event cards, making decisions, and managing ship resources.
- The GUI/TUI will guide interaction through menus, prompts, and status displays.

### 4. Endgame and Results

- When the game ends (after a fixed number of rounds or objectives), results are shown.
- The winner and ranking are displayed, along with a summary of the match.
- You can then return to the lobby, start a new game, or exit the application.

---

**Note:**
- Both GUI and TUI offer equivalent functionalities, though the interface style will differ.
- In any phase, follow on-screen instructions to proceed.