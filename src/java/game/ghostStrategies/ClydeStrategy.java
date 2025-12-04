package game.ghostStrategies;

import game.Game;
import game.GameplayPanel;
import game.entities.ghosts.Ghost;
import game.utils.Utils;

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
    //Chase pacman when far but within 8 dots run to a random corner
    public int[] getChaseTargetPosition() {
        double dist = Utils.getDistance(
            ghost.getxPos(), ghost.getyPos(),
            Game.getPacman().getxPos(), Game.getPacman().getyPos()
        );
        //When far chase direct
        if (dist >= 256) {
            //Not scared
            runAway = null;

            int[] position = new int[2];
            position[0] = Game.getPacman().getxPos();
            position[1] = Game.getPacman().getyPos();
            return position;
        }else{
            //Go to random corner
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

    //Choose one of the corners at random
    private int[] pickFarthestCorner() {
        int pacX = Game.getPacman().getxPos();
        int pacY = Game.getPacman().getyPos();

        //4 possible corners
        int[][] corners = new int[][]{
            {0,0},
            {GameplayPanel.height},
            {GameplayPanel.width, GameplayPanel.height}
        };

        double maxDist = -1;
        int[] bestCorner = corners[0];

        for (int[] c : corners) {
            double d = Utils.getDistance(pacX, pacY, c[0], c[1]);
            if (d > maxDist) {
                maxDist = d;
                bestCorner = c;
            }
        }
        return bestCorner;
    }
}


