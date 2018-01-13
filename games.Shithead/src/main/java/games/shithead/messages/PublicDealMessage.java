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
public class PublicDealMessage {
	
	private int numberOfPlayers;
	private Deque<Integer> playingQueue;
	private Map<Integer, List<IGameCard>> publicDeals;
	
	public PublicDealMessage(int numberOfPlayers, Deque<Integer> playingQueue) {
		this.numberOfPlayers = numberOfPlayers;
		this.playingQueue = playingQueue;
		this.publicDeals = new HashMap<Integer, List<IGameCard>>();
	}
	
	//FIXME: players can modify message
	public void setDeal(int playerId, List<IGameCard> deal) {
		publicDeals.put(playerId, deal);
	}
	
	public List<IGameCard> getDeal(int playerId) {
		return publicDeals.get(playerId);
	}

	public int getNumberOfPlayers() {
		return numberOfPlayers;
	}

	public Deque<Integer> getPlayingQueue() {
		return playingQueue;
	}
	

}
