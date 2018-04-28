package games.shithead.messages;

import java.util.List;
import java.util.Map;

import games.shithead.game.interfaces.IGameCard;
import games.shithead.game.interfaces.IPlayerState;

/**
 * Sent from GameActor to PlayerActor.
 * Contains a snapshot with all the relevant information about the current state of the game.
 * Namely: The states of all players, the contents of the pile, the next move id and the next player id.
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
