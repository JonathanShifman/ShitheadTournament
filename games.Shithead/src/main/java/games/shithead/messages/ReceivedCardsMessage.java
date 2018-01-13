package games.shithead.messages;

import java.util.ArrayList;
import java.util.List;

import games.shithead.game.IGameCard;

/**
 * Sent from <code>GameActor</code> to <code>PlayerActor</code>.
 * After accepting a move from a certain player, sends this player the cards he has drawn from the deck (if any),
 * to complete his hand to 3 cards.
 *
 */
//FIXME: What if a player sends this message to another player?
public class ReceivedCardsMessage {
	
	private List<IGameCard> cards;
	
	public ReceivedCardsMessage() {
		cards = new ArrayList<IGameCard>();
	}
	
	public void addCard(IGameCard card) {
		this.cards.add(card);
	}

	public List<IGameCard> getCards() {
		return cards;
	}

}
