package games.shithead.deck;

import java.util.List;

/**
 * This interface represents a multi deck, which consists of the combination of cards
 * of (potentially) multiple regular card decks.
 */
public interface IMultiDeck {

	/**
	 * Returns the next card face and removes it from the shuffled multi-deck, or null
	 * if the multi-deck is empty.
	 * @return The next card face
	 */
	ICardFace getNextCardFace();

	/**
	 * Returns the specified amount of the next card faces and removes them from the multi-deck.
	 * If there aren't enough cards, the amount left in the multi-deck will be returned.
	 * @param numberOfCards The number of cards to return
	 * @return The next card faces
	 */
	List<ICardFace> getNextCardFaces(int numberOfCards);

	/**
	 * Returns the number of regular decks the multi-deck was initialized with.
	 * @return The number of regular decks in the multi-deck
	 */
	int getNumberOfDecks();

	/**
	 * Returns the number of cards the multi-deck was initialized with.
	 * @return the number of initial cards in the multi-deck
	 */
	int getNumberOfInitialCards();

	/**
	 * Returns the number of cards currently remaining in the multi-deck.
	 * @return the number of cards currently in the multi-deck
	 */
	int getNumberOfRemainingCards();

	/**
	 * Checks if the multi-deck is empty.
	 * @return True if the multi-deck is empty, false otherwise
	 */
	boolean isEmpty();
	
}
