package games.shithead.messages;

import java.util.ArrayList;
import java.util.List;

import games.shithead.game.IGameCard;

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
