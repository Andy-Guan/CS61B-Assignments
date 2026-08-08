# CS61B Project 3: BYOW (Build Your Own World) Design Document

### Author : Andy Guan (and Gemini)
## 1. Classes and Data Structures

*   **`Engine.java`**: The core controller and state manager of the application.
    *   **State Management**: It maintains the global state of the game, including the protagonist's coordinates (`playerX`, `playerY`), the deterministic generation seed (`currentSeed`), the player's cumulative `score`, and spatial data for interactive entities (e.g., portal coordinates `X1`, `Y1`, `X2`, `Y2`).
    *   **Game Loop & I/O**: It acts as the bridge between the user and the backend, processing real-time keyboard inputs through `interactWithKeyboard()` (with dual-buffering rendering via `TERenderer`) and parsing deterministic string sequences through `interactWithInputString()` for headless autograder testing.
*   **`Room.java`**: A foundational spatial data structure representing randomly generated rectangular rooms.
    *   **Properties**: It encapsulates the `(x, y)` origin coordinates (bottom-left) along with `width` and `height`.
    *   **Collision Logic**: It contains the critical `overlaps(Room other)` method. To prevent intersecting structures and guarantee at least a 1-tile thick wall between adjacent rooms, the collision detection logic intentionally adds a +1 buffer zone when comparing the boundaries of two rooms.
    *   **Utility**: Methods like `getCenterX()` and `getCenterY()` calculate the exact middle point of the room, providing crucial anchor points for the hallway generation algorithm.

## 2. Algorithms

I got the algorithm of world generation from [this website](https://christianjmills.com/posts/procedural-map-generation-techniques-notes/), and there are so many fantastic algorithms. If you are interested, you can also find some others in two files from the "related" folder.

*   **World Generation (BSP-inspired Random Placement)**:
    The engine pseudo-randomly attempts to place a designated number of rooms (e.g., 100 attempts) onto an initially empty grid filled with `Tileset.NOTHING`. For each attempt, a random width, height, and coordinate are generated. The engine then iterates through the existing list of successfully placed `Room` objects. If the `overlaps` check passes, the room is instantiated, added to the list, and its inner area is carved out using `Tileset.FLOOR`.
*   **Corridor Connectivity (L-Shaped Pathfinding)**:
    To guarantee 100% global reachability without isolated areas, the algorithm sequentially iterates through the validated list of rooms. It connects adjacent pairs (e.g., Room[i] to Room[i+1]) by drawing an L-shaped hallway between their center points. A pseudo-random coin flip (50% probability) determines the routing order: either horizontal-then-vertical or vertical-then-horizontal. `Math.min` and `Math.max` are utilized to ensure the `for` loops draw correctly regardless of the relative spatial positions of the two rooms.
*   **Wall Generation (3x3 Convolution Sweep)**:
    Instead of complex and error-prone wall calculations during room/hallway instantiation, the engine employs a robust global post-processing sweep (similar to a convolution filter in image processing). The `generateWalls` method iterates over every single tile. If a tile is `Tileset.NOTHING` but has at least one `Tileset.FLOOR` within its 3x3 surrounding neighborhood (8-way directional check), it is instantly transformed into `Tileset.WALL`. This guarantees a perfectly enclosed, waterproof map regardless of how chaotic the floor layout is.
*   **Creative Biome Generation**:
    The empty void surrounding the dungeon is populated with biomes using spatial partitioning. The `colorBackground` algorithm evaluates the `(x, y)` coordinates against the total `WIDTH` and `HEIGHT`. The outer 15% margins are rendered as Mountains, the intermediate layers as Flowers, and the central void as Water, adding immense visual depth to the world.
*   **Momentum-Preserving Teleportation**:
    Two interconnected portals (`UNLOCKED_DOOR`) are generated at the centers of the last two rooms. When the player steps onto a portal, the algorithm computes the movement directional vector: `dx = nextX - playerX` and `dy = nextY - playerY`. It then applies this exact vector to the destination portal's coordinates. This allows the player to exit the portal maintaining their original momentum and facing direction, resulting in a buttery-smooth gameplay feel.
*   **Endless Dungeon Progression**:
    A `LOCKED_DOOR` acts as the staircase to the next level. Upon interaction, the algorithm modifies the `currentSeed` (e.g., `currentSeed + 10`), caches the current `score`, and recursively calls the generation pipeline. This elegantly creates infinite, deterministic levels without memory leaks.
*   **Line of Sight (Fog of War)**:
    The `applyLineOfSight` algorithm creates a sense of mystery by applying a Euclidean distance filter. It calculates `Math.sqrt(Math.pow(x - playerX, 2) + Math.pow(y - playerY, 2))`. Tiles falling outside the `visionRadius` are overwritten with `Tileset.NOTHING` in a cloned display array, ensuring the true underlying world data remains intact while obscuring the player's vision.

## 3. Persistence (Record and Replay Architecture)

The game abandons traditional state-saving (e.g., saving discrete variables like coordinates or chest status) in favor of a much more robust **Event Sourcing** architecture.
*   **Continuous Recording**: Every valid deterministic input (the initial seed, followed by validated `W, A, S, D` keystrokes) is appended to a `moveHistory` string (e.g., `N7345696SWWDDSA...`).
*   **Saving**: When the `:Q` command is detected, the engine simply writes this single, comprehensive string to `savefile.txt`.
*   **Loading and Fast-Forwarding**: Upon pressing `L`, the engine reads the command string, strips the quit command, and feeds it entirely into `interactWithInputString()`. The engine deterministically rebuilds the exact world from the seed and silently, instantly fast-forwards through every single historical move in memory without engaging the `TERenderer`. This guarantees 100% state accuracy, perfectly restoring player position, score variations, and opened chests, while effectively preventing save-scumming or infinite-loot exploits.

## 4. Future Outlook (Prospects)

While the core engine currently demonstrates strong architectural integrity and interactivity, future iterations can evolve this project from a dungeon generator into a fully-fledged game:

*   **Advanced Raycasting for True Shadows**: The current Euclidean distance vision is a simple circular mask. Implementing **Bresenham's Line Algorithm** or **Recursive Shadowcasting** would calculate actual physical occlusion. If a ray hits a wall tile, it terminates, casting realistic, dynamic shadows behind structures and severely limiting vision around corners.
*   **Cellular Automata Cave Generation**: Supplementing the BSP-style rectangular room generation with Cellular Automata algorithms (the 4-5 rule) to generate organic, natural-looking cave systems, providing a stark contrast to the rigid dungeon architecture.
*   **Dynamic Entity Component System (ECS) & Pathfinding**: Introducing hostile mobs or NPCs. By implementing graph-search algorithms like **A* (A-Star)** or **Breadth-First Search (BFS)**, entities could dynamically calculate the shortest path to the player, transforming the exploration experience into a tense, tactical survival scenario.
*   **Inventory & State Machine**: Expanding the interaction mechanics beyond a simple integer score. We could introduce a robust inventory system with equippable items, keys matching specific door IDs, and a combat state machine dealing with HP, attack frames, and damage calculations.

