package game.entities.ghosts;

import game.Game;
import game.ghostStrategies.InkyStrategy;

//Classe concrète de Inky (le fantôme bleu)
// Hybrid Implementation: Combines BFS pathfinding (like Blinky) with Inky's strategic targeting (based on Blinky position)
public class Inky extends BFSGhost {
    public Inky(int xPos, int yPos) {
        super(xPos, yPos, "inky.png");
        setStrategy(new InkyStrategy(Game.getBlinky()));
    }

    // CHANGED: Implements abstract methods from BFSGhost
    // Inky uses its state's target position (which uses InkyStrategy)
    // InkyStrategy targets a position calculated from Blinky's position relative to Pacman
    // This makes Inky unpredictable (different from Blinky's direct chase)
    @Override
    protected int getTargetRow() {
        return state.getTargetPosition()[1];
    }

    // CHANGED: Column target is from InkyStrategy calculation
    @Override
    protected int getTargetCol() {
        return state.getTargetPosition()[0];
    }
}
