package games.shithead.messages;

import games.shithead.game.entities.PlayerActionInfo;

/**
 * Sent from <code>PlayerActor</code> to <code>GameActor</code>.
 * Contains the action a player chose to make.
 *
 */
public class PlayerMoveMessage {

    /* The PlayerActionInfo object containing info about the cards the player chose
     * to play, and the victim id if relevant. */
    private final PlayerActionInfo playerActionInfo;

    /* The id of the move this action is relevant for.
     * Used to prevent ambiguity in case an action message arrives too late. */
    private final int moveId;

    public PlayerMoveMessage(PlayerActionInfo playerActionInfo, int moveId) {
        this.playerActionInfo = playerActionInfo;
        this.moveId = moveId;
    }

    public PlayerActionInfo getPlayerActionInfo() {
        return playerActionInfo;
    }

    public int getMoveId() {
        return moveId;
    }
}
