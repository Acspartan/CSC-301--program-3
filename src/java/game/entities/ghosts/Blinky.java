package game.entities.ghosts;

import game.Game;
import game.ghostStrategies.BlinkyStrategy;

// Classe concrète de Blinky (le fantôme rouge)
// EDITED FROM ORIGINAL: Refactored to extend BFSGhost instead of Ghost
// CHANGE 1: Class inheritance changed from Ghost -> BFSGhost
// CHANGE 2: Removed duplicate BFS code (bfsNextMove, isValid methods) - now inherited from BFSGhost
// CHANGE 3: Removed update() method override - now inherited from BFSGhost
// CHANGE 4: Added abstract method implementations for target positioning
// BENEFIT: Eliminated 168 lines of duplicate code, improved maintainability
public class Blinky extends BFSGhost {
    public Blinky(int xPos, int yPos) {
        super(xPos, yPos, "blinky.png");
        setStrategy(new BlinkyStrategy());
    }

    // CHANGED: Implements abstract methods from BFSGhost
    // Blinky always targets Pacman's current position (direct chase strategy)
    @Override
    protected int getTargetRow() {
        return Game.getPacman().getyPos();
    }

    // CHANGED: Column target is Pacman's x position
    @Override
    protected int getTargetCol() {
        return Game.getPacman().getxPos();
    }
}
