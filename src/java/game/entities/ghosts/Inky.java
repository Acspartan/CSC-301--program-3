package game.entities.ghosts;

import game.Game;
import game.ghostStates.PinkyChaseMode;
import game.ghostStrategies.InkyStrategy;

//Classe concrète de Inky (le fantôme bleu)
// Hybrid of Blinky (BFS pathfinding via inheritance) and Pinky (A* strategy with PinkyChaseMode)
public class Inky extends BFSGhost {
    public Inky(int xPos, int yPos) {
        super(xPos, yPos, "inky.png");
        setStrategy(new InkyStrategy(Game.getBlinky()));
        
        // Use A* strategy with PinkyChaseMode (from Pinky)
        this.chaseMode = new PinkyChaseMode(this);
    }

    // CHANGED: Implements abstract methods from BFSGhost
    // Inky uses its state's target position (which uses InkyStrategy/Pinky's logic)
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
