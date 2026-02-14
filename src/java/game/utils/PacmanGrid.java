package game.utils;

import game.GameplayPanel;
import game.utils.WallCollisionDetector;

/**
 * PacmanGrid generates a grid representation of the game level.
 * Converts the pixel-based level with walls into a 0/1 grid where:
 * 0 = free cell (passable)
 * 1 = wall (blocked)
 * This grid is used for pathfinding algorithms like A* and BFS.
 */
public class PacmanGrid {
    private int[][] grid;
    private int rows;
    private int cols;
    private static final int CELL_SIZE = 8;

    /**
     * Initializes the grid based on the current game level.
     * Scans the level and marks walls as 1, free cells as 0.
     */
    public PacmanGrid() {
        initializeGrid();
    }

    /**
     * Initializes the grid by checking wall collisions at each position.
     */
    private void initializeGrid() {
        // Get grid dimensions based on game level size
        this.rows = GameplayPanel.height / CELL_SIZE;
        this.cols = GameplayPanel.width / CELL_SIZE;

        this.grid = new int[rows][cols];

        // For each grid cell, check if there's a wall
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int pixelX = col * CELL_SIZE;
                int pixelY = row * CELL_SIZE;

                // Check if this cell is blocked by a wall
                if (isWallAtPosition(pixelX, pixelY)) {
                    grid[row][col] = 1; // Wall
                } else {
                    grid[row][col] = 0; // Free cell
                }
            }
        }
    }

    /**
     * Checks if there's a wall at the given pixel position.
     * Uses WallCollisionDetector to determine if the cell is passable.
     *
     * @param pixelX pixel X coordinate
     * @param pixelY pixel Y coordinate
     * @return true if there's a wall, false if free
     */
    private boolean isWallAtPosition(int pixelX, int pixelY) {
        // Check if any wall occupies this cell by testing collision
        for (game.entities.Wall w : game.Game.getWalls()) {
            if (w.getxPos() == pixelX && w.getyPos() == pixelY) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the grid representation.
     *
     * @return the 2D grid array (0 = free, 1 = wall)
     */
    public int[][] getGrid() {
        return grid;
    }

    /**
     * Gets the number of rows in the grid.
     */
    public int getRows() {
        return rows;
    }

    /**
     * Gets the number of columns in the grid.
     */
    public int getCols() {
        return cols;
    }

    /**
     * Refreshes the grid if the level changes.
     */
    public void refresh() {
        initializeGrid();
    }
}
