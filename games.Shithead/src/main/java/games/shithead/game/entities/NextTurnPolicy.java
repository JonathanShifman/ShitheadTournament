package games.shithead.game.entities;

/**
 * This enum represents all possible ways in which the turn of the next player should be determined
 */
public enum NextTurnPolicy {
    /**
     * The next turn should be determined by advancing the playing queue once
     */
    REGULAR,

    /**
     * The next turn should be determined by advancing the playing queue twice (i.e. skipping one player)
     */
    SKIP,

    /**
     * The next turn is awarded to the player who made the last action
     */
    STEAL
}
