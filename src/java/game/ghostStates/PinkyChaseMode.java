package game.ghostStates;

import game.Game;
import game.entities.ghosts.Ghost;
import game.ghostStrategies.PinkyStrategy;
import game.utils.AStar;
import game.utils.GridUtils;
import game.utils.PacmanGrid;

/**
 * PinkyChaseMode - A* Based Chase Mode for Pinky Ghost
 * Pinky uses the A* pathfinding algorithm combined with strategic targeting.
 * - Targets 2 cells ahead of Pacman in the direction Pacman is moving
 * - Uses A* algorithm for optimal pathfinding (faster than BFS for larger grids)
 * - Manhattan distance heuristic guides the search
 * - Falls back to greedy movement if no path is found
 *
 * This creates a more intelligent Pinky that uses modern pathfinding techniques.
 */
public class PinkyChaseMode extends GhostState {
    private PinkyStrategy pinkyStrategy;

    public PinkyChaseMode(Ghost ghost) {
        super(ghost);
        this.pinkyStrategy = new PinkyStrategy();
    }

    @Override
    public int[] getTargetPosition() {
        // Pinky targets 2 cells ahead of Pacman using the strategy
        return pinkyStrategy.getChaseTargetPosition();
    }

    @Override
    public void computeNextDir() {
        if (!ghost.onTheGrid()) return;
        if (!ghost.onGameplayWindow()) return;

        // Get current grid position
        int ghostGridRow = GridUtils.pixelToGridRow(ghost.getyPos());
        int ghostGridCol = GridUtils.pixelToGridCol(ghost.getxPos());

        // Get target grid position
        int[] targetPos = getTargetPosition();
        int targetGridRow = GridUtils.pixelToGridRow(targetPos[1]);
        int targetGridCol = GridUtils.pixelToGridCol(targetPos[0]);

        // Generate the maze grid for this frame
        PacmanGrid pacmanGrid = new PacmanGrid();
        int[][] maze = pacmanGrid.getGrid();

        // Validate that target is within bounds and accessible
        if (!GridUtils.isWithinBounds(targetGridRow, targetGridCol)) {
            computeNextDirGreedy();
            return;
        }

        // Use A* algorithm to find the shortest path to the target
        int[] nextCell = AStar.findPath(maze, ghostGridRow, ghostGridCol, targetGridRow, targetGridCol);

        if (nextCell == null) {
            // Fallback: no path found, use greedy approach
            computeNextDirGreedy();
            return;
        }

        // Convert grid coordinates back to pixel coordinates
        int nextPixelX = GridUtils.gridColToPixel(nextCell[1]);
        int nextPixelY = GridUtils.gridRowToPixel(nextCell[0]);

        // Calculate the movement needed to reach the next cell
        int newXSpd = 0;
        int newYSpd = 0;

        if (nextPixelX < ghost.getxPos()) {
            newXSpd = -ghost.getSpd();
        } else if (nextPixelX > ghost.getxPos()) {
            newXSpd = ghost.getSpd();
        }

        if (nextPixelY < ghost.getyPos()) {
            newYSpd = -ghost.getSpd();
        } else if (nextPixelY > ghost.getyPos()) {
            newYSpd = ghost.getSpd();
        }

        // Set the ghost's velocity
        if (newXSpd != 0 || newYSpd != 0) {
            ghost.setxSpd(newXSpd);
            ghost.setySpd(newYSpd);
        }
    }

    /**
     * Greedy fallback movement when A* finds no valid path.
     * Minimizes Euclidean distance to the target cell.
     */
    private void computeNextDirGreedy() {
        int[] targetPos = getTargetPosition();
        double minDist = Double.MAX_VALUE;
        int newXSpd = 0;
        int newYSpd = 0;

        // Test each possible direction and pick the closest to target
        int[][] directions = {{-ghost.getSpd(), 0}, {ghost.getSpd(), 0}, {0, -ghost.getSpd()}, {0, ghost.getSpd()}};
        for (int[] dir : directions) {
            int testX = ghost.getxPos() + dir[0];
            int testY = ghost.getyPos() + dir[1];
            double distance = Math.hypot(testX - targetPos[0], testY - targetPos[1]);

            if (distance < minDist) {
                minDist = distance;
                newXSpd = dir[0];
                newYSpd = dir[1];
            }
        }

        if (newXSpd != 0 || newYSpd != 0) {
            ghost.setxSpd(newXSpd);
            ghost.setySpd(newYSpd);
        }
    }
}
