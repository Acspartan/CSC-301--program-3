package game.ghostStrategies;

import game.Game;
import game.GameplayPanel;
import game.entities.ghosts.Ghost;
import game.utils.GridUtils;

//Stratégie concrète de Clyde (le fantôme jaune)
public class ClydeStrategy implements IGhostStrategy{
    private Ghost ghost;
    
    //When clyde gets scared he will run to corner
    private int[] runAway = null;

    public ClydeStrategy(Ghost ghost) {
        this.ghost = ghost;
    }

    //Clyde cible directement Pacman s'il est au dela d'un rayon de 8 cases, et sinon il cible sa position de pause
    @Override
    //Chase pacman when far but within 8 dots run to farthest corner
    public int[] getChaseTargetPosition() {
        //Convert to grid tiles
        int clydeCol = GridUtils.toCol(ghost.getxPos());
        int clydeRow = GridUtils.toRow(ghost.getyPos());

        int pacCol = GridUtils.toCol(Game.getPacman().getxPos());
        int pacRow = GridUtils.toRow(Game.getPacman().getyPos());

        int tileDist = Math.abs(clydeCol - pacCol) + Math.abs(clydeRow - pacRow);
        //When far chase direct
        if (tileDist > 8) {
            //Not scared
            runAway = null;

            int[] position = new int[2];
            position[0] = Game.getPacman().getxPos();
            position[1] = Game.getPacman().getyPos();
            return position;
        }else{
            //Go to farthest corner
            if (runAway == null) {
                runAway = pickFarthestCorner();
            }
            return runAway;
        }
    }

    //En pause, Clyde cible la case en bas à gauche
    @Override
    public int[] getScatterTargetPosition() {
        int[] position = new int[2];
        position[0] = 0;
        position[1] = GameplayPanel.height;
        return position;
    }

    //Chooses farthest corner from pac
    private int[] pickFarthestCorner() {
        int pacX = Game.getPacman().getxPos();
        int pacY = Game.getPacman().getyPos();

        //4 possible corners
        int[][] corners = new int[][]{
            {0,0},
            {GameplayPanel.width, 0},
            {0, GameplayPanel.height},
            {GameplayPanel.width, GameplayPanel.height}
        };

        double maxDist = -1;
        int[] bestCorner = corners[0];

        for (int[] c : corners) {
            double dx = pacX - c[0];
            double dy = pacY - c[1];
            double d = Math.sqrt(dx * dx + dy * dy);
            if (d > maxDist) {
                maxDist = d;
                bestCorner = c;
            }
        }
        return bestCorner;
    }
}



