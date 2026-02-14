# CSC 301 Program 3: Pac-Man Game Implementation Summary

**Overview and Functional Changes**

This document summarizes major modifications to the Pac-Man game, focusing on gameplay mechanics and pathfinding algorithms. The scoring system was adjusted: SuperPacGum now awards 500 points (previously 100), regular PacGum remains 10 points, eating scared ghosts awards 200 points (previously 500), and per-move penalty is -1 point to add time pressure. The ghost vulnerability system transitioned from frame-based (7 seconds) to move-based (10 Pacman moves), making scared ghost duration fairer and adaptable to player pace. The codebase was refactored to create BFSGhost, a new abstract base class centralizing Breadth-First Search pathfinding logic. Both Blinky and Inky now inherit from BFSGhost, eliminating code duplication. GhostState.computeNextDir() was completely refactored to implement true Breadth-First Search instead of the original greedy straight-line distance heuristic, ensuring all ghosts compute genuine shortest paths to their targets.

**Design Patterns and Implementation**

Four design patterns are used to manage clean separation of concerns. Abstract Factory (ghostFactory directory) encapsulates ghost creation through specialized factories (BlinkyFactory, ClydeFactory, InkyFactory, PinkyFactory), enabling extensibility without modifying Game.java. State Pattern (ghostStates directory) manages six ghost modes—HouseMode, ChaseMode, ScatterMode, FrightenedMode, EatenMode, PinkyChaseMode—allowing dynamic state transitions. Each state overrides update() and computeNextDir(); the key improvement is GhostState.computeNextDir() now uses true Breadth-First Search instead of greedy heuristics, guaranteeing optimal shortest paths. Strategy Pattern (ghostStrategies directory) defines chase targeting through IGhostStrategy: Blinky targets Pacman directly, Pinky targets two cells ahead, Inky calculates position from Blinky-Pacman relative distance, and Clyde switches between chasing (when far) and scattering (when close). Observer Pattern manages scoring and timers: Pacman.notifyObserverMove() triggers Game.notifyObserverMove(), which calls updateMove() on all observers. Game's updateMove() decrements scared ghost timers; UIPanel's updateMove() decrements score by 1 point, cleanly separating mechanics from scoring.
# Justification of Design Patterns Chosen

## Abstract Factory
There are 4 different ghosts, each with its own sprite (image) and behavior.
 * **Blinky** the red ghost
 * **Pinky** the pink ghost
 * **Inky** the blue ghost
 * and **Clyde** the yellow ghost 
 
The 4 "concrete" ghosts implement the same abstract class "Ghost". To instantiate them, the Abstract Factory pattern is used. Different "Concrete Factories" call the different constructors of the concrete ghosts.

## State
The ghosts each have different states, and they must be able to transition from one state to another based on certain conditions (see GhostState.png diagram). Depending on their state, the targeted cell may be different, or their sprite may be different for example.
The State pattern is therefore ideal in this case. Respecting the single responsibility principle, the different states are managed in dedicated classes that extend the same abstract "GhostState" class.
 * **HouseMode** : State of ghosts when they are in their house initially. They will try to leave.
 * **ChaseMode** : In this state, ghosts try to pursue Pacman, each ghost having its own strategy to do so (see Strategy pattern). With a timer, ghosts alternate between ChaseMode and ScatterMode states.
 * **ScatterMode** : In this state, ghosts stop pursuing Pacman and target a corner instead. Each ghost targets a corner specific to it (see Strategy pattern). With a timer, ghosts alternate between ChaseMode and ScatterMode states.
 * **FrightenedMode** : State of ghosts after Pacman eats a SuperPacGum. In this state, the direction chosen by ghosts is random, and Pacman is able to "eat" them. After a certain time, ghosts return to ChaseMode or ScatterMode state.
 * **EatenMode** : State of ghosts once "eaten" by Pacman. They then seek to return to their house. Pacman can touch them without danger in this case.

 ## Strategy
