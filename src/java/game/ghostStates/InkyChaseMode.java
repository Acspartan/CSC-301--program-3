package game.ghostStates;

import game.Game;
import game.entities.ghosts.Ghost;
import game.ghostStrategies.InkyStrategy;
import game.utils.AStar;
import game.utils.GridUtils;
import game.utils.PacmanGrid;

/**
 * InkyChaseMode - Hybrid Chase Mode for Inky Ghost
 * Inky uses a hybrid approach combining BFS (from Blinky) and A* (from Pinky).
 * - Uses A* algorithm for pathfinding (faster heuristic-guided search)
 * - Uses Inky's strategic targeting (based on Blinky's position and Pacman's location)
 * - When pathing fails, falls back to basic movement toward target
 *
 * This creates a challenging ghost that combines intelligent targeting with optimal pathfinding.
 */
public class InkyChaseMode extends GhostState {
    private InkyStrategy inkyStrategy;

    public InkyChaseMode(Ghost ghost) {
        super(ghost);
        this.inkyStrategy = new InkyStrategy(Game.getBlinky());
    }

    @Override
    public int[] getTargetPosition() {
        // Use Inky's strategic positioning: based on Blinky and Pacman
        return inkyStrategy.getChaseTargetPosition();
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

        // Generate the maze grid
        PacmanGrid pacmanGrid = new PacmanGrid();
        int[][] maze = pacmanGrid.getGrid();

        // Use A* algorithm to find the optimal path
        int[] nextCell = AStar.findPath(maze, ghostGridRow, ghostGridCol, targetGridRow, targetGridCol);

        if (nextCell == null) {
            // Fallback: no path found, use greedy approach
            computeNextDirGreedy();
            return;
        }

        // Convert grid coordinates back to pixel coordinates
        int nextPixelX = GridUtils.gridColToPixel(nextCell[1]);
        int nextPixelY = GridUtils.gridRowToPixel(nextCell[0]);

        // Calculate the movement needed
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
     * Greedy fallback when A* finds no path.
     * Moves toward the target by minimizing distance.
     */
    private void computeNextDirGreedy() {
        int[] targetPos = getTargetPosition();
        double minDist = Double.MAX_VALUE;
        int newXSpd = 0;
        int newYSpd = 0;

        // Try each direction and pick the one that minimizes distance to target
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
