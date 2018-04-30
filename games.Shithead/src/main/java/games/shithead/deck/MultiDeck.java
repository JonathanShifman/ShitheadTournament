package games.shithead.deck;

import java.util.*;
import java.util.stream.Collectors;

public class MultiDeck implements IMultiDeck {

	private static final int MIN_REGULAR_RANK = 2;
	private static final int MAX_REGULAR_RANK = 14;
	private static final int NUMBER_OF_SUITS = 4;
	private static final int JOKER_RANK = 15;
	private static final int NUMBER_OF_JOKERS = 2;
	private static final int CARDS_PER_DECK = (MAX_REGULAR_RANK - MIN_REGULAR_RANK + 1) * NUMBER_OF_SUITS +
			NUMBER_OF_JOKERS;
	
	private int numberOfDecks;
	private int currentCardFaceIndex;
	private List<ICardFace> cardFaces;

	/**
	 * Initializes a new shuffled multi-deck, consisting of a given number of
	 * regular 54 card decks.
	 * @param numberOfDecks The number of card decks to include in the multi-deck
	 */
	public MultiDeck(int numberOfDecks) {
		this.numberOfDecks = numberOfDecks;
		this.currentCardFaceIndex = 0;
		init();
		shuffle();
	}

	/**
	 * Initializes the multi-deck by inserting the appropriate amount of regular decks.
	 * The multi-deck is not shuffled after initialization.
	 */
	private void init() {
		cardFaces = new ArrayList<>(CARDS_PER_DECK * numberOfDecks);
		for(int deckIndex = 0; deckIndex < numberOfDecks; deckIndex++) {
			insertSingleDeck(deckIndex);
		}
	}

	/**
	 * Inserts the contents of a single regular card deck into the multi-deck.
	 * @param deckIndex The current deck index (should increment by 1 with each inserted deck)
	 */
	private void insertSingleDeck(int deckIndex) {
		int currentIndex = deckIndex * CARDS_PER_DECK;

		// Insert all non joker cards
		for(int currentRank = MIN_REGULAR_RANK; currentRank <= MAX_REGULAR_RANK; currentRank++) {
			for(int currentSuit = 1; currentSuit <= NUMBER_OF_SUITS; currentSuit++) {
				cardFaces.add(currentIndex, new CardFace(currentRank, currentSuit));
				currentIndex++;
			}
		}

		// Insert jokers
		for(int currentSuit = 1; currentSuit <= NUMBER_OF_JOKERS; currentSuit++) {
			cardFaces.add(currentIndex, new CardFace(JOKER_RANK, currentSuit));
			currentIndex++;
		}
	}
	
	private void shuffle() {
		Collections.shuffle(cardFaces);
	}

	@Override
	public ICardFace getNextCardFace() {
		if (currentCardFaceIndex >= cardFaces.size()) {
			return null;
		}
		return cardFaces.get(currentCardFaceIndex++);
	}

	@Override
	public List<ICardFace> getNextCardFaces(int numberOfCards) {
		List<ICardFace> cardFaces = new LinkedList<>();
		for(int i = 0; i < numberOfCards; i++) {
			ICardFace cardFace = getNextCardFace();
			if (cardFace != null) {
				cardFaces.add(cardFace);
			}
			else {
				break;
			}
		}
		return cardFaces;
	}
	
	@Override
	public int getNumberOfDecks() {
		return numberOfDecks;
	}
	
	@Override
	public int getNumberOfInitialCards() {
		return numberOfDecks * CARDS_PER_DECK;
	}

	@Override
	public boolean isEmpty() {
		return currentCardFaceIndex >= cardFaces.size();
	}

	@Override
	public String toString() {
		return cardFaces.stream()
				.map(cardFace -> CardDescriptionGenerator.cardFaceToDescription(cardFace))
				.collect(Collectors.joining(","));
	}


}
