package game.entities.ghosts;

import game.Game;
import game.GameplayPanel;
import game.ghostStrategies.BlinkyStrategy;
import game.utils.WallCollisionDetector;

import java.util.*;

// Classe concrète de Blinky (le fantôme rouge)
// EDITED FROM ORIGINAL: added BFS-based pathfinding so Blinky always targets Pacman's current tile.
// Changes: replaced simple class that only set strategy with a class that overrides `update()` and
// implements a grid-based BFS to compute the next cell toward Pacman when in chase mode.
public class Blinky extends Ghost {
    public Blinky(int xPos, int yPos) {
        super(xPos, yPos, "blinky.png");
        setStrategy(new BlinkyStrategy());
    }

    // EDITED FROM ORIGINAL: override update to use BFS when in chaseMode.
    @Override
    public void update() {
        if (!Game.getFirstInput()) return; // same behavior as Ghost.update

        if (state == frightenedMode) {
            frightenedTimer++;

            if (frightenedTimer >= (60 * 7)) {
                state.timerFrightenedModeOver();
            }
        }

        if (state == chaseMode || state == scatterMode) {
            modeTimer++;

            if ((isChasing && modeTimer >= (60 * 20)) || (!isChasing && modeTimer >= (60 * 5))) {
                state.timerModeOver();
                isChasing = !isChasing;
            }
        }

        if (xPos == 208 && yPos == 168) {
            state.outsideHouse();
        }

        if (xPos == 208 && yPos == 200) {
            state.insideHouse();
        }

        // If we're in chase mode and aligned on grid, compute next move using BFS
        if (state == chaseMode && onTheGrid() && onGameplayWindow()) {
            int cellSize = 8; // consistent with level loading
            int cols = GameplayPanel.width / cellSize;
            int rows = GameplayPanel.height / cellSize;

            // Build maze grid from walls (1 = wall, 0 = free)
            int[][] maze = new int[rows][cols];
            for (int r = 0; r < rows; r++) Arrays.fill(maze[r], 0);
            for (game.entities.Wall w : Game.getWalls()) {
                int cx = w.getxPos() / cellSize;
                int cy = w.getyPos() / cellSize;
                if (cy >= 0 && cy < rows && cx >= 0 && cx < cols) maze[cy][cx] = 1;
            }

            int startR = this.yPos / cellSize;
            int startC = this.xPos / cellSize;
            int goalR = Game.getPacman().getyPos() / cellSize;
            int goalC = Game.getPacman().getxPos() / cellSize;

            int[] nextCell = bfsNextMove(maze, startR, startC, goalR, goalC);
            if (nextCell != null) {
                int targetR = nextCell[0];
                int targetC = nextCell[1];

                // Convert cell to pixel target
                int targetX = targetC * cellSize;
                int targetY = targetR * cellSize;

                // Decide direction avoiding walls (fallback to existing logic if collision)
                if (this.xPos > targetX && !WallCollisionDetector.checkWallCollision(this, -getSpd(), 0)) {
                    setxSpd(-getSpd()); setySpd(0);
                } else if (this.xPos < targetX && !WallCollisionDetector.checkWallCollision(this, getSpd(), 0)) {
                    setxSpd(getSpd()); setySpd(0);
                } else if (this.yPos > targetY && !WallCollisionDetector.checkWallCollision(this, 0, -getSpd())) {
                    setxSpd(0); setySpd(-getSpd());
                } else if (this.yPos < targetY && !WallCollisionDetector.checkWallCollision(this, 0, getSpd())) {
                    setxSpd(0); setySpd(getSpd());
                } else {
                    // fallback to default state-based direction selection
                    state.computeNextDir();
                }
            } else {
                // unreachable: fallback
                state.computeNextDir();
            }
        } else {
            // Not in chase mode or not aligned: use existing state logic
            state.computeNextDir();
        }

        updatePosition();
    }

    // EDITED FROM ORIGINAL: added BFS helper methods here
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

    private boolean isValid(int r, int c, int[][] maze, boolean[][] visited) {
        return r >= 0 && r < maze.length &&
                c >= 0 && c < maze[0].length &&
                maze[r][c] == 0 &&
                !visited[r][c];
    }
}
