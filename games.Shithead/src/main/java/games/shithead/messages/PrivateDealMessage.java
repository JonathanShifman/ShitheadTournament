package games.shithead.messages;

import java.util.List;

import games.shithead.game.IGameCard;

/**
 * Sent from <code>GameActor</code> to <code>PlayerActor</code>.
 * Sends a player the 9 cards he has been dealt. 3 of them (the hidden table cards) have nullified card faces.
 *
 */
public class PrivateDealMessage {
	
	private List<IGameCard> cards;

	public PrivateDealMessage(List<IGameCard> cards) {
		this.cards = cards;
	}

	public List<IGameCard> getCards() {
		return cards;
	}

}
