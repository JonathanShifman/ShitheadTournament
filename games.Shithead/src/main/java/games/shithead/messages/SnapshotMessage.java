package games.shithead.messages;

import java.util.List;
import java.util.Map;

import games.shithead.game.interfaces.IGameCard;
import games.shithead.game.interfaces.IPlayerState;

/**
 * Sent from <code>GameActor</code> to <code>PlayerActor</code>.
 * Sends each player the the cards that have been dealt to all players in the game.
 * Only visible table cards' faces are not nullified.
 * This message also contains the number of players and the players order, and signals the effective start of the game.
 *
 */
public class SnapshotMessage {

    private Map<Integer, IPlayerState> playerStates;
    private List<IGameCard> pile;
    private int nextMoveId;
    private int nextPlayerTurnId;

    public SnapshotMessage(Map<Integer, IPlayerState> playerStates, List<IGameCard> pile, int nextMoveId, int nextPlayerTurnId) {
        this.playerStates = playerStates;
        this.pile = pile;
        this.nextMoveId = nextMoveId;
        this.nextPlayerTurnId = nextPlayerTurnId;
    }

    public Map<Integer, IPlayerState> getPlayerStates() {
        return playerStates;
    }

    public List<IGameCard> getPile() {
        return pile;
    }

    public int getNextMoveId() {
        return nextMoveId;
    }

    public int getNextPlayerTurnId() {
        return nextPlayerTurnId;
    }
}
