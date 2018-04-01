package games.shithead.messages;

import games.shithead.game.IGameCard;

import java.util.List;

/**
 * Sent from <code>PlayerActor</code> to <code>GameActor</code>.
 * Contains the action a player chose to make.
 *
 */
public class PlayerActionInfo {

    private final int playerId;
    private final List<Integer> cardsToPut;
    private final boolean isInterruption;

    public PlayerActionInfo(int playerId, List<Integer> cardsToPut, boolean isInterruption) {
        this.playerId = playerId;
        this.cardsToPut = cardsToPut;
        this.isInterruption = isInterruption;
    }

    public List<Integer> getCardsToPut() {
        return cardsToPut;
    }

    public int getPlayerId() {
        return playerId;
    }

    public boolean isInterruption() {
        return isInterruption;
    }

}
