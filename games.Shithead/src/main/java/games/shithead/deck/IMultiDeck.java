package games.shithead.deck;

import java.util.List;

public interface IMultiDeck {
	
	ICardFace getNextCardFace();
	
	List<ICardFace> getNextCardFaces(int numberOfCards);
	
	int getNumberOfDecks();
	
	int getNumberOfCards();

	boolean isEmpty();
	
}
