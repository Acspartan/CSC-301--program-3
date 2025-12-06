package game.entities.ghosts;

import game.Game;
import game.ghostStates.InkyChaseMode;
import game.ghostStrategies.InkyStrategy;

//Classe concrète de Inky (le fantôme bleu)
// HYBRID IMPLEMENTATION: Combines A* pathfinding (like Pinky) with Inky's strategic targeting
// Inky targets based on Blinky's position relative to Pacman, then uses A* to find the shortest path
public class Inky extends Ghost {
    public Inky(int xPos, int yPos) {
        super(xPos, yPos, "inky.png");
        setStrategy(new InkyStrategy(Game.getBlinky()));
        
        // Use InkyChaseMode: A* pathfinding with InkyStrategy targeting (hybrid of Blinky+Pinky approaches)
        this.chaseMode = new InkyChaseMode(this);
    }

    }
}
