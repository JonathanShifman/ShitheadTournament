package games.shithead.messages;

import games.shithead.game.IGameCard;

import java.util.LinkedList;
import java.util.List;

/**
 * Sent from <code>PlayerActor</code> to <code>GameActor</code>.
 * Contains the action a player chose to make.
 *
 */
public class PlayerActionInfo {

    private final int playerId;
    private final List<Integer> cardsToPut;

    public PlayerActionInfo(int playerId, List<Integer> cardsToPut) {
        this.playerId = playerId;
        this.cardsToPut = cardsToPut;
    }

    public List<Integer> getCardsToPut() {
        return cardsToPut;
    }

    public int getPlayerId() {
        return playerId;
    }

}
