package games.shithead.game.entities;

import java.util.List;

/**
 * Contains info about the action a player chose to make.
 * Namely, the cards the player chose to put and the victim id in case a joker was played.
 */
public class PlayerActionInfo {

    /* The unique ids of the cards the player would like to play.
     * An empty list indicates the player is taking the pile. */
    private final List<Integer> cardsToPut;

    /* The id of the chosen victim (the player who takes the pile) when a joker is played.
     * Will be ignored if a joker wasn't played. */
    private final int victimId;

    public PlayerActionInfo(List<Integer> cardsToPut) {
        this.cardsToPut = cardsToPut;
        this.victimId = -1;
    }

    public PlayerActionInfo(List<Integer> cardsToPut, int victimId) {
        this.cardsToPut = cardsToPut;
        this.victimId = victimId;
    }

    public List<Integer> getCardsToPut() {
        return cardsToPut;
    }

    public int getVictimId() {
        return victimId;
    }
}
