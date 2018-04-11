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
    private final int moveId;
    private final int victimId;

    public PlayerActionInfo(int playerId, List<Integer> cardsToPut, int moveId) {
        this.playerId = playerId;
        this.cardsToPut = cardsToPut;
        this.moveId = moveId;
        this.victimId = -1;
    }

    public PlayerActionInfo(int playerId, List<Integer> cardsToPut, int moveId, int victimId) {
        this.playerId = playerId;
        this.cardsToPut = cardsToPut;
        this.moveId = moveId;
        this.victimId = victimId;
    }

    public List<Integer> getCardsToPut() {
        return cardsToPut;
    }

    public int getPlayerId() {
        return playerId;
    }

    public int getMoveId() {
        return moveId;
    }

    public int getVictimId() {
        return victimId;
    }
}
