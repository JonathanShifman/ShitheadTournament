package games.shithead.messages;

import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import games.shithead.game.IGameCard;
import games.shithead.game.IPlayerHand;

/**
 * Sent from <code>GameActor</code> to <code>PlayerActor</code>.
 * Sends each player the the cards that have been dealt to all players in the game.
 * Only revealed table cards' faces are not nullified. 
 * This message also contains the number of players and the players order, and signals the effective start of the game.
 *
 */
public class PostMoveMessage {

    private Map<Integer, IPlayerHand> playerHands;
    private List<IGameCard> pile;
    private int nextMoveId;
    private int nextPlayerTurnId;

    public PostMoveMessage(Map<Integer, IPlayerHand> playerHands, List<IGameCard> pile, int nextMoveId, int nextPlayerTurnId) {
        this.playerHands = playerHands;
        this.pile = pile;
        this.nextMoveId = nextMoveId;
        this.nextPlayerTurnId = nextPlayerTurnId;
    }

    public Map<Integer, IPlayerHand> getPlayerHands() {
        return playerHands;
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
