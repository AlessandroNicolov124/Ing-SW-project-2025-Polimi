# ing-sw-2025-Nicolov-Errigo-Roggi-Galimberti

In order to run the project JDK 23.0.2 must be used.

To modify sequence diagram visit the following link: https://sequencediagram.org/  
Paste in the diagram you want to modify and then export the file once done.

The group implemented the following features: Complete Rules + TUI + GUI + RMI + Socket + 2FA.  
Among the advanced functionalities we managed to implement multiple matches feature and the trial flight feature.

---

## Build and Run Instructions

### Prerequisites

- **JDK 23.0.2** must be installed and set as your JAVA_HOME.
- **Maven** is required to build and manage project dependencies.
- **JavaFX** dependencies are already managed by Maven and will be downloaded automatically when building the project.

### Building the project

To build the project, run the following command in the project root directory:
```
mvn clean package
```

After the build completes, you will find the executable JAR file in the `target` directory.  
By default, the JAR will be named using the pattern `<artifactId>-<version>.jar`, according to your `pom.xml`.  
For this project, the generated file will be:

```
target/PSP47-1.0-SNAPSHOT.jar
```

You can verify the name by listing the contents of the `target` directory:
```
ls target/
```

---

## Running the Server

To launch the server, use:
```
java it.polimi.ingsw.App [host] [rmiPort] [socketPort]
```
- **host**:        (optional) the server's IP address (default: 127.0.0.1)
- **rmiPort**:     (optional) the RMI port (default: 1099)
- **socketPort**:  (optional) the socket port (default: 7777)

---

## Running the Client

There are **two separate client entry points**—one for the graphical interface (GUI) and one for the text-based interface (TUI):

### Avvio modalità GUI

```sh
java -cp target/PSP47-1.0-SNAPSHOT.jar it.polimi.ingsw.client.gui.ClientGUI
```

### Launching the TUI

```sh
java -cp target/PSP47-1.0-SNAPSHOT.jar it.polimi.ingsw.client.tui.ClientTUI
```

> **Note:** GUI requires JavaFX on your system, add the following options (replace `$PATH_TO_FX` with the path to your JavaFX SDK `lib` directory):
> ```
> java --module-path $PATH_TO_FX --add-modules javafx.controls,javafx.fxml -cp target/PSP47-1.0-SNAPSHOT.jar it.polimi.ingsw.client.gui.ClientGUI
> ```

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


## Spaceship Construction Rules

During the Spaceship Construction Phase, each player must assemble their spaceship according to the following rules:

1. **Connectivity**
  - All tiles (modules) must be connected directly or indirectly to the cockpit (the central starting tile).
  - There can be no isolated modules.

2. **No Overlapping**
  - Tiles cannot overlap or be placed on top of each other.

3. **Placement**
  - You must respect the grid boundaries: modules cannot be placed outside the designated construction area.

4. **Component Compatibility**
  - Some modules require specific connections (e.g., connectors, engines, batteries) and cannot be attached arbitrarily.
  - Modules with incompatible connectors cannot be joined.

5. **Open Connectors**
  - Try to minimize open connectors (i.e., connectors that do not connect to another module or the ship's border), as these may expose your spaceship to risks during flight.

6. **Required Components**
  - The spaceship must contain at least:
    - 1 cockpit (mandatory)
    - 1 engine
    - 1 crew cabin
  - Additional modules (cargo holds, shields, batteries, etc.) are optional but recommended for better performance.

7. **Orientation**
  - Some modules (e.g., engines, shields) are directional and must be oriented correctly to be effective.

8. **Validation**
  - Before proceeding, your spaceship will be validated by the system. If any rule is violated, you will be prompted to correct your design.

**Note:**  
Invalid spaceships (e.g., disconnected modules, missing required components, or incorrect placements) cannot proceed to the flight phase.  
Follow in-game prompts and warnings to adjust your design as needed.

---

**Note:**
- Both GUI and TUI offer equivalent functionalities, though the interface style will differ.
- In any phase, follow on-screen instructions to proceed.