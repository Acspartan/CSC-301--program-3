package game.utils;

import game.GameplayPanel;

/**
 * Utility class for converting between pixel coordinates and grid coordinates.
 * The game uses 8-pixel cells, so this class handles conversions between the two.
 */
public class GridUtils {
    private static final int CELL_SIZE = 8;

    /**
     * Converts pixel X coordinate to grid column index.
     *
     * @param pixelX the X pixel coordinate
     * @return the grid column index
     */
    public static int pixelToGridCol(int pixelX) {
        return pixelX / CELL_SIZE;
    }

    /**
     * Converts pixel Y coordinate to grid row index.
     *
     * @param pixelY the Y pixel coordinate
     * @return the grid row index
     */
    public static int pixelToGridRow(int pixelY) {
        return pixelY / CELL_SIZE;
    }

    /**
     * Converts grid column index to pixel X coordinate (center of cell).
     *
     * @param gridCol the grid column index
     * @return the pixel X coordinate
     */
    public static int gridColToPixel(int gridCol) {
        return gridCol * CELL_SIZE;
    }

    /**
     * Converts grid row index to pixel Y coordinate (center of cell).
     *
     * @param gridRow the grid row index
     * @return the pixel Y coordinate
     */
    public static int gridRowToPixel(int gridRow) {
        return gridRow * CELL_SIZE;
    }

    /**
     * Checks if a grid position is within bounds of the game level.
     *
     * @param row the grid row
     * @param col the grid column
     * @return true if the position is within bounds, false otherwise
     */
    public static boolean isWithinBounds(int row, int col) {
        int maxRows = GameplayPanel.height / CELL_SIZE;
        int maxCols = GameplayPanel.width / CELL_SIZE;
        return row >= 0 && row < maxRows && col >= 0 && col < maxCols;
    }

    /**
     * Calculates Manhattan distance between two grid positions.
     *
     * @param row1 the first position's row
     * @param col1 the first position's column
     * @param row2 the second position's row
     * @param col2 the second position's column
     * @return the Manhattan distance
     */
    public static int manhattanDistance(int row1, int col1, int row2, int col2) {
        return Math.abs(row1 - row2) + Math.abs(col1 - col2);
    }
}