Each of the 4 ghosts has its own strategy (the targeted cell is not the same) to pursue Pacman in "ChaseMode", and has its own corner in "ScatterMode". The Strategy pattern is therefore used to describe these behavioral variants in different classes extending the same abstract "GhostStrategy" class.
 * **BlinkyStrategy** : In ChaseMode, Blinky targets Pacman's position directly, and in ScatterMode Blinky targets the top-right corner.
 * **PinkyStrategy** : In ChaseMode, Pinky targets the position 2 cells ahead of Pacman, and in ScatterMode Pinky targets the top-left corner. 
 * **InkyStrategy** : Simply put, in ChaseMode, Inky targets a cell based on the positions of Blinky and Pacman to try to encircle the latter. In ScatterMode Inky targets the bottom-right corner. 
 * **ClydeStrategy** : In ChaseMode, Clyde targets Pacman's position directly if it is within a radius of more than 8 cells from him, and otherwise targets the same cell as its ScatterMode state. In ScatterMode Clyde targets the bottom-left corner. 
 
 ## Observer
When Pacman comes into contact with a PacGum, a SuperPacGum, or a ghost, other classes must be notified in order to trigger certain things. This is managed through the Observer pattern:
 * When Pacman eats a PacGum, the score displayed by the "UIPanel" class increases by 10, and the "Game" class managing all entities must consider this PacGum as destroyed.
 * When Pacman eats a SuperPacGum, the score displayed by the "UIPanel" class increases by 500, and the "Game" class managing all entities must consider this SuperPacGum as destroyed and transition ghosts into "FrightenedMode" state.
 * When Pacman comes into contact with a ghost, if the latter is neither in "FrightenedMode" nor "EatenMode" state, the Game class indicates that it is game over! Otherwise, if the ghost is in "FrightenedMode" state, it transitions to "EatenMode" state and the displayed score increases by 200.
 

**Implementation Details and How It Works**

The move-based scared ghost timer operates through the observer notification chain: Pacman.notifyObserverMove() triggers Game.updateMove(), which calls reduceFrightenedMove() on each ghost. This method decrements frightenedMovesRemaining and transitions the ghost out of FrightenedMode when the counter reaches zero. BFS pathfinding operates on an 8-pixel cell grid matching the level's cell size. When a ghost in chase mode aligns to the grid, GhostState.computeNextDir() builds a 2D boolean grid of walls and free cells, executes queue-based BFS from the ghost's current cell to its target, maintains visited sets and parent pointers for path reconstruction, and returns the next movement cell. For Inky, the hybrid implementation extends BFSGhost and overrides getTargetRow() and getTargetCol() to delegate target calculation to its state; when in PinkyChaseMode, that state uses InkyStrategy to calculate a target from Blinky's location and Pacman's relative position, then inherited bfsNextMove() finds the shortest path. Modified files include: Observer.java and Sujet.java (added signatures), Pacman.java (calls notifyObserverMove()), UIPanel.java (scoring), Ghost.java (frightened timer), Game.java (implements updateMove()), Blinky.java and Inky.java (now extend BFSGhost), and new BFSGhost.java abstract class (provides bfsNextMove() and isValid() helpers).

**Strengths, Limitations, and References**

