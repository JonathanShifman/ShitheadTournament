package games.shithead.messages;

import games.shithead.game.IGameCard;

import java.util.LinkedList;
import java.util.List;

/**
 * Sent from <code>PlayerActor</code> to <code>GameActor</code>.
 * Contains the action a player chose to make.
 *
 */
public class PlayerActionMessage {

    /* The unique ids of the cards the player would like to play.
     * An empty list indicates the player is taking the pile. */
    private final List<Integer> cardsToPut;

    /* The id of the move this action is relevant for.
     * Used to prevent ambiguity in case an action message arrives too late. */
    private final int moveId;

    /* The id of the chosen victim (the player who takes the pile) when a joker is played.
     * Will be ignored if a joker wasn't played. */
    private final int victimId;

    public PlayerActionMessage(List<Integer> cardsToPut, int moveId) {
        this.cardsToPut = cardsToPut;
        this.moveId = moveId;
        this.victimId = -1;
    }

    public PlayerActionMessage(List<Integer> cardsToPut, int moveId, int victimId) {
        this.cardsToPut = cardsToPut;
        this.moveId = moveId;
        this.victimId = victimId;
    }

    public List<Integer> getCardsToPut() {
        return cardsToPut;
    }

    public int getMoveId() {
        return moveId;
    }

    public int getVictimId() {
        return victimId;
    }
}
