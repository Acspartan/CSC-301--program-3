package game.ghostStates;

import game.Game;
import game.GameplayPanel;
import game.entities.ghosts.Ghost;
import game.utils.Utils;
import game.utils.WallCollisionDetector;

import java.util.*;
import java.util.Queue;
import java.util.LinkedList;

//Classe abstrate pour décrire les différents états de fantômes
public abstract class GhostState {
    protected Ghost ghost;

    public GhostState(Ghost ghost) {
        this.ghost = ghost;
    }

    //Différentes transitions possibles d'un état vers un autre
    public void superPacGumEaten() {}
    public void timerModeOver() {}
    public void timerFrightenedModeOver() {}
    public void eaten() {}
    public void outsideHouse() {}
    public void insideHouse() {}

    public int[] getTargetPosition(){
        return new int[2];
    } //retourne le point que va cibler le fantôme

    //Méthode pour calculer la prochaine direction que le fantôme va prendre (using true BFS instead of greedy approach)
    // EDITED FROM ORIGINAL: Completely refactored from greedy distance calculation to true BFS
    // CHANGE: Replaced simple distance-based greedy algorithm with Breadth-First Search
    // BEFORE: Calculated straight-line distance to target and picked the closest direction (not optimal through maze)
    // AFTER: Uses BFS to find the shortest path through the maze (optimal pathfinding)
    // BENEFIT: All ghosts using computeNextDir() now have intelligent pathfinding instead of greedy behavior
    public void computeNextDir() {
        if (!ghost.onTheGrid()) return; //Le fantôme doit être sur une "case" de la zone de jeu
        if (!ghost.onGameplayWindow()) return;  //Le fantôme doit être dans la zone de jeu

        int cellSize = 8; // consistent with level loading
        int cols = GameplayPanel.width / cellSize;
        int rows = GameplayPanel.height / cellSize;

        // CHANGED: Build maze representation for BFS pathfinding
        // Create a grid where 1 = wall (impassable) and 0 = free (passable)
        int[][] maze = new int[rows][cols];
        for (int r = 0; r < rows; r++) Arrays.fill(maze[r], 0);
        for (game.entities.Wall w : Game.getWalls()) {
            int cx = w.getxPos() / cellSize;
            int cy = w.getyPos() / cellSize;
            if (cy >= 0 && cy < rows && cx >= 0 && cx < cols) maze[cy][cx] = 1;
        }

        int startR = ghost.getyPos() / cellSize;
        int startC = ghost.getxPos() / cellSize;
        int goalR = getTargetPosition()[1] / cellSize;
        int goalC = getTargetPosition()[0] / cellSize;

        int[] nextCell = bfsNextMove(maze, startR, startC, goalR, goalC);
        if (nextCell != null) {
            int targetR = nextCell[0];
            int targetC = nextCell[1];

            // Convert cell to pixel target
            int targetX = targetC * cellSize;
            int targetY = targetR * cellSize;

            // Decide direction avoiding walls (fallback to no movement if collision)
            if (ghost.getxPos() > targetX && !WallCollisionDetector.checkWallCollision(ghost, -ghost.getSpd(), 0)) {
                ghost.setxSpd(-ghost.getSpd());
                ghost.setySpd(0);
            } else if (ghost.getxPos() < targetX && !WallCollisionDetector.checkWallCollision(ghost, ghost.getSpd(), 0)) {
                ghost.setxSpd(ghost.getSpd());
                ghost.setySpd(0);
            } else if (ghost.getyPos() > targetY && !WallCollisionDetector.checkWallCollision(ghost, 0, -ghost.getSpd())) {
                ghost.setxSpd(0);
                ghost.setySpd(-ghost.getSpd());
            } else if (ghost.getyPos() < targetY && !WallCollisionDetector.checkWallCollision(ghost, 0, ghost.getSpd())) {
                ghost.setxSpd(0);
                ghost.setySpd(ghost.getSpd());
            }
        }
    }

    // Helper method for true BFS pathfinding
    // CHANGED: True Breadth-First Search implementation (moved from individual ghost classes)
    // This replaces the greedy distance calculation that was used before
    // BFS explores the maze level-by-level, guaranteeing the shortest path
    private int[] bfsNextMove(int[][] maze, int startR, int startC, int goalR, int goalC) {
        int rows = maze.length;
        int cols = maze[0].length;

        boolean[][] visited = new boolean[rows][cols];
        int[][] parentR = new int[rows][cols];
        int[][] parentC = new int[rows][cols];

        for (int[] row : parentR) Arrays.fill(row, -1);
        for (int[] row : parentC) Arrays.fill(row, -1);

        Queue<int[]> q = new LinkedList<>();
        q.add(new int[]{startR, startC});
        visited[startR][startC] = true;

        int[][] directions = {
                {-1, 0}, // up
                {1, 0},  // down
                {0, -1}, // left
                {0, 1}   // right
        };

        while (!q.isEmpty()) {
            int[] cur = q.poll();
            int r = cur[0];
            int c = cur[1];

            if (r == goalR && c == goalC) break;

            for (int[] d : directions) {
                int nr = r + d[0];
                int nc = c + d[1];

                if (isValid(nr, nc, maze, visited)) {
                    visited[nr][nc] = true;
                    parentR[nr][nc] = r;
                    parentC[nr][nc] = c;
                    q.add(new int[]{nr, nc});
                }
            }
        }

        if (goalR < 0 || goalR >= rows || goalC < 0 || goalC >= cols) return null;
        if (!visited[goalR][goalC]) return null;

        int r = goalR;
        int c = goalC;

        while (!(parentR[r][c] == startR && parentC[r][c] == startC)) {
            int pr = parentR[r][c];
            int pc = parentC[r][c];
            r = pr;
            c = pc;
            if (r == -1 || c == -1) return null; // safety
        }

        return new int[]{r, c};
    }

    // CHANGED: Validation method for BFS (checks if cell is in bounds, not a wall, and not visited)
    private boolean isValid(int r, int c, int[][] maze, boolean[][] visited) {
        return r >= 0 && r < maze.length &&
                c >= 0 && c < maze[0].length &&
                maze[r][c] == 0 &&
                !visited[r][c];
    }
}
