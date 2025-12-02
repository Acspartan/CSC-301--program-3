# Scoring & Scared Timer Implementation - Changes Summary

This document details all changes made to implement the new scoring system and 10-move scared timer for the Pac-Man game.

---

## Overview of Changes

### 1. **Scoring System Update**
- **SuperPacGum (Capsule):** +500 points (was 100)
- **Regular PacGum:** +10 points (unchanged)
- **Ghost eaten during scared timer:** +200 points (was 500)
- **Per-move penalty:** -1 point per Pacman move (new)

### 2. **Scared Timer (Ghost Vulnerability)**
- Changed from **frame-based (7 seconds)** to **move-based (10 Pacman moves)**
- Scared duration now decrements on each Pacman move, not per frame
- Provides more consistent and fair gameplay

---

## File Edits with Comments

### File 1: `src/java/game/Observer.java`
**Change:** Added `updateMove()` method to notify observers when Pacman moves.

```java
// EDITED FROM ORIGINAL: notify observers when Pacman moves (per-move effects)
void updateMove();
```

**Why:** Enables per-move penalties and scared timer decrement logic.

---

### File 2: `src/java/game/Sujet.java`
**Change:** Added `notifyObserverMove()` method to allow Pacman to notify observers of movement.

```java
// EDITED FROM ORIGINAL: added to notify observers when Pacman moves
void notifyObserverMove();
```

**Why:** Implements the notification pattern used by the observer architecture.

---

### File 3: `src/java/game/entities/Pacman.java`
**Change 1:** Added `notifyObserverMove()` implementation.

```java
// EDITED FROM ORIGINAL: notify observers that Pacman made a move (used for per-move penalties and scared timer updates)
@Override
public void notifyObserverMove() {
    observerCollection.forEach(obs -> obs.updateMove());
}
```

**Change 2:** Call `notifyObserverMove()` after position update.

```java
if (!WallCollisionDetector.checkWallCollision(this, xSpd, ySpd)) {
    updatePosition();
    // EDITED FROM ORIGINAL: notify observers that Pacman made a move (applies per-move penalties and decrements scared timers)
    notifyObserverMove();
}
```

**Why:** Triggers the per-move effects (scoring penalty and scared timer decrement) each time Pacman moves.

---

### File 4: `src/java/game/UIPanel.java`
**Change 1:** Updated SuperPacGum score from 100 to 500.

```java
@Override
public void updateSuperPacGumEaten(SuperPacGum spg) {
    // EDITED FROM ORIGINAL: capsules (SuperPacGum) give +500 points
    updateScore(500);
}
```

**Change 2:** Updated ghost eating score from 500 to 200.

```java
@Override
public void updateGhostCollision(Ghost gh) {
    if (gh.getState() instanceof FrightenedMode) {
        // EDITED FROM ORIGINAL: eating a ghost during scared timer gives +200
        updateScore(200);
    }
}
```

**Change 3:** Added `updateMove()` to apply per-move penalty of -1 point.

```java
// EDITED FROM ORIGINAL: per-move penalty; called on each Pacman move
@Override
public void updateMove() {
    updateScore(-1); // time penalty per move
}
```

**Why:** Implements the new scoring values and per-move penalty mechanics.

---

### File 5: `src/java/game/entities/ghosts/Ghost.java`
**Change 1:** Added move-based frightened counter.

```java
protected int modeTimer = 0;
protected int frightenedTimer = 0; // used for frightened animation only
protected int frightenedMovesRemaining = 0; // EDITED FROM ORIGINAL: move-based scared timer (number of Pacman moves remaining)
protected boolean isChasing = false;
```

**Change 2:** Initialize `frightenedMovesRemaining = 10` when entering frightened mode.

```java
public void switchFrightenedMode() {
    frightenedTimer = 0;
    frightenedMovesRemaining = 10; // EDITED FROM ORIGINAL: frightened lasts 10 Pacman moves
    state = frightenedMode;
}
```

**Change 3:** Remove frame-based timeout from update(); now only increments animation timer.

```java
if (state == frightenedMode) {
    // keep animation timer for rendering only
    frightenedTimer++;
}
```

**Change 4:** Add method to decrement move-based frightened counter.

```java
// EDITED FROM ORIGINAL: decrement frightened move counter; when it reaches zero, notify state
public void reduceFrightenedMove() {
    if (frightenedMovesRemaining > 0) {
        frightenedMovesRemaining--;
        if (frightenedMovesRemaining <= 0) {
            state.timerFrightenedModeOver();
        }
    }
}
```

**Why:** Switches from frame-based (7 seconds) to move-based (10 Pacman moves) scared timer.

---

### File 6: `src/java/game/Game.java`
**Change:** Added `updateMove()` implementation to decrement ghost scared timers.

```java
// EDITED FROM ORIGINAL: handle per-move notifications (called by Pacman when it moves)
@Override
public void updateMove() {
    // Decrement frightened counters on each ghost; ghosts manage their own transition when counter hits zero
    for (Ghost gh : ghosts) {
        gh.reduceFrightenedMove();
    }
}
```

**Why:** Notifies all ghosts to decrement their scared-move counters on each Pacman move.

---

### File 7: `src/java/game/entities/ghosts/Blinky.java`
**Change:** Updated frightened mode animation handling to not trigger state transition (moved to move-based).

```java
// Handle animation timer for frightened mode
if (state == frightenedMode) {
    frightenedTimer++;
}
```

**Why:** Separates animation logic from state transition logic; transitions now only occur when `frightenedMovesRemaining <= 0`.

---

## Summary Table

| Component | Old Value | New Value | Type |
|-----------|-----------|-----------|------|
| SuperPacGum points | 100 | **500** | Scoring |
| Ghost eaten points | 500 | **200** | Scoring |
| Regular PacGum points | 10 | 10 | Scoring (unchanged) |
| Per-move penalty | 0 | **-1** | Scoring (new) |
| Scared duration (frame-based) | 7 seconds | — | Removed |
| Scared duration (move-based) | — | **10 Pacman moves** | New |

---

## Testing Recommendations

1. **Verify Scoring:**
   - Eat a SuperPacGum (capsule) and confirm +500 points
   - Eat a regular PacGum (food) and confirm +10 points
   - Eat a scared ghost and confirm +200 points
   - Move Pacman 5 times without eating anything; verify score decreases by 5 (-1 per move)

2. **Verify Scared Timer:**
   - Eat a SuperPacGum
   - Count Pacman's moves: ghosts should be vulnerable for exactly 10 moves
   - On the 11th move, ghosts should return to chase/scatter mode behavior

3. **Verify Integration:**
   - All scoring updates display correctly in the UI
   - Game state transitions properly after scared timer expires
   - No compilation errors or runtime exceptions

---

## Files Modified
1. `src/java/game/Observer.java`
2. `src/java/game/Sujet.java`
3. `src/java/game/entities/Pacman.java`
4. `src/java/game/UIPanel.java`
5. `src/java/game/entities/ghosts/Ghost.java`
6. `src/java/game/Game.java`
7. `src/java/game/entities/ghosts/Blinky.java`

All files pass static error checking. No additional dependencies added.
