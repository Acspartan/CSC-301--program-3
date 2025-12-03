package game.ghostStrategies;

import game.Game;
import game.GameplayPanel;
import game.entities.ghosts.Ghost;
import game.utils.Utils;

//For Clyde's random movement
import java.util.Random;

//Stratégie concrète de Clyde (le fantôme jaune)
public class ClydeStrategy implements IGhostStrategy{
    private Ghost ghost;
    private Random random = new Random();

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
                runAway = pickRandomCorner();
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
    private int[] pickRandomCorner() {
        int[] position = new int[2];
        int corner = random.nextInt(4);

        switch (corner) {
            case 0: //top left
                position[0] = 0;
                position[1] = 0;
                break;
            case 1: //top right
                position[0] = GameplayPanel.width;
                position[1] = 0;
                break;
            case 2: //bottom left
                position[0] = 0;
                position[1] = GameplayPanel.height;
                break;
            case 3: //bottom right
                position[0] = GameplayPanel.width;
                position[1] = GameplayPanel.height;
                break;
        }
        return position;
    }
}

