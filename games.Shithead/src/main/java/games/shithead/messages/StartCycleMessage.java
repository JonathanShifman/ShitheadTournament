package games.shithead.messages;

import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import games.shithead.game.IGameCard;

/**
 * Sent from <code>GameActor</code> to <code>PlayerActor</code>.
 * Sends each player the the cards that have been dealt to all players in the game.
 * Only revealed table cards' faces are not nullified. 
 * This message also contains the number of players and the players order, and signals the effective start of the game.
 *
 */
public class StartCycleMessage {

}