Strengths include deterministic shortest-path behavior guaranteeing optimal ghost movement and intelligent chase tactics; fair move-based scared ghost timer adapting to player pace; reduced code duplication improving maintainability; and clear design pattern mapping enhancing readability. Limitations include: BFS per-grid-alignment is more CPU-intensive than greedy heuristics (acceptable on modern hardware with small grids); BFS operates only on the 8-pixel cell grid, requiring different approaches for finer navigation; and perfectly rational ghost paths reduce arcade unpredictability. Code references: BFSGhost.java (new abstract base centralizing BFS), Blinky.java and Inky.java (extend BFSGhost), GhostState.java (core BFS implementation). External references: Wikipedia's Breadth-first search (https://en.wikipedia.org/wiki/Breadth-first-search), Red Blob Games' pathfinding guide (https://www.redblobgames.com/pathfinding/), The Pac-Man Dossier by Jamey Pittman.

**Learning Outcomes and Testing Validation**

CO7 (Graphs and Shortest Path Algorithms) is addressed through BFS implementation in GhostState and BFSGhost, where the game board functions as an unweighted graph. BFS guarantees shortest paths from any ghost's current cell to its target, providing hands-on experience with graph representation, queue-based traversal, visited-set management, and path reconstruction. CO3 (Algorithm Selection and Complexity Analysis) is demonstrated by contrasting greedy heuristics (simple but non-optimal) with BFS (correct but computationally costlier), illustrating context-dependent algorithm choice and simplicity-optimality trade-offs. Validation includes: verifying BFS produces shortest paths; testing scared timer accuracy (10 moves per capsule); validating score deductions (-1 per move); testing rewards and state transitions; examining inheritance hierarchy without code duplication; profiling BFS performance; testing edge cases (pathfinding when surrounded); verifying Observer pattern propagation; and end-to-end play-testing for cohesive, fun gameplay.

---

*Document prepared for CSC 301 Program 3 submission*
Ghosts have multiple modes (states) and must switch between them depending on game events: `HouseMode`, `ChaseMode`, `ScatterMode`, `FrightenedMode`, and `EatenMode`. Using the State pattern keeps each mode's behavior encapsulated in a dedicated class that extends `GhostState`, making state transitions easy to manage and extend.

### Strategy
Chase behavior differs for each ghost; this is modeled with the Strategy pattern. Each ghost gets an `IGhostStrategy` (e.g., `BlinkyStrategy`, `PinkyStrategy`, `InkyStrategy`, `ClydeStrategy`) that provides the chase target calculation. This cleanly separates *how* a target is chosen from *what* the ghost does to reach that target.

### Observer
The Observer pattern is used for events related to Pacman interactions: when Pacman eats a `PacGum`, `SuperPacGum`, or collides with a ghost, observers (such as `Game` and `UIPanel`) are notified to update game state and the UI.

**Why these patterns:** they promote modularity and single responsibility. Adding another ghost or new mode/state is straightforward without modifying existing classes.

---
1. `src/java/game/Observer.java` — added `updateMove()` to notify observers on Pacman moves.
2. `src/java/game/Sujet.java` — added `notifyObserverMove()` so `Pacman` can notify observers each move.
3. `src/java/game/entities/Pacman.java` — call `notifyObserverMove()` after a successful move; added observer notification implementation.
4. `src/java/game/UIPanel.java` — updated scoring values and added per-move penalty handling.
5. `src/java/game/entities/ghosts/Ghost.java` — introduced move-based frightened counter (`frightenedMovesRemaining`) and `reduceFrightenedMove()`.
6. `src/java/game/Game.java` — implemented `updateMove()` to decrement ghosts' scared timers.
7. `src/java/game/entities/ghosts/Blinky.java` — moved to BFS pathfinding (see later changes); adjusted frightened-mode animation separation.

---

*Document prepared for CSC 301 Program 3 submission*
  - Implemented a move-based scared timer for ghosts (10 Pacman moves) and made the game decrement the scared counter on each Pacman move.
  - Introduced per-move scoring penalty (-1 point per Pacman move) and adjusted capsule/ghost scoring values.
  - Centralized BFS pathfinding for ghosts into a new `BFSGhost` abstract class and refactored `Blinky` and `Inky` to inherit from it. `Inky` becomes a true hybrid by using Pinky-style targeting logic (via `InkyStrategy`/`PinkyChaseMode`) combined with inherited BFS movement.
  - Refactored `GhostState` to use true BFS in `computeNextDir()` for all ghosts that use that method (converted from a greedy straight-line approach), improving path optimality.

- How it works (implementation summary):
  - Pacman notifies observers on every successful move via `notifyObserverMove()`; `Game` listens to these events and calls `reduceFrightenedMove()` on each ghost. When a ghost’s frightened move counter reaches zero, it transitions out of `FrightenedMode`.
  - BFS pathfinding operates on an 8x8 grid representation (matching level cell size). When a ghost is in chase mode and aligned to the grid, the BFS routine builds a grid of walls and free cells, runs a queue-based BFS to find the shortest path to the target cell, and returns the next cell to move toward. The ghost then sets x/y speeds toward that cell.

- Strengths:
  - Deterministic shortest-path chase: BFS guarantees shortest-path movement on the grid, making ghost behavior more intelligent and predictable.
  - Correctness of scared timer: move-based scared duration is fairer than frame-based durations because it adapts consistently to player pace.
  - Reduced code duplication: centralizing BFS logic in `BFSGhost` improves maintainability and reduces chances for bugs.
  - Clear mapping to design patterns: changes are implemented within the existing Abstract Factory, State, Strategy and Observer patterns.

- Limitations:
  - Performance cost: running BFS each time a ghost aligns to grid (for multiple ghosts) can be more CPU intensive than a greedy heuristic, particularly on large levels or with many ghosts. On modern machines this should be acceptable for the small grid used here, but profiling is recommended.
  - Grid resolution: BFS operates on the same 8-pixel cell grid that the level uses; if more precise or continuous navigation is needed, a different pathfinding resolution would be required.
  - Determinism vs. unpredictability: BFS yields optimal paths but can make ghosts too perfect; combining BFS with heuristics or randomized tie-breaking may produce more classic arcade behavior.

---

*Document prepared for CSC 301 Program 3 submission*
- Red Blob Games: A* and graph pathfinding — https://www.redblobgames.com/pathfinding/
- Classic Pac-Man ghost behavior explanation — e.g., "The Pac-Man Dossier" by Jamey Pittman (useful for ghost strategies and behavior models)

If you borrowed external code or algorithms, cite the source explicitly above (here we referenced BFS and pathfinding resources for background).

---
## 6) Mapping to Learning Outcomes

CO7. Graphs (3.0 pts threshold)
- Learning outcome: "Understand several graph algorithms and their applications, such as topological sorting, minimum spanning trees, and shortest paths."
- How work maps to CO7: The refactor introduces BFS (a core shortest-path graph algorithm) as the primary navigation method for ghosts. The implementation shows constructing a grid graph (cells and walls), exploring nodes with BFS, and reconstructing the shortest path to a target node. This directly meets the "shortest paths" competency.
- Suggested evidence: `BFSGhost.java` and `GhostState.java` contain the BFS implementation and usage.

CO3. Algorithms (3.0 pts threshold)
- Learning outcome: "Understand a variety of algorithms, including their behavior, their complexity, and their implementation."
- How work maps to CO3: The repository now includes an implemented BFS algorithm for shortest-path, and previously-added strategy logic (Pinky's targeting calculation) demonstrates algorithm design choices. The complexity of BFS is O(V + E) for the grid graph (here proportional to the number of cells), and the trade-offs (optimality vs. performance cost) were considered in the Results Summary.

---
1. Verify the scoring changes in `UIPanel` by playing and checking the displayed score after eat events.
2. Verify PACMAN's move notifies observers and ghosts decrement scared counters precisely 10 times after a capsule.
3. Observe ghost movement in chase mode: ghosts should follow shortest-path corridors rather than just greedy straight-line movement.
4. Profile or log BFS invocation counts if needed to ensure performance is acceptable.

---

For the pinky ghost, we are using a strategy that the ghost will target 2 tiles in front of pacman in the direction that pacman is moving. It uses its normal scatter target as the top left corner still. At first, the ghosts used the same greedy movement logic by choosing a direction that minimized the Euclidean distance to pacman. We upgraded pinky by using an A* algorithm, so it still targets 2 tiles ahead but instead of the greedy approach it computes the shortest path to the target. To make the A* algorithm fit with the game, we converted the pixels and walls to a grid for the ghosts and pacman. Converting their positions to grid coordinates helped in terms of how they moved being through tiles instead of pixels for A*. We built the grid based on the game’s actual maze using 0s and 1s for the walls are and are not. For pinky’s specific strategy, we made it so pinky will recalculate his direction when on a tile using the grid coordinates of his current position with is target position being 2 tiles in front of pacman. As for Clyde, we kept the original program’s greedy movement logic. However, we also decided to convert his position into tile coordinates from a grid too. We used Geeks4Geeks to implement the use of Manhattan distance in tiles for when Clyde gets too close to pacman. When he does get too close, we decided that Clyde will pick the farthest corner from pacman and run to there. His movement uses the same of the original program, but his target changes depending on his distance from pacman.

CItations:
https://www.geeksforgeeks.org/dsa/calculate-the-manhattan-distance-between-two-cells-of-given-2d-array/
https://www.geeksforgeeks.org/dsa/a-search-algorithm/
https://www.geeksforgeeks.org/dsa/a-search-algorithm/
https://pacman.fandom.com/wiki/Maze_Ghost_AI_Behaviors
https://www.geeksforgeeks.org/dsa/breadth-first-search-or-bfs-for-a-graph/
https://stackoverflow.com/questions/16492731/how-to-create-a-path-tracing-algorithm-for-pacman
VS code chat bot to orginise and keep track of chnages to use in documentation
Pac- man GitHub refrences:https://github.com/lucasvigier/pacman/blob/main/src/java/game/Game.java 



