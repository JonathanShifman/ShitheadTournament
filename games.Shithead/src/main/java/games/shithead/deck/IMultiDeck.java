package games.shithead.deck;

import java.util.List;

public interface IMultiDeck {
	
	ICard getNextCard();
	
	List<ICard> getNextCards(int numberOfCards);
	
}
