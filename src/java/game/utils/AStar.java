package game.utils;

import game.GameplayPanel;
import game.entities.ghosts.Ghost;
import java.util.*;

/**
 * A* Pathfinding Algorithm Implementation
 * Uses Manhattan distance heuristic for grid-based pathfinding.
 * Guarantees the shortest path in weighted grids or heuristic-guided search.
 */
public class AStar {
    private static final int[][] DIRECTIONS = {
        {-1, 0}, // Up
        {1, 0},  // Down
        {0, -1}, // Left
        {0, 1}   // Right
    };

    private static class Node implements Comparable<Node> {
        int row, col;
        int gCost; // Cost from start
        int hCost; // Heuristic cost to goal
        int fCost; // Total cost (g + h)
        Node parent;

        Node(int row, int col, int gCost, int hCost) {
            this.row = row;
            this.col = col;
            this.gCost = gCost;
            this.hCost = hCost;
            this.fCost = gCost + hCost;
            this.parent = null;
        }

        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.fCost, other.fCost);
        }
    }

    /**
     * Finds the shortest path from start to goal using A* algorithm.
     *
     * @param maze 2D grid where 0 = free cell, 1 = wall
     * @param startRow starting row
     * @param startCol starting column
     * @param goalRow goal row
     * @param goalCol goal column
     * @return array [row, col] representing the next step toward goal, or null if no path exists
     */
    public static int[] findPath(int[][] maze, int startRow, int startCol, int goalRow, int goalCol) {
        if (!isValid(maze, goalRow, goalCol)) {
            return null;
        }

        if (startRow == goalRow && startCol == goalCol) {
            return new int[]{startRow, startCol};
        }

        PriorityQueue<Node> openSet = new PriorityQueue<>();
        boolean[][] closedSet = new boolean[maze.length][maze[0].length];
        Map<String, Node> nodeMap = new HashMap<>();

        Node startNode = new Node(startRow, startCol, 0, heuristic(startRow, startCol, goalRow, goalCol));
        openSet.add(startNode);
        nodeMap.put(startRow + "," + startCol, startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current.row == goalRow && current.col == goalCol) {
                // Reconstruct path to get the first step
                return getNextStep(current, startRow, startCol);
            }

            closedSet[current.row][current.col] = true;

            // Explore neighbors
            for (int[] direction : DIRECTIONS) {
                int newRow = current.row + direction[0];
                int newCol = current.col + direction[1];

                if (!isValid(maze, newRow, newCol) || closedSet[newRow][newCol]) {
                    continue;
                }

                int newGCost = current.gCost + 1;
                String key = newRow + "," + newCol;
                Node neighbor = nodeMap.get(key);

                if (neighbor == null || newGCost < neighbor.gCost) {
                    int hCost = heuristic(newRow, newCol, goalRow, goalCol);
                    Node newNode = new Node(newRow, newCol, newGCost, hCost);
                    newNode.parent = current;

                    if (neighbor != null) {
                        openSet.remove(neighbor);
                    }

                    openSet.add(newNode);
                    nodeMap.put(key, newNode);
                }
            }
        }

        return null; // No path found
    }

    /**
     * Heuristic function: Manhattan distance to goal.
     */
    private static int heuristic(int row, int col, int goalRow, int goalCol) {
        return Math.abs(row - goalRow) + Math.abs(col - goalCol);
    }

    /**
     * Reconstructs the first step from start toward the goal by backtracking the path.
     */
    private static int[] getNextStep(Node goalNode, int startRow, int startCol) {
        Node current = goalNode;
        Node nextStep = current;

        while (current.parent != null) {
            nextStep = current;
            current = current.parent;
        }

        return new int[]{nextStep.row, nextStep.col};
    }

    /**
     * Checks if a cell is valid (within bounds and not a wall).
     */
    private static boolean isValid(int[][] maze, int row, int col) {
        if (row < 0 || row >= maze.length || col < 0 || col >= maze[0].length) {
            return false;
        }
        return maze[row][col] == 0; // 0 = free cell, 1 = wall
    }
}
